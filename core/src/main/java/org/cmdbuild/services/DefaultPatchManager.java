package org.cmdbuild.services;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Ordering.from;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.io.FileUtils.lineIterator;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.common.Constants.BASE_CLASS_NAME;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.OrderByClause.Direction.DESC;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Aliases.as;
import static org.cmdbuild.dao.query.clause.alias.Aliases.name;
import static org.cmdbuild.logger.Log.CMDBUILD;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;

import javax.sql.DataSource;

import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.TableType;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Supplier;

public class DefaultPatchManager implements PatchManager {

	private static class DefaultPatch implements Patch {

		private final String version;
		private String description;
		private String filePath;

		public DefaultPatch(final File file) throws ORMException, IOException {
			this(file, false);
		}

		public DefaultPatch(final File file, final boolean fake) throws ORMException, IOException {
			version = extractVersion(file);
			if (fake) {
				description = "Create database";
				filePath = EMPTY;
			} else {
				LineIterator lines = null;
				try {
					lines = lineIterator(file);
					if (!lines.hasNext()) {
						throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
					}
					final String firstLine = lines.next();
					description = extractDescription(firstLine);
					filePath = file.getAbsolutePath();
				} finally {
					if (lines != null) {
						lines.close();
					}
				}
			}
		}

		@Override
		public String getVersion() {
			return version;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public String getFilePath() {
			return filePath;
		}

		private String extractDescription(final String description) throws ORMException {
			final Matcher descriptionParts = compile("--\\W*(.+)").matcher(description);
			if (!descriptionParts.lookingAt()) {
				throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
			}
			return descriptionParts.group(1);
		}

		private String extractVersion(final File file) throws ORMException {
			final Matcher fileNameParts = compile("(\\d\\.\\d\\.\\d-\\d{2})\\.sql").matcher(file.getName());
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

	private static final Logger logger = CMDBUILD;

	private static final String PATCHES_TABLE = "Patch";
	private static final String PATCHES_FOLDER = ".";
	private static final String PATCH_PATTERN = "[\\d\\.]+-[\\d]+\\.sql";

	private static final String VERSION = CODE_ATTRIBUTE;
	private static final String DESCRIPTION = DESCRIPTION_ATTRIBUTE;

	private static final Alias P = name("P");

	private static final Comparator<File> BY_ABSOLUTE_PATH = new Comparator<File>() {

		@Override
		public int compare(final File o1, final File o2) {
			return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
		}

	};

	private final DataSource dataSource;
	private final CMDataView dataView;
	private final DataDefinitionLogic dataDefinitionLogic;
	private final FilesStore filesStore;
	private Patch lastAvaiablePatch;
	private final List<Patch> availablePatch;

	public DefaultPatchManager( //
			final DataSource dataSource, //
			final CMDataView dataView, //
			final DataDefinitionLogic dataDefinitionLogic, //
			final FilesStore filesStore //
	) {
		this.dataSource = dataSource;
		this.dataView = dataView;
		this.dataDefinitionLogic = dataDefinitionLogic;
		this.filesStore = filesStore;
		this.availablePatch = newLinkedList();
		reset();
	}

	@Override
	public void reset() {
		synchronized (this) {
			try {
				setAvaiblePatches(dataView.select(attribute(P, VERSION)) //
						.from(getOrCreateClass(), as(P)) //
						.limit(1) //
						.orderBy(attribute(P, VERSION), DESC) //
						.run() //
						.getOnlyRow() //
						.getCard(P) //
						.get(VERSION, String.class));
			} catch (final Exception e) {
				setAvaiblePatches(EMPTY);
			}
		}
	}

	private void setAvaiblePatches(final String lastAppliedPatch) {
		availablePatch.clear();
		final List<File> patchFiles = from(BY_ABSOLUTE_PATH).immutableSortedCopy(
				filesStore.files(PATCHES_FOLDER, PATCH_PATTERN));
		logger.info("number of fetched patches: {}", patchFiles.size());
		logger.info("last patch: {}", patchFiles.get(patchFiles.size() - 1));
		if (!patchFiles.isEmpty()) {
			final File file = patchFiles.get(patchFiles.size() - 1);
			try {
				lastAvaiablePatch = new DefaultPatch(file, true);
				logger.info("last available patch is '{}'", lastAvaiablePatch.getVersion());
			} catch (final ORMException e) {
				lastAvaiablePatch = null;
			} catch (final IOException e) {
				lastAvaiablePatch = null;
			}
		}
		for (final File file : patchFiles) {
			try {
				final DefaultPatch patch = new DefaultPatch(file);
				if (lastAppliedPatch.compareTo(patch.getVersion()) < 0) {
					availablePatch.add(patch);
				}
			} catch (final ORMException e) {
				availablePatch.clear();
			} catch (final IOException e) {
				availablePatch.clear();
			}
		}
	}

	@Override
	public void applyPatchList() {
		for (final Patch patch : newLinkedList(availablePatch)) {
			applyPatch(patch);
			createPatchCard(patch);
			availablePatch.remove(patch);
		}
	}

	private void applyPatch(final Patch patch) throws ORMException {
		logger.info("applying patch '{}'", patch.getVersion());
		final AtomicBoolean error = new AtomicBoolean(false);
		final PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {
				try {
					final String sql = readFileToString(new File(patch.getFilePath()));
					new JdbcTemplate(dataSource).execute(sql);
				} catch (final IOException e) {
					logger.error("error reading content of file", e);
					error.set(true);
				} catch (final DataAccessException e) {
					logger.error(format("failed applying patch '%s'", patch.getVersion()), e);
					status.setRollbackOnly();
					error.set(true);
				}
			}

		});
		if (error.get()) {
			throw ORMExceptionType.ORM_SQL_PATCH.createException();
		}
	}

	@Override
	public Iterable<Patch> getAvaiblePatch() {
		return availablePatch;
	}

	@Override
	public boolean isUpdated() {
		return availablePatch.isEmpty();
	}

	@Override
	public void createLastPatch() {
		if (lastAvaiablePatch != null) {
			logger.info("creating card for last available patch '{}'", lastAvaiablePatch.getVersion());
			createPatchCard(lastAvaiablePatch);
			availablePatch.clear();
		}
	}

	private CMClass getOrCreateClass() {
		return fromNullable(dataView.findClass(PATCHES_TABLE)) //
				.or(new Supplier<CMClass>() {

					@Override
					public CMClass get() {
						return dataDefinitionLogic.createOrUpdate(EntryType.newClass() //
								.withName(PATCHES_TABLE) //
								.withDescription("Applied patches") //
								.withParent(dataView.findClass(BASE_CLASS_NAME).getId()) //
								.withTableType(TableType.standard) //
								.thatIsSuperClass(false) //
								.thatIsSystem(true) //
								.build());
					}

				});
	}

	private void createPatchCard(final Patch patch) {
		dataView.createCardFor(getOrCreateClass()) //
				.set(VERSION, patch.getVersion()) //
				.set(DESCRIPTION, patch.getDescription()) //
				.setUser("system") //
				.save();
	}

}
