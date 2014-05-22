package org.cmdbuild.common.java.sql;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.common.java.sql.DataSourceTypes.mysql;
import static org.cmdbuild.common.java.sql.DataSourceTypes.oracle;
import static org.cmdbuild.common.java.sql.DataSourceTypes.postgresql;
import static org.cmdbuild.common.java.sql.DataSourceTypes.sqlserver;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import net.sourceforge.jtds.jdbcx.JtdsDataSource;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.java.sql.DataSourceTypes.DataSourceType;
import org.cmdbuild.common.java.sql.DataSourceTypes.DataSourceTypeVisitor;
import org.cmdbuild.common.java.sql.DataSourceTypes.MySql;
import org.cmdbuild.common.java.sql.DataSourceTypes.Oracle;
import org.cmdbuild.common.java.sql.DataSourceTypes.PostgreSql;
import org.cmdbuild.common.java.sql.DataSourceTypes.SqlServer;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class DefaultDataSourceHelper implements DataSourceHelper {

	private static final Logger logger = LoggerFactory.getLogger(DefaultDataSourceHelper.class);

	private static final DataSource UNSUPPORTED = UnsupportedProxyFactory.of(DataSource.class).create();

	private static final String ORACLE_JDBC_DRIVER_CLASS_NAME = "oracle.jdbc.driver.OracleDriver";

	private static final Object[] ONE_NULL_ARGUMENT_ONLY = new Object[] { null };

	@Override
	public Iterable<DataSourceType> getAvailableTypes() {
		final Collection<DataSourceType> available = Sets.newHashSet();
		final DataSourceTypeVisitor visitor = new DataSourceTypeVisitor() {

			@Override
			public void visit(final MySql type) {
				if (hasClass(MysqlDataSource.class)) {
					available.add(mysql());
				}
			}

			@Override
			public void visit(final Oracle type) {
				if (hasClass(ORACLE_JDBC_DRIVER_CLASS_NAME)) {
					available.add(oracle());
				}
			}

			@Override
			public void visit(final PostgreSql type) {
				if (hasClass(PGSimpleDataSource.class)) {
					available.add(postgresql());
				}
			}

			@Override
			public void visit(final SqlServer type) {
				if (hasClass(JtdsDataSource.class)) {
					available.add(sqlserver());
				}
			}

			private boolean hasClass(final Class<?> type) {
				return hasClass(type.getName());
			}

			private boolean hasClass(final String name) {
				try {
					Class.forName(name);
					return true;
				} catch (final Exception e) {
					return false;
				}
			}

		};
		for (final Method method : DataSourceTypeVisitor.class.getMethods()) {
			try {
				method.invoke(visitor, ONE_NULL_ARGUMENT_ONLY);
			} catch (final Exception e) {
				logger.error("error invoking method '{}'", method);
			}
		}
		return available;
	}

	@Override
	public DataSource create(final Configuration configuration) {
		return new DataSourceTypeVisitor() {

			private DataSource dataSource;

			public DataSource create() {
				configuration.getType().accept(this);
				Validate.notNull(dataSource, "creation error for type '{}'", configuration.getType());
				return dataSource;
			}

			@Override
			public void visit(final MySql type) {
				final MysqlDataSource dataSource = new MysqlDataSource();
				dataSource.setServerName(configuration.getHost());
				dataSource.setPortNumber(configuration.getPort());
				dataSource.setDatabaseName(configuration.getDatabase());
				dataSource.setUser(configuration.getUsername());
				dataSource.setPassword(configuration.getPassword());
			}

			@Override
			public void visit(final Oracle type) {
				dataSource = new ForwardingDataSource(UNSUPPORTED) {

					@Override
					public Connection getConnection() throws SQLException {
						return DriverManager.getConnection(url());
					}

					@Override
					public Connection getConnection(final String username, final String password) throws SQLException {
						return DriverManager.getConnection(url(), configuration.getUsername(),
								configuration.getPassword());
					}

					private String url() {
						return format("jdbc:oracle:thin:@%s:%d:%s", //
								configuration.getHost(), //
								configuration.getPort(), //
								configuration.getDatabase());
					}

				};
			}

			@Override
			public void visit(final PostgreSql type) {
				final PGSimpleDataSource dataSource = new PGSimpleDataSource();
				dataSource.setServerName(configuration.getHost());
				dataSource.setPortNumber(configuration.getPort());
				dataSource.setDatabaseName(configuration.getDatabase());
				dataSource.setUser(configuration.getUsername());
				dataSource.setPassword(configuration.getPassword());
			}

			@Override
			public void visit(final SqlServer type) {
				final JtdsDataSource dataSource = new JtdsDataSource();
				dataSource.setServerName(configuration.getHost());
				dataSource.setPortNumber(configuration.getPort());
				dataSource.setDatabaseName(configuration.getDatabase());
				if (!isBlank(configuration.getInstance())) {
					dataSource.setInstance(configuration.getInstance());
				}
				dataSource.setUser(configuration.getUsername());
				dataSource.setPassword(configuration.getPassword());
			}

		}.create();
	}

}
