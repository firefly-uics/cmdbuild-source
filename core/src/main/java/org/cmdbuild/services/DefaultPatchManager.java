package org.cmdbuild.services;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Ordering.from;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.io.FileUtils.lineIterator;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.common.Constants.BASE_CLASS_NAME;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public class DefaultPatchManager implements PatchManager {

	private static final Logger logger = CMDBUILD;

	private static class DefaultPatch implements Patch {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<DefaultPatch> {

			private File file;
			private boolean fake;
			private String version;
			private String description;

			/**
			 * Use factory method.
			 */
			private Builder() {
			}

			@Override
			public DefaultPatch build() {
				validate();
				return new DefaultPatch(this);
			}

			private void validate() {
				extractVersion();
				extractDescription();
			}

			private void extractVersion() {
				logger.debug("extracting version from file name '{}'", file);
				final Matcher matcher = compile(FILENAME_PATTERN).matcher(file.getName());
				if (!matcher.lookingAt()) {
					logger.error("file name does not match expected pattern");
					throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
				}
				version = matcher.group(1);
			}

			private void extractDescription() {
				if (fake) {
					description = "Create database";
				} else {
					logger.debug("extracting description from first line of file '{}'", file);
					final Matcher matcher = compile(FIRST_LINE_PATTERN).matcher(firstLineOfFile());
					if (!matcher.lookingAt()) {
						logger.error("first line does not match expected pattern");
						throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
					}
					description = matcher.group(1);
				}
			}

			private String firstLineOfFile() {
				LineIterator lines = null;
				try {
					lines = lineIterator(file);
					if (!lines.hasNext()) {
						logger.error("file '{}' seems empty", file);
						throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
					}
					return lines.next();
				} catch (final IOException e) {
					logger.error("error getting lines iterator", e);
					throw ORMExceptionType.ORM_MALFORMED_PATCH.createException();
				} finally {
					if (lines != null) {
						lines.close();
					}
				}
			}

			public Builder file(final File file) {
				this.file = file;
				return this;
			}

			public Builder fake(final boolean fake) {
				this.fake = fake;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private static final String FILENAME_PATTERN = "(\\d\\.\\d\\.\\d-\\d{2})\\.sql";
		private static final String FIRST_LINE_PATTERN = "--\\W*(.+)";

		private final String version;
		private final String description;
		private final File file;

		private DefaultPatch(final Builder builder) {
			this.version = builder.version;
			this.description = builder.description;
			this.file = builder.file;
		}

		@Override
		public String getVersion() {
			return version;
		}

		@Override
		public String getDescription() {
			return description;
		}

		File getFile() {
			return file;
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private static final String PATCHES_TABLE = "Patch";
	private static final String PATCH_PATTERN = "[\\d\\.]+-[\\d]+\\.sql";

	private static final String VERSION = CODE_ATTRIBUTE;
	private static final String DESCRIPTION = DESCRIPTION_ATTRIBUTE;

	private static final Comparator<File> BY_ABSOLUTE_PATH = new Comparator<File>() {

		@Override
		public int compare(final File o1, final File o2) {
			return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
		}

	};

	private static final Predicate<Patch> ALWAYS_TRUE = alwaysTrue();

	private final DataSource dataSource;
	private final CMDataView dataView;
	private final DataDefinitionLogic dataDefinitionLogic;
	private final FilesStore filesStore;
	private Optional<? extends Patch> lastAvaiablePatch;
	private final List<DefaultPatch> availablePatch;

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
		this.lastAvaiablePatch = absent();
		this.availablePatch = newLinkedList();
		reset();
	}

	@Override
	public void reset() {
		synchronized (this) {
			Predicate<Patch> predicate = ALWAYS_TRUE;
			try {
				predicate = new Predicate<Patch>() {

					final Alias P = name("P");
					final String version = dataView.select(anyAttribute(P)) //
							.from(getOrCreateClass(), as(P)) //
							.limit(1) //
							.orderBy(attribute(P, VERSION), DESC) //
							.run() //
							.getOnlyRow() //
							.getCard(P) //
							.get(VERSION, String.class);

					@Override
					public boolean apply(final Patch input) {
						return version.compareTo(input.getVersion()) < 0;
					}

				};
			} catch (final Exception e) {
				logger.error("error getting last applied patch version", e);
			} finally {
				final List<File> patchFiles = from(BY_ABSOLUTE_PATH) //
						.immutableSortedCopy(filesStore.files(PATCH_PATTERN));
				logger.info("fetched patches ({}): {}", patchFiles.size(), patchFiles);
				if (!patchFiles.isEmpty()) {
					final File file = from(patchFiles).last().get();
					try {
						logger.debug("creating last available patch from '{}'", file);
						lastAvaiablePatch = of(DefaultPatch.newInstance() //
								.file(file) //
								.fake(true) //
								.build());
						logger.info("last available patch is '{}'", lastAvaiablePatch);
					} catch (final Exception e) {
						logger.error("error creating last available patch", e);
						lastAvaiablePatch = absent();
					}
				}
				availablePatch.clear();
				for (final File file : patchFiles) {
					try {
						logger.debug("creating patch from '{}'", file);
						final DefaultPatch patch = DefaultPatch.newInstance() //
								.file(file) //
								.build();
						if (predicate.apply(patch)) {
							availablePatch.add(patch);
						}
					} catch (final Exception e) {
						logger.error("error creating patch", e);
						availablePatch.clear();
					}
				}
			}
		}
	}

	@Override
	public void applyPatchList() {
		for (final DefaultPatch patch : newLinkedList(availablePatch)) {
			applyPatch(patch);
			createPatchCard(patch);
			availablePatch.remove(patch);
		}
	}

	private void applyPatch(final DefaultPatch patch) throws ORMException {
		logger.info("applying patch '{}'", patch);
		final AtomicBoolean error = new AtomicBoolean(false);
		final PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {
				try {
					final String sql = readFileToString(patch.getFile());
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
		return from(availablePatch) //
				.filter(Patch.class);
	}

	@Override
	public boolean isUpdated() {
		return availablePatch.isEmpty();
	}

	@Override
	public void createLastPatch() {
		if (lastAvaiablePatch.isPresent()) {
			logger.info("creating card for last available patch '{}'", lastAvaiablePatch);
			createPatchCard(lastAvaiablePatch.get());
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
