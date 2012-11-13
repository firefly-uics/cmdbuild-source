package utils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.SystemUtils;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.elements.database.DatabaseConfigurator;
import org.cmdbuild.services.Settings;

public class DBInitializer {

	private static final String DB_HOST = "host";
	private static final String DB_PORT = "port";
	private static final String DB_SUPER_USER = "super.user";
	private static final String DB_SUPER_PASSWORD = "super.password";
	private static final String DB_USER = "user";
	private static final String DB_PASSWORD = "password";
	private static String DB_NAME = "db.name";
	private static DatabaseConfigurator dbConfigurator;
	private static PostgresDriver pgDriver;

	public static void initDatabase() throws ConfigurationException {
		final Configuration conf = new PropertiesConfiguration("database.properties");
		final String webRoot = SystemUtils.USER_DIR.concat("/../../cmdbuild/src/main/webapp/WEB-INF/sql/");
		Settings.getInstance().setRootPath(SystemUtils.USER_DIR.concat("/../../cmdbuild/src/main/webapp/"));
		dbConfigurator = new DatabaseConfigurator(new DatabaseConfigurator.Configuration() {

			@Override
			public String getHost() {
				return conf.getString("host");
			}

			@Override
			public int getPort() {
				return conf.getInt("port");
			}

			@Override
			public String getUser() {
				return conf.getString("super.user");
			}

			@Override
			public String getPassword() {
				return conf.getString("super.password");
			}

			@Override
			public String getDatabaseName() {
				return conf.getString("db.name");
			}

			@Override
			public String getDatabaseType() {
				return DatabaseConfigurator.EMPTY_DBTYPE;
			}

			@Override
			public boolean useLimitedUser() {
				return false;
			}

			@Override
			public String getLimitedUser() {
				return conf.getString("user");
			}

			@Override
			public String getLimitedUserPassword() {
				return conf.getString("password");
			}

			@Override
			public boolean useSharkSchema() {
				return false;
			}

			@Override
			public String getSqlPath() {
				return webRoot;
			}

		});
		pgDriver = new PostgresDriver(dbConfigurator.systemDataSource());
		if (!databaseExists()) {
			createDatabase();
		}
	}

	private static void createDatabase() {
		dbConfigurator.configureAndDoNotSaveSettings();
	}

	private static boolean databaseExists() {
		final DataSource dataSource = dbConfigurator.systemDataSource();
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			return true;
		} catch (final SQLException ex) {
			return false;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static DBDriver getDBDriver() {
		return pgDriver;
	}

}
