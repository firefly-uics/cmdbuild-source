package org.cmdbuild.cql.compiler;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.cmdbuild.cql.CQLBuilderListener;
import org.cmdbuild.cql.CQLCompilerBuilder;
import org.cmdbuild.cql.CQLLexer;
import org.cmdbuild.cql.CQLParser;

public class CQLCompiler {
	
	public CQLCompiler() {
	}

	public void init(){}
	
	public void compile(String text, CQLBuilderListener listener) throws RecognitionException {
		ANTLRStringStream input = new ANTLRStringStream( text );
	    CQLLexer lexer = new CQLLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CQLParser parser = new CQLParser(tokens);
		CQLParser.expr_return r = parser.expr();
		CommonTree t = (CommonTree) r.getTree();
		CQLCompilerBuilder builder = new CQLCompilerBuilder();
		builder.setCQLBuilderListener(listener);
		builder.compile(t);
	}
}
