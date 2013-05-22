package org.cmdbuild.cql.sqlbuilder;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdbuild.cql.compiler.CQLCompiler;
import org.cmdbuild.cql.compiler.CQLCompilerListener;
import org.cmdbuild.cql.compiler.impl.FactoryImpl;
import org.cmdbuild.cql.compiler.impl.QueryImpl;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class CQLFacadeCompiler {

	private static final Logger logger = Log.CMDBUILD;
	private static final Marker marker = MarkerFactory.getMarker(CQLFacadeCompiler.class.getName());

	public static QueryImpl compileWithTemplateParams(final String cqlQueryTemplate) throws Exception {
		final String compilableCqlQuery = substituteCqlVariableNames(cqlQueryTemplate);
		return compileAndCheck(compilableCqlQuery);
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

	public static QuerySpecsBuilder compileAndFill(final String query, final Map<String, Object> context, final QuerySpecsBuilder querySpecsBuilder) {
		try {
			final QueryImpl compiled = compileAndCheck(query);
			return NaiveCmdbuildSQLBuilder.build(compiled, context, querySpecsBuilder);
		} catch (final Exception e) {
			logger.error(marker, "CQL compilation failed", e);
			throw WorkflowExceptionType.CQL_COMPILATION_FAILED.createException();
		}
	}

	private static QueryImpl compileAndCheck(final String query) throws Exception {
		final CQLCompiler compiler = new CQLCompiler();
		final CQLCompilerListener listener = new CQLCompilerListener();
		listener.setFactory(new FactoryImpl());
		FactoryImpl.CmdbuildCheck = true;

		compiler.compile(query, listener);

		final QueryImpl compiled = (QueryImpl) listener.getRootQuery();
		compiled.check();
		return compiled;
	}

}
