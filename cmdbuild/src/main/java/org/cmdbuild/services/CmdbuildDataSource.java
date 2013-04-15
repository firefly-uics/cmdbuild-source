package org.cmdbuild.services;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.lang.Validate;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.cmdbuild.config.DatabaseProperties;

public class CmdbuildDataSource implements DataSource {

	private static final String DATASOURCE_NAME = "jdbc/cmdbuild";
	private static final Class<?> DRIVER_CLASS = org.postgresql.Driver.class;

	public static CmdbuildDataSource newInstance() {
		BasicDataSource ds;
		try {
			final InitialContext ictx = new InitialContext();
			final Context ctx = (Context) ictx.lookup("java:/comp/env");
			ds = (BasicDataSource) ctx.lookup(DATASOURCE_NAME);
		} catch (final NamingException e) {
			ds = new BasicDataSource();
		}
		ds.setDriverClassName(DRIVER_CLASS.getCanonicalName());
		return new CmdbuildDataSource(ds);
	}

	private final BasicDataSource ds;
	private final Boolean configured = new Boolean(false);

	private CmdbuildDataSource(final BasicDataSource ds) {
		this.ds = ds;
	}

	private DataSource configureDatasource() {
		final DatabaseProperties dp = DatabaseProperties.getInstance();
		if (!dp.isConfigured()) {
			throw new IllegalStateException("Database connection not configured");
		}
		ds.setUrl(dp.getDatabaseUrl());
		ds.setUsername(dp.getDatabaseUser());
		ds.setPassword(dp.getDatabasePassword());
		return ds;
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
	public Connection getConnection(final String username, final String password) throws SQLException {
		return ds.getConnection(username, password);
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return ds.getLogWriter();
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return ds.getLoginTimeout();
	}

	@Override
	public void setLogWriter(final PrintWriter out) throws SQLException {
		ds.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(final int seconds) throws SQLException {
		ds.setLoginTimeout(seconds);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		Validate.notNull(iface, "Interface argument must not be null");
		if (!DataSource.class.equals(iface)) {
			throw new SQLException("DataSource of type [" + getClass().getName()
					+ "] can only be unwrapped as [javax.sql.DataSource], not as [" + iface.getName());
		}
		return (T) this;
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		return DataSource.class.equals(iface);
	}

}
