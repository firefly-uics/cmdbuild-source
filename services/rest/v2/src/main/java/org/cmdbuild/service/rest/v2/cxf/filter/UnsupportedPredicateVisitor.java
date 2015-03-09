package org.cmdbuild.service.rest.v2.cxf.filter;

import static java.lang.String.format;

import org.cmdbuild.logic.data.access.filter.model.And;
import org.cmdbuild.logic.data.access.filter.model.Contains;
import org.cmdbuild.logic.data.access.filter.model.EndsWith;
import org.cmdbuild.logic.data.access.filter.model.EqualTo;
import org.cmdbuild.logic.data.access.filter.model.GreaterThan;
import org.cmdbuild.logic.data.access.filter.model.In;
import org.cmdbuild.logic.data.access.filter.model.IsNull;
import org.cmdbuild.logic.data.access.filter.model.LessThan;
import org.cmdbuild.logic.data.access.filter.model.Like;
import org.cmdbuild.logic.data.access.filter.model.Not;
import org.cmdbuild.logic.data.access.filter.model.Or;
import org.cmdbuild.logic.data.access.filter.model.PredicateVisitor;
import org.cmdbuild.logic.data.access.filter.model.StartsWith;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;

public class UnsupportedPredicateVisitor implements PredicateVisitor, LoggingSupport {

	@Override
	public void visit(final And predicate) {
		logger.warn(format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final Contains predicate) {
		logger.warn(format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final EndsWith predicate) {
		logger.warn(format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final EqualTo predicate) {
		logger.warn(format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final GreaterThan predicate) {
		logger.warn(format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final In predicate) {
		logger.warn(format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final IsNull predicate) {
		logger.warn(format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final LessThan predicate) {
		logger.warn(format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final Like predicate) {
		logger.warn(format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final Not predicate) {
		logger.warn(format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final Or predicate) {
		logger.warn(format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final StartsWith predicate) {
		logger.warn(format("predicate '%s' not supported", predicate));
	}

}
