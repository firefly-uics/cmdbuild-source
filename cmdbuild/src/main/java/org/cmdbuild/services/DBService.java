package org.cmdbuild.services;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.jcip.annotations.GuardedBy;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.datasource.AbstractDataSource;

public class DBService {

	private static final String DATASOURCE_NAME = "jdbc/cmdbuild";
	private static final Class<?> DRIVER_CLASS = org.postgresql.Driver.class;
	private static final String MANAGEMENT_DATABASE = "postgres";

	protected final DataSource datasource;
	private ThreadLocal<Connection> connection = new ThreadLocal<Connection>();

	@GuardedBy("syncObject")
	private static volatile DBService instance;
	private static final Object syncObject = new Object();

	private DBService() {
		datasource = new LazyConfDataSource(newDataSource());
	}

	private BasicDataSource newDataSource() {
		BasicDataSource ds;
		try {
			InitialContext ictx = new InitialContext();
			Context ctx = (Context) ictx.lookup("java:/comp/env");
			ds = (BasicDataSource) ctx.lookup(DATASOURCE_NAME);
		} catch (NamingException e) {
			ds = new BasicDataSource();
		}
		ds.setDriverClassName(DRIVER_CLASS.getCanonicalName());
		return ds;
	}

	/*
	 * public for DatabaseHelper tests
	 */
	public static DBService getInstance() {
		if (instance == null) {
			synchronized (syncObject) {
				if (instance == null) {
					instance = new DBService();
				}
			}
		}
		return instance;
	}

	public static Connection getConnection() {
		DBService instance = DBService.getInstance();
		Connection con = instance.connection.get();
		if (con == null) {
			try {
				con = instance.datasource.getConnection();
				instance.connection.set(con);
			} catch (SQLException e) {
				Log.PERSISTENCE.error("Error trying to get database connection", e);
				throw ORMExceptionType.ORM_DATABASE_CONNECTION_ERROR.createException();
			} catch (IllegalStateException e) {
				Log.PERSISTENCE.error("Error trying to get database connection", e);
				throw ORMExceptionType.ORM_DBNOTCONFIGURED.createException();
			}
		}
		return con;
	}

	public static void releaseConnection() {
		if (instance == null)
			return;
		Connection con = instance.connection.get();
		if (con != null) {
			try {
				con.close();
			} catch (SQLException ex) {
				Log.SQL.error("Error closing database connection", ex);
			}
		}
		instance.connection.remove();
	}

	public static void close(ResultSet res, Statement stm) {
		try {
			if (res != null) {
				res.close();
			}
			if (stm != null) {
				stm.close();
			}
		} catch (SQLException ex) {
			Log.SQL.error("Error closing database connection", ex);
		}
	}

	public static Connection getConnection(String host, int port, String user, String password) throws SQLException {
		return getConnection(host, port, user, password, MANAGEMENT_DATABASE);
	}

	public static Connection getConnection(String host, int port, String user, String password, final String database)
			throws SQLException {
		final PGSimpleDataSource ds = new PGSimpleDataSource();
		ds.setServerName(host);
		ds.setPortNumber(port);
		ds.setDatabaseName(database);
		ds.setUser(user);
		ds.setPassword(password);
		return ds.getConnection();
	}

	// TODO: Move it to the driver implementation
	public static String getDriverVersion() {
		try {
			// Needs to read it from the current classpath, thus we can't use
			// the field reference directly!
			final int major = DRIVER_CLASS.getField("MAJORVERSION").getInt(null);
			final int minor = DRIVER_CLASS.getField("MINORVERSION").getInt(null);
			final int build = org.postgresql.util.PSQLDriverVersion.class.getField("buildNumber").getInt(null);
			return String.format("%d.%d-%d", major, minor, build);
		} catch (Exception e) {
			return "undefined";
		}
	}

	private static String postgisVersion;

	public static String getPostGISVersion() {
		if (postgisVersion == null) {
			synchronized (syncObject) {
				if (postgisVersion == null) {
					postgisVersion = fetchPostGISVersion();
				}
			}
		}

		return postgisVersion;
	}

	public static String fetchPostGISVersion() {
		try {
			Connection c = getConnection();
			Statement s = c.createStatement();
			ResultSet r = s.executeQuery("select postgis_lib_version()");
			if (r.next()) {
				final String postgisVersion = r.getString(1);
				Log.SQL.info("PostGIS version is " + postgisVersion);
				return postgisVersion;
			}
		} catch (SQLException ex) {
			Log.SQL.error("PostGIS is not installed", ex);
		}

		return null;
	}

	public static boolean isPostGISConfigured() {
		return getPostGISVersion() != null;
	}

	/*
	 * This is used in the transition between the old and the new DAO
	 */
	public DataSource getDataSource() {
		return this.datasource;
	}
}

class LazyConfDataSource extends AbstractDataSource {

	private final BasicDataSource ds;
	private Boolean configured = new Boolean(false);

	LazyConfDataSource(final BasicDataSource ds) {
		this.ds = ds;
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (!configured.booleanValue()) {
			synchronized (configured) {
				if (!configured.booleanValue()) {
					configureDatasource();
				}
			}
		}
		return ds.getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return ds.getConnection(username, password);
	}

	private DataSource configureDatasource() {
		DatabaseProperties dp = DatabaseProperties.getInstance();
		if (!dp.isConfigured()) {
			throw new IllegalStateException("Database connection not configured");
		}
		ds.setUrl(dp.getDatabaseUrl());
		ds.setUsername(dp.getDatabaseUser());
		ds.setPassword(dp.getDatabasePassword());
		return ds;
	}

}
