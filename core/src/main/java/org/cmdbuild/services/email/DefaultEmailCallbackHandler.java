package org.cmdbuild.services.email;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

public class DefaultEmailCallbackHandler implements EmailCallbackHandler {

	public static DefaultEmailCallbackHandler of(final Rule rule) {
		return of(new Rule[] { rule });
	}

	public static DefaultEmailCallbackHandler of(final Rule... rules) {
		return of(Arrays.asList(rules));
	}

	public static DefaultEmailCallbackHandler of(final Iterable<? extends Rule> rules) {
		return new DefaultEmailCallbackHandler(rules);
	}

	private Iterable<? extends Rule> rules;
	private final List<RuleAction> actions = Lists.newArrayList();

	public DefaultEmailCallbackHandler() {
		this.rules = Lists.newArrayList();
	}

	public DefaultEmailCallbackHandler(final Iterable<? extends Rule> rules) {
		this.rules = rules;
	}

	@Override
	public Iterable<? extends Rule> getRules() {
		return rules;
	}

	public void setRules(final Iterable<? extends Rule> rules) {
		this.rules = rules;
	}

	@Override
	public void notify(final RuleAction action) {
		this.actions.add(action);
	}

	public Iterable<? extends RuleAction> getActions() {
		return actions;
	}

}
