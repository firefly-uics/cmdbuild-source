package org.cmdbuild.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdbuild.cql.compiler.CQLCompiler;
import org.cmdbuild.cql.compiler.CQLCompilerListener;
import org.cmdbuild.cql.compiler.impl.FactoryImpl;
import org.cmdbuild.cql.compiler.impl.QueryImpl;
import org.cmdbuild.cql.sqlbuilder.NaiveCmdbuildSQLBuilder;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;

public class CQLFacadeCompiler {

	public static QueryImpl compileWithTemplateParams(String cqlQueryTemplate) throws Exception {
		String compilableCqlQuery = substituteCqlVariableNames(cqlQueryTemplate);
		return compile(compilableCqlQuery);
	}

	/*
	 * {ns:varname} is not parsable, so we need to substitute
	 * them with fake ones to parse the CQL query string 
	 */
	private static String substituteCqlVariableNames(String cqlQuery) {
		Pattern r = Pattern.compile("\\{[^\\{\\}]+\\}");
	    Matcher m = r.matcher(cqlQuery);
	    return m.replaceAll("{fake}");
	}

	public static QueryImpl compile(String query) throws Exception {
		CQLCompiler comp = new CQLCompiler();
		CQLCompilerListener l = new CQLCompilerListener();
		l.setFactory(new FactoryImpl());
		FactoryImpl.CmdbuildCheck = true;
		
		comp.compile(query, l);
		
		QueryImpl compiled = (QueryImpl)l.getRootQuery();
		compiled.check();
		return compiled;
	}

	public static void naiveCmbuildCompile(
			CardQuery mngr,
			String query,
			int limit, int offset,
			Map<String,Object> context,
			UserContext userCtx) {
		try {
			CQLCompiler comp = new CQLCompiler();
			CQLCompilerListener l = new CQLCompilerListener();
			l.setFactory(new FactoryImpl());
			FactoryImpl.CmdbuildCheck = true;
			
			comp.compile(query, l);
			
			QueryImpl compiled = (QueryImpl)l.getRootQuery();
			compiled.check();
			
			if(limit > 0) {
				compiled.setLimit(limit);
			}
			if(offset > 0) {
				compiled.setOffset(offset);
			}
			
			NaiveCmdbuildSQLBuilder sqlbuilder = new NaiveCmdbuildSQLBuilder();
			sqlbuilder.build(compiled, context, mngr, userCtx);
		} catch (Exception e) {
			throw ORMExceptionType.ORM_CQL_COMPILATION_FAILED.createException();
		}
	}

	public static CardQuery naiveCmbuildCompile(
			String query,
			int limit, int offset,
			Map<String,Object> context,
			UserContext userCtx) {
		try {
			CQLCompiler comp = new CQLCompiler();
			CQLCompilerListener l = new CQLCompilerListener();
			l.setFactory(new FactoryImpl());
			FactoryImpl.CmdbuildCheck = true;
			
			comp.compile(query, l);
			
			QueryImpl compiled = (QueryImpl)l.getRootQuery();
			compiled.check();
			
			if(limit > 0) {
				compiled.setLimit(limit);
			}
			if(offset > 0) {
				compiled.setOffset(offset);
			}
			
			NaiveCmdbuildSQLBuilder sqlbuilder = new NaiveCmdbuildSQLBuilder();
			return sqlbuilder.build(compiled, context, userCtx);
		} catch(Exception e) {
			Log.OTHER.error("CQL compilation failed", e);
			throw WorkflowExceptionType.CQL_COMPILATION_FAILED.createException();
		}
	}

	public static CardQuery naiveCmbuildCompileSystemUser(
			String query,
			Map<String,Object> context) {
		return naiveCmbuildCompile(query, 0, 0, context,
				UserContext.systemContext());
	}

	public static void naiveCmbuildCompileSystemUser(
			CardQuery mngr,
			String query,
			int limit, int offset,
			Map<String,Object> context) {
		naiveCmbuildCompile(mngr, query, limit, offset, context,
				UserContext.systemContext());
	}

	public static CardQuery naiveCmbuildCompile(
			String query,
			Map<String,Object> context,
			UserContext userCtx) {
		return naiveCmbuildCompile(query, 0, 0, context, userCtx);
	}
}
