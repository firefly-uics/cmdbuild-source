package org.cmdbuild.servlets.resource.shark;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.StoredProcedureException;
import org.cmdbuild.exception.StoredProcedureException.StoredProcedureExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.DBService;
import org.cmdbuild.utils.StringUtils;
import org.cmdbuild.utils.StringUtils.Stringyfier;

public class StoredProcedureFacade {

	static final Stringyfier<Object> questionMarkStringyfier = new Stringyfier<Object>(){
		public String stringify(Object obj) {
			return "?";
		}
	};
	
	@SuppressWarnings("unchecked")
	public static List<List<Object>> callStoredProcedureRS(String name, Object[] inArgs, Class[] outArgTypes)
			throws AuthException, StoredProcedureException {
		Connection conn = null;
		CallableStatement stmt = null;
		ResultSet rs = null;
		
		String toCall = "{ ? = call " + name + "(";
		toCall += StringUtils.join(inArgs, ",", questionMarkStringyfier);
		toCall += ") }";
		
		try{
			List<List<Object>> out = new ArrayList();
			conn = DBService.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.prepareCall(toCall);
			
			stmt.registerOutParameter(1, Types.OTHER);
			int idx = 2;
			
			for(Object obj : inArgs){
				CallableStmtArg.setObj(stmt, idx, obj);
				idx++;
			}
			
			stmt.execute();
			
			rs = (ResultSet) stmt.getObject(1);
			while(rs.next()){
				List<Object> tmp = new ArrayList();
				//parse row
				idx = 1;
				for(Class cls : outArgTypes){
					tmp.add( CallableStmtArg.get(rs, idx, cls) );
					idx++;
				}
				//add to out list
				out.add(tmp);
			}
			
			conn.commit();
			
			return out;
		} catch (Exception e) {
			Log.WORKFLOW.error("Original StoredProcedure Exception", e);
			throw StoredProcedureExceptionType.STOREDPROCEDURE_CANNOT_EXECUTE.createException(name);
		} finally {
			DBService.close(rs, stmt);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<List<Object>> callStoredProcedure( String name, Object[] inArgs, Class[] outArgTypes ) 
	throws AuthException, StoredProcedureException{
		Connection conn = null;
		CallableStatement stmt = null;
		ResultSet rs = null;
		
		String toCall = "{";
		toCall += StringUtils.join(outArgTypes, ",", questionMarkStringyfier);
		toCall += " = call " + name + "(";
		toCall += StringUtils.join(inArgs, ",", questionMarkStringyfier);
		toCall += ") }";
		try{
			List<List<Object>> out = new ArrayList();
			conn = DBService.getConnection();
			stmt = conn.prepareCall(toCall);
			
			int idx = 1;
			for(Class cls : outArgTypes) {
				CallableStmtArg.setOut(stmt, idx, cls);
				idx++;
			}
			
			for(Object obj : inArgs) {
				CallableStmtArg.setObj(stmt, idx, obj);
				idx++;
			}
			
			if(!stmt.execute()) {
				List tmp = new ArrayList();
				idx = 1;
				for(Class cls : outArgTypes){
					Object obj = stmt.getObject(idx);
					if(obj == null && cls.equals(String.class)) {
						obj = "";
					}
					tmp.add(obj);
					idx ++;
				}
				out.add(tmp);
			} else {
				rs = stmt.getResultSet();
				while(rs.next()){
					List<Object> tmp = new ArrayList();
					//parse row
					idx = 1;
					for(Class cls : outArgTypes){
						tmp.add( CallableStmtArg.get(rs, idx, cls) );
						idx++;
					}
					//add to out list
					out.add(tmp);
				}
			}
			
			return out;
		} catch (Exception exc) {
			Log.WORKFLOW.error("Original StoredProcedure Exception", exc);
			throw StoredProcedureExceptionType.STOREDPROCEDURE_CANNOT_EXECUTE.createException(name);
		} finally {
			DBService.close(rs, stmt);
		}
	}

	@SuppressWarnings("unchecked")
	public enum CallableStmtArg {
		BOOLEAN(Boolean.class,Types.BOOLEAN){
			@Override
			protected void set(CallableStatement stmt, int index, Object obj) throws SQLException {
				stmt.setBoolean(index, (Boolean)obj);
			}
			@Override
			protected Object get(ResultSet rs, int index) throws SQLException {
				return rs.getBoolean(index);
			}
		},
		INT(Integer.class,Types.INTEGER){
			@Override
			public void set(CallableStatement stmt, int index, Object obj) throws SQLException {
				stmt.setInt(index, (Integer)obj);
			}
			@Override
			protected Object get(ResultSet rs, int index) throws SQLException {
				return rs.getInt(index);
			}
		},
		LONG(Long.class,Types.BIGINT){
			@Override
			protected void set(CallableStatement stmt, int index, Object obj) throws SQLException {
				stmt.setLong(index, (Long)obj);
			}
			@Override
			protected Object get(ResultSet rs, int index) throws SQLException {
				return rs.getLong(index);
			}
		},
		DOUBLE(Double.class,Types.DOUBLE){
			@Override
			public void set(CallableStatement stmt, int index, Object obj) throws SQLException {
				stmt.setDouble(index, (Double)obj);
			}
			@Override
			protected Object get(ResultSet rs, int index) throws SQLException {
				return rs.getDouble(index);
			}
		},
		FLOAT(Float.class,Types.FLOAT){
			@Override
			public void set(CallableStatement stmt, int index, Object obj) throws SQLException {
				stmt.setFloat(index, (Float)obj);
			}
			@Override
			protected Object get(ResultSet rs, int index) throws SQLException {
				return rs.getFloat(index);
			}
		},
		STRING(String.class,Types.VARCHAR){
			@Override
			public void set(CallableStatement stmt, int index, Object obj) throws SQLException {
				stmt.setString(index, (String)obj);
			}
			@Override
			protected Object get(ResultSet rs, int index) throws SQLException {
				String out = rs.getString(index);
				if(out == null){ out = "";}
				return out;
			}
		},
		DATE(Date.class,Types.DATE){
			@Override
			public void set(CallableStatement stmt, int index, Object obj) throws SQLException {
				stmt.setDate(index, (Date)obj);
			}
			@Override
			protected Object get(ResultSet rs, int index) throws SQLException {
				return rs.getDate(index);
			}
		},
		UDATE(java.util.Date.class,Types.DATE){
			@Override
			protected void set(CallableStatement stmt, int index, Object obj) throws SQLException {
				Date dt = new Date( ((java.util.Date)obj).getTime() );
				stmt.setDate(index, dt);
			}
			@Override
			protected Object get(ResultSet rs, int index) throws SQLException {
				Date dt = rs.getDate(index);
				if (dt!=null) return new java.util.Date(dt.getTime());
				else return null;
			}
		},
		TIMESTAMP(Timestamp.class,Types.TIMESTAMP){
			@Override
			public void set(CallableStatement stmt, int index, Object obj) throws SQLException {
				stmt.setTimestamp(index, (Timestamp)obj);
			}
			@Override
			protected Object get(ResultSet rs, int index) throws SQLException {
				return rs.getTimestamp(index);
			}
		},
		TIME(Time.class,Types.TIME){
			@Override
			public void set(CallableStatement stmt, int index, Object obj) throws SQLException {
				stmt.setTime(index, (Time)obj);
			}
			@Override
			protected Object get(ResultSet rs, int index) throws SQLException {
				return rs.getTime(index);
			}
		};
		
		private Class cls;
		private int type;
		
		private CallableStmtArg(Class cls,int type){
			this.cls = cls;
			this.type = type;
		}
		
		protected boolean isClass(Class cls){
			return this.cls.equals(cls);
		}
		
		protected void registerOutParm(CallableStatement stmt, int index) throws SQLException{
			stmt.registerOutParameter(index, this.type);
		}
		protected abstract void set(CallableStatement stmt, int index, Object obj) throws SQLException;
		
		protected abstract Object get(ResultSet rs,int index) throws SQLException;
		protected Object get(CallableStatement stmt, int index) throws SQLException{
			return stmt.getObject(index);
		}
		
		/**
		 * Register an out parameter to be of type cls
		 * @param stmt
		 * @param index
		 * @param cls
		 * @throws SQLException
		 */
		public static void setOut(CallableStatement stmt, int index, Class cls) throws SQLException{
			for(CallableStmtArg csa : CallableStmtArg.values()){
				if(csa.isClass(cls)){
					csa.registerOutParm(stmt, index);
					return;
				}
			}
			throw new RuntimeException("Unknown class type to register : " + cls);
		}
		
		/**
		 * Set an input parameter to be obj
		 * @param stmt
		 * @param index
		 * @param obj
		 * @throws SQLException
		 */
		public static void setObj(CallableStatement stmt, int index, Object obj) throws SQLException{
			for(CallableStmtArg csa : CallableStmtArg.values()){
				if(csa.isClass(obj.getClass())){
					csa.set(stmt, index, obj);
					return;
				}
			}
			throw new RuntimeException("Unknown class type to set : " + obj.getClass());
		}
		public static Object get(ResultSet rs, int idx, Class cls) throws SQLException{
			for(CallableStmtArg csa : CallableStmtArg.values()){
				if(csa.isClass(cls)){
					return csa.get(rs, idx);
				}
			}
			throw new RuntimeException("Unknown class type for retrieval : " + cls);
		}
	}
}
