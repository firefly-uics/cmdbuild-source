package org.cmdbuild.logic.filter;

import java.util.Optional;

import org.cmdbuild.common.utils.PagedElements;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingFilterLogic extends ForwardingObject implements FilterLogic {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingFilterLogic() {
	}

	@Override
	protected abstract FilterLogic delegate();

	@Override
	public Filter create(final Filter filter) {
		return delegate().create(filter);
	}

	@Override
	public Optional<Filter> read(final Filter filter) {
		return delegate().read(filter);
	}

	@Override
	public void update(final Filter filter) {
		delegate().update(filter);
	}

	@Override
	public void delete(final Filter filter) {
		delegate().delete(filter);
	}

	@Override
	public Iterable<Filter> readForCurrentUser(final String className) {
		return delegate().readForCurrentUser(className);
	}

	@Override
	public PagedElements<Filter> readShared(final String className, final int start, final int limit) {
		return delegate().readShared(className, start, limit);
	}

	@Override
	public PagedElements<Filter> readNotShared(final String className, final int start, final int limit) {
		return delegate().readNotShared(className, start, limit);
	}

	@Override
	public Iterable<Filter> getDefaults(final String className, final String groupName) {
		return delegate().getDefaults(className, groupName);
	}

	@Override
	public void setDefault(final Iterable<Long> filters, final Iterable<String> groups) {
		delegate().setDefault(filters, groups);
	}

	@Override
	public Iterable<String> getGroups(final Long filter) {
		return delegate().getGroups(filter);
	}

}
