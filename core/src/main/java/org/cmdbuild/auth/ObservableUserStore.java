package org.cmdbuild.auth;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.EventListener;

import org.cmdbuild.auth.user.OperationUser;

public class ObservableUserStore extends ForwardingUserStore {

	public static interface Observer extends EventListener {

		void settingUser(OperationUser value);

	}

	private final UserStore delegate;
	private final Collection<Observer> observers;

	public ObservableUserStore(final UserStore delegate) {
		this.delegate = delegate;
		this.observers = newArrayList();
	}

	@Override
	protected UserStore delegate() {
		return delegate;
	}

	public void add(final Observer observer) {
		observers.add(observer);
	}

	@Override
	public void setUser(final OperationUser user) {
		for (final Observer element : observers) {
			element.settingUser(user);
		}
		super.setUser(user);
	}

}
