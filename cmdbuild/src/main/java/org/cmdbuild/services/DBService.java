package org.cmdbuild.services;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import net.jcip.annotations.GuardedBy;

import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.postgresql.ds.PGSimpleDataSource;

public class DBService {

	private static final Class<?> DRIVER_CLASS = org.postgresql.Driver.class;
	private static final String MANAGEMENT_DATABASE = "postgres";

	protected final DataSource datasource;
	private final ThreadLocal<Connection> connection = new ThreadLocal<Connection>();

	@GuardedBy("syncObject")
	private static volatile DBService instance;
	private static final Object syncObject = new Object();

	private DBService() {
		datasource = CmdbuildDataSource.newInstance();
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
		try {
			return getInstance().datasource.getConnection();
		} catch (final SQLException e) {
			Log.PERSISTENCE.error("Error trying to get database connection", e);
			throw ORMExceptionType.ORM_DATABASE_CONNECTION_ERROR.createException();
		} catch (final IllegalStateException e) {
			Log.PERSISTENCE.error("Error trying to get database connection", e);
			throw ORMExceptionType.ORM_DBNOTCONFIGURED.createException();
		}
	}

	public static void releaseConnection() {
		if (instance == null) {
			return;
		}
		final Connection con = instance.connection.get();
		if (con != null) {
			try {
				con.close();
			} catch (final SQLException ex) {
				Log.SQL.error("Error closing database connection", ex);
			}
		}
		instance.connection.remove();
	}

	public static void close(final ResultSet res, final Statement stm, final Connection con) {
		try {
			if (res != null) {
				res.close();
			}
			if (stm != null) {
				stm.close();
			}
			if (con != null) {
				con.close();
			}
		} catch (final SQLException ex) {
			Log.SQL.error("Error closing database connection", ex);
		}
	}

	public static Connection getConnection(final String host, final int port, final String user, final String password)
			throws SQLException {
		return getConnection(host, port, user, password, MANAGEMENT_DATABASE);
	}

	public static Connection getConnection(final String host, final int port, final String user, final String password,
			final String database) throws SQLException {
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
		} catch (final Exception e) {
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
			final Connection c = getConnection();
			final Statement s = c.createStatement();
			final ResultSet r = s.executeQuery("select postgis_lib_version()");
			if (r.next()) {
				final String postgisVersion = r.getString(1);
				Log.SQL.info("PostGIS version is " + postgisVersion);
				return postgisVersion;
			}
		} catch (final SQLException ex) {
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
