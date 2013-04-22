package org.cmdbuild.elements.wrappers;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.CardForwarder;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.services.auth.PrivilegeManager.PrivilegeType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

/*
 * TODO: Read only privileges on superclasses can't be implemented
 * with the current attribute implementation, so we trap only the
 * call (setMode) and the default constructor used by the AJAX
 * interface.
 */

public class PrivilegeCard extends CardForwarder {
	protected static final long serialVersionUID = 1L;

	public static final String GRANT_CLASS_NAME = "Grant";
	public static final String ROLE_ID_ATTRIBUTE = "IdRole";
	public static final String GRANTED_CLASS_ID_ATTRIBUTE = "IdGrantedClass";
	private static final ITable grantClass = UserOperations.from(UserContext.systemContext()).tables()
			.get(GRANT_CLASS_NAME);

	ITable grantTargetClass;

	

	/*
	 * WHAT IS THIS? Should be moved into PrivilegeType!
	 */
	public static PrivilegeType getPrivilegeTypeOf(final String type) {
		final PrivilegeType[] types = PrivilegeType.values();
		try {
			for (final PrivilegeType t : types) {
				if (t.getGrantType().equals(type)) {
					return PrivilegeType.valueOf(t.toString());
				}
			}
			return PrivilegeType.NONE;
		} catch (final IllegalArgumentException e) {
			return PrivilegeType.NONE;
		}
	}

	public PrivilegeCard() throws NotFoundException {
		super(grantClass.cards().create());
	}

	public PrivilegeCard(final ICard card) throws NotFoundException {
		super(card);
	}

	public PrivilegeCard(final Integer groupId, final Integer grantedClassId, final PrivilegeType mode)
			throws NotFoundException {
		super(grantClass.cards().create());
		this.setGroupId(groupId);
		this.setGrantedClass(grantedClassId);
		this.setMode(mode);
	}

	public ITable getGrantedClass() {
		return UserOperations.from(UserContext.systemContext()).tables().get(getGrantedClassId());
	}

	@Override
	public ITable getSchema() {
		return UserOperations.from(UserContext.systemContext()).tables().get(GRANT_CLASS_NAME);
	}

	public PrivilegeType getMode() {
		return getPrivilegeTypeOf(getAttributeValue("Mode").getString());
	}

	public void setMode(final PrivilegeType mode) {
		getAttributeValue("Mode").setValue(mode.getGrantType());
	}

	public Integer getGrantedClassId() {
		return getAttributeValue(GRANTED_CLASS_ID_ATTRIBUTE).getInt();
	}

	public void setGrantedClass(final Integer id) {
		getAttributeValue(GRANTED_CLASS_ID_ATTRIBUTE).setValue(id);
	}

	public int getGroupId() {
		return getAttributeValue(ROLE_ID_ATTRIBUTE).getInt();
	}

	public void setGroupId(final Integer id) {
		getAttributeValue(ROLE_ID_ATTRIBUTE).setValue(id);
	}

	@Override
	public String toString() {
		return String.format("%s %s", getGrantedClass().getDescription(), getMode().toString());
	}

	static public Iterable<PrivilegeCard> forGroup(final Integer groupId) {
		final Map<Integer, PrivilegeCard> privMap = new HashMap<Integer, PrivilegeCard>();
		for (final ICard card : grantClass.cards().list()
				.filter(ROLE_ID_ATTRIBUTE, AttributeFilterType.EQUALS, String.valueOf(groupId))) {
			final PrivilegeCard priv = new PrivilegeCard(card);
			privMap.put(priv.getGrantedClassId(), priv);
		}
		for (final ITable grantedClass : UserOperations.from(UserContext.systemContext()).tables().list()) {
			if (grantedClass.getMode().isCustom() && !privMap.containsKey(grantedClass.getId())) {
				final PrivilegeCard priv = new PrivilegeCard(groupId, grantedClass.getId(), PrivilegeType.NONE);
				privMap.put(priv.getGrantedClassId(), priv);
			}
		}
		return privMap.values();
	}

	public static PrivilegeCard get(final Integer groupId, final Integer classId) {
		PrivilegeCard priv;
		try {
			final ICard privCard = grantClass.cards().list()
					.filter(ROLE_ID_ATTRIBUTE, AttributeFilterType.EQUALS, String.valueOf(groupId))
					.filter(GRANTED_CLASS_ID_ATTRIBUTE, AttributeFilterType.EQUALS, String.valueOf(classId)).get();
			priv = new PrivilegeCard(privCard);
		} catch (final NotFoundException e) {
			priv = new PrivilegeCard(groupId, classId, PrivilegeType.NONE);
		}
		return priv;
	}
}
