package org.cmdbuild.dao.view.user;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public class UserCard implements CMCard {

	private final CMCard inner;
	private UserClass userClass;
	private final Map<String, Object> allValues;
	private final Map<String, Object> values;

	static UserCard newInstance(final UserDataView view, final CMCard inner) {
		return new UserCard(view, inner);
	}

	private UserCard(final UserDataView view, final CMCard inner) {
		this.inner = inner;
		this.userClass = UserClass.newInstance(view, inner.getType());
		this.allValues = Maps.newHashMap();
		this.values = Maps.newHashMap();
		for (final Entry<String, Object> entry : inner.getAllValues()) {
			final String name = entry.getKey();
			final CMAttribute attribute = userClass.getAttribute(name);
			if (attribute == null) {
				continue;
			}
			final Object value = entry.getValue();
			if (attribute.isSystem()) {
				allValues.put(name, value);
			}
			values.put(name, value);
		}
	}

	@Override
	public Long getId() {
		return inner.getId();
	}

	@Override
	public String getUser() {
		return inner.getUser();
	}

	@Override
	public DateTime getBeginDate() {
		return inner.getBeginDate();
	}

	@Override
	public DateTime getEndDate() {
		return inner.getEndDate();
	}

	@Override
	public Iterable<Entry<String, Object>> getAllValues() {
		return allValues.entrySet();
	}

	@Override
	public Object get(String key) {
		return allValues.get(key);
	}

	@Override
	public <T> T get(String key, Class<? extends T> requiredType) {
		return requiredType.cast(get(key));
	}

	@Override
	public Iterable<Entry<String, Object>> getValues() {
		return values.entrySet();
	}

	@Override
	public UserClass getType() {
		return userClass;
	}

	@Override
	public Object getCode() {
		return inner.getCode();
	}

	@Override
	public Object getDescription() {
		return inner.getDescription();
	}

}
