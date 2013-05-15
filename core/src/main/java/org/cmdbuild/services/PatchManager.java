package org.cmdbuild.services;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.TableType;
import org.cmdbuild.utils.FileUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Lists;

public class PatchManager {

	public class Patch {

		private final String version;
		private String description;
		private String filePath;

		protected Patch(final File file) throws ORMException, IOException {
			this(file, false);
		}

		protected Patch(final File file, final boolean fake) throws ORMException, IOException {
			this.version = extractVersion(file);
			if (fake) {
				description = "Create database";
				filePath = EMPTY;
			} else {
				final BufferedReader input = new BufferedReader(new FileReader(file));
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

		private String extractVersion(final File file) throws ORMException {
			final Pattern testFileNameSintax = Pattern.compile("(\\d\\.\\d\\.\\d-\\d{2})\\.sql");
			final Matcher fileNameParts = testFileNameSintax.matcher(file.getName());
			if (!fileNameParts.lookingAt()) {
				throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
			}
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

	private static final String PATCHES_TABLE = "Patch";
	private static final String PATCHES_FOLDER = "patches";
	private static final String PATCH_PATTERN = "[\\d\\.]+-[\\d]+\\.sql";

	private final DataSource dataSource;
	private final CMDataView dataView;
	private final DataAccessLogic dataAccessLogic;
	private final DataDefinitionLogic dataDefinitionLogic;
	private final FilesStore filesStore;

	private Patch lastAvaiablePatch;
	private List<Patch> availablePatch;

	public PatchManager(final DataSource dataSource, final CMDataView dataView, final DataAccessLogic dataAccessLogic,
			final DataDefinitionLogic dataDefinitionLogic, final FilesStore filesStore) {
		this.dataSource = dataSource;
		this.dataView = dataView;
		this.dataAccessLogic = dataAccessLogic;
		this.dataDefinitionLogic = dataDefinitionLogic;
		this.filesStore = filesStore;
	}

	public void reset() {
		synchronized (this) {
			final String lastAppliedPatch = getLastAppliedPatch();
			setAvaiblePatches(lastAppliedPatch);
		}
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
					.thatIsSystem(true) //
					.build());
			final JdbcTemplate template = new JdbcTemplate(applicationContext().getBean(DataSource.class));
			template.execute("COMMENT ON TABLE \"Patch\""
					+ " IS 'DESCR: Applied patches|MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: class'");
		}
		return patchTable;
	}

	private void setAvaiblePatches(final String lastAppliedPatch) {
		this.availablePatch = Lists.newLinkedList();
		final File[] patchFiles = filesStore.listFiles(PATCHES_FOLDER, PATCH_PATTERN);
		Arrays.sort(patchFiles, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			}
		});
		Log.CMDBUILD.info("Number of fetched patches: " + patchFiles.length);
		Log.CMDBUILD.info("Last patch " + patchFiles[patchFiles.length - 1]);
		setLastPatch(patchFiles);
		for (final File file : patchFiles) {
			try {
				final Patch patch = new Patch(file);
				if (lastAppliedPatch.compareTo(patch.getVersion()) < 0) {
					this.availablePatch.add(patch);
				}
			} catch (final ORMException e) {
				this.availablePatch = null;
			} catch (final IOException e) {
				this.availablePatch = null;
			}
		}
	}

	private void setLastPatch(final File[] patcheFiles) {
		if (patcheFiles.length > 0) {
			final File file = patcheFiles[patcheFiles.length - 1];
			try {
				this.lastAvaiablePatch = new Patch(file, true);
				Log.CMDBUILD.info("Last available patch is " + lastAvaiablePatch.getVersion());
			} catch (final ORMException e) {
				this.lastAvaiablePatch = null;
			} catch (final IOException e) {
				this.lastAvaiablePatch = null;
			}
		}
	}

	public void applyPatchList() throws SQLException {
		final List<Patch> currentPatch = Lists.newLinkedList(availablePatch);
		for (final Patch patch : currentPatch) {
			applyPatch(patch);
			createPatchCard(patch);
			availablePatch.remove(patch);
		}
	}

	private void applyPatch(final Patch patch) throws SQLException, ORMException {
		Log.CMDBUILD.info("Applying patch " + patch.getVersion());
		final AtomicBoolean error = new AtomicBoolean(false);
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

	public List<Patch> getAvaiblePatch() {
		if (availablePatch != null) {
			return availablePatch;
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
			Log.CMDBUILD.info("Creating card for last available patch in Patch table... Patch version: "
					+ lastAvaiablePatch.getVersion());
			createPatchCard(this.lastAvaiablePatch);
			this.availablePatch.clear();
		}
	}

}
