package org.cmdbuild.services.sync.store.sql;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import javax.sql.DataSource;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.services.sync.logging.LoggingSupport;
import org.cmdbuild.services.sync.store.CardEntry;
import org.cmdbuild.services.sync.store.ClassType;
import org.cmdbuild.services.sync.store.Entry;
import org.cmdbuild.services.sync.store.Store;
import org.cmdbuild.services.sync.store.Type;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

public class SqlStore implements Store, LoggingSupport {

	private static final Marker marker = MarkerFactory.getMarker(SqlStore.class.getName());

	public static class Builder implements org.apache.commons.lang3.builder.Builder<SqlStore> {

		private static final Iterable<TableOrViewMapping> NO_MAPPINGS = Collections.emptyList();

		private DataSource dataSource;
		private final Collection<TableOrViewMapping> tableOrViewMappings = newHashSet();
		private SqlType type;

		private Builder() {
			// user factory method
		}

		@Override
		public SqlStore build() {
			validate();
			return new SqlStore(this);
		}

		private void validate() {
			Validate.notNull(dataSource, "missing '%s'", dataSource.getClass());
			Validate.notNull(type, "missing '%s'", Type.class);
		}

		public Builder withDataSource(final DataSource dataSource) {
			this.dataSource = dataSource;
			return this;
		}

		public Builder withTableOrViewMappings(final Iterable<? extends TableOrViewMapping> tableOrViewMappings) {
			addAll(this.tableOrViewMappings, defaultIfNull(tableOrViewMappings, NO_MAPPINGS));
			return this;
		}

		public Builder withType(final SqlType sqlType) {
			this.type = sqlType;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static interface Action<T> {

		T execute();

	}

	private static class Unsupported implements Action<Void> {

		@Override
		public Void execute() {
			throw new UnsupportedOperationException();
		}

	}

	private static class ReadAll implements Action<Iterable<Entry<?>>> {

		private final JdbcTemplate jdbcTemplate;
		private final Iterable<TableOrViewMapping> tableOrViewMappings;
		private final SqlType type;
		private final Collection<Entry<? extends Type>> entries;

		public ReadAll(final JdbcTemplate jdbcTemplate, final Iterable<TableOrViewMapping> tableOrViewMappings,
				final SqlType type) {
			this.jdbcTemplate = jdbcTemplate;
			this.tableOrViewMappings = tableOrViewMappings;
			this.type = type;
			this.entries = newHashSet();
		}

		@Override
		public Iterable<Entry<?>> execute() {
			for (final TableOrViewMapping tableOrViewMapping : tableOrViewMappings) {
				logger.debug(marker, "creating select statement for '{}'", tableOrViewMapping);
				final String sql = selectAllFrom(tableOrViewMapping);
				logger.debug(marker, "select statement '{}'", sql);
				logger.debug(marker, "executing select statement");
				try {
					jdbcTemplate.query(sql, new RowCallbackHandler() {

						@Override
						public void processRow(final ResultSet rs) throws SQLException {
							for (final TypeMapping typeMapping : tableOrViewMapping.getTypeMappings()) {
								final ClassType type = typeMapping.getType();
								final CardEntry.Builder builder = CardEntry.newInstance().withType(type);
								for (final AttributeMapping attributeMapping : typeMapping.getAttributeMappings()) {
									builder.withValue(attributeMapping.to(), rs.getObject(attributeMapping.from()));
								}
								entries.add(builder.build());
							}
						}

					});
				} catch (final RuntimeException e) {
					logger.error(marker, "error executing select statement", e);
					throw e;
				}
			}
			return entries;
		}

		// TODO use a visitor
		private String selectAllFrom(final TableOrViewMapping tableOrViewMapping) {
			final String sql;
			switch (type) {
			case ORACLE:
			case POSTGRESQL:
				sql = format("SELECT * FROM \"%s\"", tableOrViewMapping.getName());
				break;

			case MYSQL:
			case SQLSERVER:
			default:
				sql = format("SELECT * FROM %s", tableOrViewMapping.getName());
				break;
			}
			return sql;
		}
	}

	private static final Unsupported UNSUPPORTED = new Unsupported();

	private final JdbcTemplate jdbcTemplate;
	private final Iterable<TableOrViewMapping> tableOrViewMappers;
	private final SqlType type;

	private SqlStore(final Builder builder) {
		this.jdbcTemplate = new JdbcTemplate(builder.dataSource);
		this.tableOrViewMappers = builder.tableOrViewMappings;
		this.type = builder.type;
	}

	@Override
	public void create(final Entry<? extends Type> entry) {
		execute(UNSUPPORTED);
	}

	@Override
	public Iterable<Entry<?>> readAll() {
		return execute(doReadAll());
	}

	@Override
	public void update(final Entry<? extends Type> entry) {
		execute(UNSUPPORTED);
	}

	@Override
	public void delete(final Entry<? extends Type> entry) {
		execute(UNSUPPORTED);
	}

	private ReadAll doReadAll() {
		return new ReadAll(jdbcTemplate, tableOrViewMappers, type);
	}

	private <T> T execute(final Action<T> action) {
		return action.execute();
	}

}
