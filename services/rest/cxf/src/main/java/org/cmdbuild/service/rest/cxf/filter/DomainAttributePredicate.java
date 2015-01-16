package org.cmdbuild.service.rest.cxf.filter;

import static java.lang.String.format;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.equalTo;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_DESTINATION;
import static org.cmdbuild.service.rest.constants.Serialization.CLASS_SOURCE;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.access.filter.model.And;
import org.cmdbuild.logic.data.access.filter.model.Attribute;
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
import org.cmdbuild.service.rest.logging.LoggingSupport;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;

public class DomainAttributePredicate implements Predicate<CMDomain>, PredicateVisitor, LoggingSupport {

	private static final Marker marker = MarkerFactory.getMarker(DomainAttributePredicate.class.getName());

	private final Attribute attribute;
	private CMDomain input;
	private boolean output;

	public DomainAttributePredicate(final Attribute attribute) {
		this.attribute = attribute;
	}

	@Override
	public boolean apply(final CMDomain input) {
		this.input = input;
		this.output = false;
		this.attribute.getPredicate().accept(this);
		return output;
	}

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
		final boolean _output;
		if (CLASS_SOURCE.equals(attribute.getName())) {
			_output = equalTo(input.getClass1().getName()).equals(predicate);
		} else if (CLASS_DESTINATION.equals(attribute.getName())) {
			_output = equalTo(input.getClass2().getName()).equals(predicate);
		} else {
			_output = true;
		}
		output = _output;
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
