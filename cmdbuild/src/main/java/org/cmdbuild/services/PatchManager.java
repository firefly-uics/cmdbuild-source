package org.cmdbuild.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.BaseSchema.SchemaStatus;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.FileUtils;
import org.cmdbuild.utils.PatternFilenameFilter;

public class PatchManager {
	private static PatchManager instance;

	private LinkedList<Patch> availablePatch;

	private static final String PATCHES_TABLE = "Patch";
	private static final String PATCHES_FOLDER = "WEB-INF/patches";
	private static final String PATCH_PATTERN = "[\\d\\.]+-[\\d]+\\.sql";
	private static final String PATH = Settings.getInstance().getRootPath() + PATCHES_FOLDER;

	private static Object SyncObject = new Object();

	public static PatchManager getInstance() {
		if (instance == null) {
			synchronized (SyncObject) {
				if (instance == null) {
					instance = new PatchManager();
				}
			}
		}
		return instance;
	}

	public static void reset() {
		synchronized (SyncObject) {
			instance = null;
		}
	}

	private Patch lastAvaiablePatch;
	
	private PatchManager() {
		String lastAppliedPatch = getLastAppliedPatch();
		setAvaiblePatches(lastAppliedPatch);
	}

	private String getLastAppliedPatch() {
		try {
			String code = getPatchTable().cards().list()
			.order("Code", OrderFilterType.DESC)
			.limit(1).get().getCode();
			return code;
		} catch (NotFoundException e){
			//return an empty string to allow the setAvailablePatches to
			//take all the patches in the list
			return "";
		}
	}
	
	private ITable getPatchTable() {
		ITable patchTable;
		try {
			patchTable = UserContext.systemContext().tables().get(PATCHES_TABLE);
		} catch (NotFoundException e) {
			patchTable = createPatchtable();
		}
		return patchTable;
	}
	
	private ITable createPatchtable() {
		ITable patchTable = UserContext.systemContext().tables().create();
		patchTable.setParent("Class");
		patchTable.setSuperClass(false);
		patchTable.setName(PATCHES_TABLE);
		patchTable.setMode(Mode.RESERVED.getModeString());
		patchTable.setStatus(SchemaStatus.ACTIVE);
		patchTable.save();
		return patchTable;
	}
	
	private void setAvaiblePatches(String lastAppliedPatch) {
		File patchesFolder = new File(PATH);
		this.availablePatch = new LinkedList<Patch>();
		FilenameFilter filenameFilter = PatternFilenameFilter.build(PATCH_PATTERN);
		String[] patchesName = patchesFolder.list(filenameFilter);
		java.util.Arrays.sort(patchesName, 0, patchesName.length);
		setLastPatch(patchesName);
		for (String patchName: patchesName) {
			try {
				Patch patch = new Patch(patchName);
				if (lastAppliedPatch.compareTo(patch.getVersion()) < 0)
					this.availablePatch.add(patch);
			} catch (ORMException e) {
				this.availablePatch = null;
			} catch (IOException e) {
				this.availablePatch = null;
			}
		}
	} 
	
	private void setLastPatch(String[] patchesName) {
		if (patchesName.length > 0) {
			String patchName = patchesName[patchesName.length -1];
			try {
				this.lastAvaiablePatch = new Patch(patchName, true);
			} catch (ORMException e) {
				this.lastAvaiablePatch = null;
			} catch (IOException e) {
				this.lastAvaiablePatch = null;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void applyPatchList() throws SQLException {
		LinkedList<Patch> currentPatch = (LinkedList<Patch>) this.availablePatch.clone();
		try {
			for (Patch patch: currentPatch) {
				applyPatch(patch);
			}
		} finally {
			new CacheManager().clearDatabaseCache();
		}
	}

	private void applyPatch(Patch patch) throws SQLException, ORMException {
		Connection con = DBService.getConnection();
		Statement stm = con.createStatement();
		con.setAutoCommit(false);
		try {
			stm.execute(FileUtils.getContents(patch.getFilePath()));
			createPatchCard(patch);
			this.availablePatch.remove(patch);
			con.commit();
		} catch (SQLException e) {
			con.rollback();
			Log.SQL.error(String.format("Failed to apply patch %s", patch.getVersion()), e);
			throw ORMExceptionType.ORM_SQL_PATCH.createException();
		} finally {
			stm.close();
		}
	}
	
	private void createPatchCard(Patch patch) {
		ICard cardPatch = getPatchTable().cards().create();
		cardPatch.setCode(patch.getVersion());
		cardPatch.setDescription(patch.getDescription());
		cardPatch.setStatus(ElementStatus.ACTIVE);
		cardPatch.save();
	}

	public LinkedList<Patch> getAvaiblePatch() {
		if (this.availablePatch != null)
			return this.availablePatch;
		else throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
	}

	public boolean isUpdated() {
		 return (this.availablePatch != null && this.availablePatch.isEmpty());
	}
	
	//used in DatabaseConfigurator to set updated a new Database
	public void createLastPatch() {
		if (this.lastAvaiablePatch != null ) {
			createPatchCard(this.lastAvaiablePatch);
			this.availablePatch.clear();
		}
	}
	
	public class Patch {
		private String version;
		private String description;
		private String filePath;

		protected Patch(String fileName) throws ORMException, IOException {			
			this(fileName, false);
		}

		protected Patch(String fileName, boolean fake) throws ORMException, IOException {			
			this.version = extractVersion(fileName);
			if (fake) {
				description = "Create database";
				filePath = "";
			} else {				
				this.filePath = PATH + File.separatorChar +fileName;
				File patchFile = new File(filePath);
				BufferedReader input =  new BufferedReader(new FileReader(patchFile));
				String firstLine = input.readLine();
				if (firstLine == null) {
					throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
				}
				description = extractDescription(firstLine);
			}
		}

		public String getVersion() {
			return version;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getFilePath() {
			return filePath;
		}
		
		private String extractDescription(String description) throws ORMException {
			Pattern extractDescriptionPattern = Pattern.compile("--\\W*(.+)");
			Matcher descriptionParts = extractDescriptionPattern.matcher(description);
			if (!descriptionParts.lookingAt()) {
				throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
			}
			return descriptionParts.group(1);
		}
		
		private String extractVersion(String fileName) throws ORMException {
			Pattern testFileNameSintax = Pattern.compile("(\\d\\.\\d\\.\\d-\\d{2})\\.sql");
			Matcher fileNameParts = testFileNameSintax.matcher(fileName);
			if(!fileNameParts.lookingAt())
				throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
			return fileNameParts.group(1);
		}
	}
}

