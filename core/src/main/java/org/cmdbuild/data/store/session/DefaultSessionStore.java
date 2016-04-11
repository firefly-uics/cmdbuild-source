package org.cmdbuild.data.store.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.data.store.ForwardingStore;
import org.cmdbuild.data.store.Store;

public class DefaultSessionStore extends ForwardingStore<Session> implements SessionStore {

	private final Store<Session> delegate;
	private final Map<Session, OperationUser> map;

	public DefaultSessionStore(final Store<Session> delegate) {
		this.delegate = delegate;
		this.map = new ConcurrentHashMap<>();
	}

	@Override
	protected Store<Session> delegate() {
		return delegate;
	}

	@Override
	public void set(final Session session, final OperationUser user) {
		map.put(session, user);
	}

}
