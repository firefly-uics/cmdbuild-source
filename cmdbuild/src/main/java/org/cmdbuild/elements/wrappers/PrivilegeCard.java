package org.cmdbuild.elements.wrappers;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.CardForwarder;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.services.auth.UserContext;

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
	private static final ITable grantClass = UserContext.systemContext().tables().get(GRANT_CLASS_NAME);

	ITable grantTargetClass;

	public enum PrivilegeType {
		READ("r"), 
		WRITE("w"), 
		NONE("-"); 

		private String type;

		PrivilegeType(String type) {
			this.type = type;
		}

		public String getGrantType(){
			return this.type;
		}

		public static PrivilegeType intersection(PrivilegeType first, PrivilegeType second) {
			if (first == PrivilegeType.WRITE &&
					second == PrivilegeType.WRITE)
				return PrivilegeType.WRITE;
			if (first == PrivilegeType.NONE ||
					second == PrivilegeType.NONE)
				return PrivilegeType.NONE;
			return PrivilegeType.READ;
		}

		public static PrivilegeType union(PrivilegeType first, PrivilegeType second) {
			if (first == PrivilegeType.WRITE ||
					second == PrivilegeType.WRITE)
				return PrivilegeType.WRITE;
			if (first == PrivilegeType.NONE &&
					second == PrivilegeType.NONE)
				return PrivilegeType.NONE;
			return PrivilegeType.READ;
		}
	}

	/*
	 * WHAT IS THIS? Should be moved into PrivilegeType!
	 */
	public static PrivilegeType getPrivilegeTypeOf(String type) {
		PrivilegeType[] types = PrivilegeType.values();
		try{
			for(PrivilegeType t: types){
				if(t.getGrantType().equals(type)){
					return PrivilegeType.valueOf(t.toString());
				}
			}
			return PrivilegeType.NONE;
		} catch(IllegalArgumentException e){
			return PrivilegeType.NONE;
		}
	}

	public PrivilegeCard() throws NotFoundException {
		super(grantClass.cards().create());
	}

	public PrivilegeCard(ICard card) throws NotFoundException {
		super(card);
	}

	public PrivilegeCard(Integer groupId, Integer grantedClassId, PrivilegeType mode) throws NotFoundException {
		super(grantClass.cards().create());
		this.setGroupId(groupId);
		this.setGrantedClass(grantedClassId);
		this.setMode(mode);
	}

	public ITable getGrantedClass() {
		return UserContext.systemContext().tables().get(getGrantedClassId());
	}

	@Override
	public ITable getSchema() {
		return UserContext.systemContext().tables().get(GRANT_CLASS_NAME);
	}

	public PrivilegeType getMode() {
		return getPrivilegeTypeOf(getAttributeValue("Mode").getString());
	}

	public void setMode(PrivilegeType mode) {
		getAttributeValue("Mode").setValue(mode.getGrantType());
	}

	public Integer getGrantedClassId() {
		return getAttributeValue(GRANTED_CLASS_ID_ATTRIBUTE).getInt();
	}

	public void setGrantedClass(Integer id){
		getAttributeValue(GRANTED_CLASS_ID_ATTRIBUTE).setValue(id);
	}

	public int getGroupId(){
		return getAttributeValue(ROLE_ID_ATTRIBUTE).getInt();
	}

	public void setGroupId(Integer id){
		getAttributeValue(ROLE_ID_ATTRIBUTE).setValue(id);
	}

	@Override
	public String toString() {
		return String.format("%s %s", getGrantedClass().getDescription(), getMode().toString());
	}

	static public Iterable<PrivilegeCard> forGroup(Integer groupId) {
    	Map<Integer, PrivilegeCard> privMap = new HashMap<Integer, PrivilegeCard>();
		for (ICard card : grantClass.cards().list()
				.filter(ROLE_ID_ATTRIBUTE, AttributeFilterType.EQUALS, String.valueOf(groupId))) {
			PrivilegeCard priv = new PrivilegeCard(card);
			privMap.put(priv.getGrantedClassId(), priv);
		}
		for (ITable grantedClass : UserContext.systemContext().tables().list()) {
			if (grantedClass.getMode().isCustom() && !privMap.containsKey(grantedClass.getId())) {
				PrivilegeCard priv = new PrivilegeCard(groupId, grantedClass.getId(), PrivilegeType.NONE);
				privMap.put(priv.getGrantedClassId(), priv);
			}
		}
		return privMap.values();
	}

	public static PrivilegeCard get(Integer groupId, Integer classId) {
		PrivilegeCard priv;
		try {
			ICard privCard = grantClass.cards().list()
				.filter(ROLE_ID_ATTRIBUTE, AttributeFilterType.EQUALS, String.valueOf(groupId))
				.filter(GRANTED_CLASS_ID_ATTRIBUTE, AttributeFilterType.EQUALS, String.valueOf(classId))
				.get();
			priv = new PrivilegeCard(privCard);
		} catch (NotFoundException e) {
			priv = new PrivilegeCard(groupId, classId, PrivilegeType.NONE);
		}
		return priv;
	}
}
