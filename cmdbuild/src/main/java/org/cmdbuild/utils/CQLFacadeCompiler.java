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
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;

public class CQLFacadeCompiler {

	public static QueryImpl compileWithTemplateParams(final String cqlQueryTemplate) throws Exception {
		final String compilableCqlQuery = substituteCqlVariableNames(cqlQueryTemplate);
		return compile(compilableCqlQuery);
	}

	/*
	 * {ns:varname} is not parsable, so we need to substitute them with fake
	 * ones to parse the CQL query string
	 */
	private static String substituteCqlVariableNames(final String cqlQuery) {
		final Pattern r = Pattern.compile("\\{[^\\{\\}]+\\}");
		final Matcher m = r.matcher(cqlQuery);
		return m.replaceAll("{fake}");
	}

	private static QueryImpl compile(final String query) throws Exception {
		final CQLCompiler comp = new CQLCompiler();
		final CQLCompilerListener l = new CQLCompilerListener();
		l.setFactory(new FactoryImpl());
		FactoryImpl.CmdbuildCheck = true;

		comp.compile(query, l);

		final QueryImpl compiled = (QueryImpl) l.getRootQuery();
		compiled.check();
		return compiled;
	}

	private static CardQuery naiveCmbuildCompile(final String query, final int limit, final int offset,
			final Map<String, Object> context, final UserContext userCtx) {
		try {
			final CQLCompiler comp = new CQLCompiler();
			final CQLCompilerListener l = new CQLCompilerListener();
			l.setFactory(new FactoryImpl());
			FactoryImpl.CmdbuildCheck = true;

			comp.compile(query, l);

			final QueryImpl compiled = (QueryImpl) l.getRootQuery();
			compiled.check();

			if (limit > 0) {
				compiled.setLimit(limit);
			}
			if (offset > 0) {
				compiled.setOffset(offset);
			}

			final NaiveCmdbuildSQLBuilder sqlbuilder = new NaiveCmdbuildSQLBuilder();
			return sqlbuilder.build(compiled, context, userCtx);
		} catch (final Exception e) {
			Log.OTHER.error("CQL compilation failed", e);
			throw WorkflowExceptionType.CQL_COMPILATION_FAILED.createException();
		}
	}

	public static CardQuery naiveCmbuildCompileSystemUser(final String query, final Map<String, Object> context) {
		return naiveCmbuildCompile(query, 0, 0, context, UserContext.systemContext());
	}

}
