package org.cmdbuild.services;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.TableType;
import org.cmdbuild.utils.FileUtils;
import org.cmdbuild.utils.PatternFilenameFilter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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

	private final DBDataView dataView;
	private final DataAccessLogic dataAccessLogic;
	private final DataDefinitionLogic dataDefinitionLogic;

	private Patch lastAvaiablePatch;

	private PatchManager() {
		dataView = applicationContext().getBean(DBDataView.class);
		dataAccessLogic = applicationContext().getBean("systemDataAccessLogic", DataAccessLogic.class);
		dataDefinitionLogic = applicationContext().getBean(DataDefinitionLogic.class);

		final String lastAppliedPatch = getLastAppliedPatch();
		setAvaiblePatches(lastAppliedPatch);
	}

	private String getLastAppliedPatch() {
		try {
			final CMClass patchClass = dataAccessLogic.findClass(PATCHES_TABLE);
			final String code = dataView.select(anyAttribute(patchClass)) //
					.from(patchClass) //
					.limit(1) //
					.orderBy(attribute(patchClass, "Code"), Direction.DESC) //
					.run() //
					.getOnlyRow() //
					.getCard(patchClass) //
					.get("Code", String.class);
			return code;
		} catch (final Exception e) {
			/*
			 * return an empty string to allow the setAvailablePatches to take
			 * all the patches in the list
			 */
			return EMPTY;
		}
	}

	private CMClass getPatchTable() {
		CMClass patchTable = dataAccessLogic.findClass(PATCHES_TABLE);
		if (patchTable == null) {
			patchTable = dataDefinitionLogic.createOrUpdate(EntryType.newClass() //
					.withName(PATCHES_TABLE) //
					.withParent(dataAccessLogic.findClass(Constants.BASE_CLASS_NAME).getId()) //
					.withTableType(TableType.standard) //
					.thatIsSuperClass(false) //
					.build());
			JdbcTemplate template = new JdbcTemplate(applicationContext().getBean(DataSource.class));
			template.execute("COMMENT ON TABLE \"Patch\""
					+ " IS 'DESCR: |MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: class'");
		}
		return patchTable;
	}

	private void setAvaiblePatches(final String lastAppliedPatch) {
		final File patchesFolder = new File(PATH);
		this.availablePatch = new LinkedList<Patch>();
		final FilenameFilter filenameFilter = PatternFilenameFilter.build(PATCH_PATTERN);
		final String[] patchesName = patchesFolder.list(filenameFilter);
		java.util.Arrays.sort(patchesName, 0, patchesName.length);
		Log.OTHER.info("Number of fetched patches: " + patchesName.length);
		Log.OTHER.info("Last patch " + patchesName[patchesName.length - 1]);
		setLastPatch(patchesName);
		for (final String patchName : patchesName) {
			try {
				final Patch patch = new Patch(patchName);
				if (lastAppliedPatch.compareTo(patch.getVersion()) < 0)
					this.availablePatch.add(patch);
			} catch (final ORMException e) {
				this.availablePatch = null;
			} catch (final IOException e) {
				this.availablePatch = null;
			}
		}
	}

	private void setLastPatch(final String[] patchesName) {
		if (patchesName.length > 0) {
			final String patchName = patchesName[patchesName.length - 1];
			try {
				this.lastAvaiablePatch = new Patch(patchName, true);
				Log.OTHER.info("Last available patch is " + lastAvaiablePatch.getVersion());
			} catch (final ORMException e) {
				this.lastAvaiablePatch = null;
			} catch (final IOException e) {
				this.lastAvaiablePatch = null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void applyPatchList() throws SQLException {
		final LinkedList<Patch> currentPatch = (LinkedList<Patch>) this.availablePatch.clone();
		try {
			for (final Patch patch : currentPatch) {
				applyPatch(patch);
				createPatchCard(patch);
				availablePatch.remove(patch);
			}
		} finally {
			new CacheManager().clearDatabaseCache();
		}
	}

	private void applyPatch(final Patch patch) throws SQLException, ORMException {
		Log.OTHER.info("Applying patch " + patch.getVersion());
		final AtomicBoolean error = new AtomicBoolean(false);
		final DataSource dataSource = DBService.getInstance().getDataSource();
		final PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {
				try {
					final String sql = FileUtils.getContents(patch.getFilePath());
					new JdbcTemplate(dataSource).execute(sql);
				} catch (final DataAccessException e) {
					Log.SQL.error(String.format("failed applying patch '%s'", patch.getVersion()), e);
					status.setRollbackOnly();
					error.set(true);
				}
			}
		});
		if (error.get()) {
			throw ORMExceptionType.ORM_SQL_PATCH.createException();
		}
	}

	private void createPatchCard(final Patch patch) {
		final CMClass patchClass = getPatchTable();
		dataView.createCardFor(patchClass) //
				.setCode(patch.getVersion()) //
				.setDescription(patch.getDescription()) //
				.save();
	}

	public LinkedList<Patch> getAvaiblePatch() {
		if (this.availablePatch != null) {
			return this.availablePatch;
		} else {
			throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
		}
	}

	public boolean isUpdated() {
		final boolean isUpdated = this.availablePatch != null && this.availablePatch.isEmpty();
		return isUpdated;
	}

	// used in DatabaseConfigurator to set updated a new Database
	public void createLastPatch() {
		if (this.lastAvaiablePatch != null) {
			Log.OTHER.info("Creating card for last available patch in Patch table... Patch version: "
					+ lastAvaiablePatch.getVersion());
			createPatchCard(this.lastAvaiablePatch);
			this.availablePatch.clear();
		}
	}

	public class Patch {

		private final String version;
		private String description;
		private String filePath;

		protected Patch(final String fileName) throws ORMException, IOException {
			this(fileName, false);
		}

		protected Patch(final String fileName, final boolean fake) throws ORMException, IOException {
			this.version = extractVersion(fileName);
			if (fake) {
				description = "Create database";
				filePath = EMPTY;
			} else {
				this.filePath = PATH + File.separatorChar + fileName;
				final File patchFile = new File(filePath);
				final BufferedReader input = new BufferedReader(new FileReader(patchFile));
				final String firstLine = input.readLine();
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

		private String extractDescription(final String description) throws ORMException {
			final Pattern extractDescriptionPattern = Pattern.compile("--\\W*(.+)");
			final Matcher descriptionParts = extractDescriptionPattern.matcher(description);
			if (!descriptionParts.lookingAt()) {
				throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
			}
			return descriptionParts.group(1);
		}

		private String extractVersion(final String fileName) throws ORMException {
			final Pattern testFileNameSintax = Pattern.compile("(\\d\\.\\d\\.\\d-\\d{2})\\.sql");
			final Matcher fileNameParts = testFileNameSintax.matcher(fileName);
			if (!fileNameParts.lookingAt())
				throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
			return fileNameParts.group(1);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
					.append(version) //
					.append(description) //
					.append(filePath) //
					.toString();
		}

	}
}
