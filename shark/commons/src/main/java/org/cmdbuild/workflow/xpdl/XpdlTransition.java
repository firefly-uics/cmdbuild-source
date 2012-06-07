package org.cmdbuild.workflow.xpdl;

import org.apache.commons.lang.Validate;
import org.enhydra.jxpdl.elements.Transition;

public class XpdlTransition {

	@SuppressWarnings("unused")
	private final Transition inner;

	XpdlTransition(final Transition transition) {
		Validate.notNull(transition);
		inner = transition;
	}

}
