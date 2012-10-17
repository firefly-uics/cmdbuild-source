package org.cmdbuild.elements.wrappers;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.CardFactoryImpl;
import org.cmdbuild.elements.interfaces.CardFactory;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.elements.proxy.TableForwarder;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.auth.UserContext;
import org.springframework.beans.factory.annotation.Autowired;

public class AvailableMenuItemsView extends TableForwarder {

	@Autowired
	private CMBackend backend = CMBackend.INSTANCE;

	static final String AvailableMenuView = "system_availablemenuitems";
	Map<String, IAttribute> attributes;

	public AvailableMenuItemsView() {
		super(UserContext.systemContext().tables().get(MenuCard.MENU_CLASS_NAME));
	}

	public String getDBName() {
		return AvailableMenuView;
	}

	public Map<String, IAttribute> getAttributes() {
		if (attributes == null) {
			attributes = new HashMap<String, IAttribute>();

			Map<String, IAttribute> attr = backend.findAttributes(t);
			attributes.put(CardAttributes.ClassId.toString(), attr.get(CardAttributes.ClassId.toString()));
			attributes.put(CardAttributes.Description.toString(), attr.get(CardAttributes.Description.toString()));
			attributes.put(CardAttributes.Code.toString(), attr.get(CardAttributes.Code.toString()));
			attributes.put(MenuCard.TYPE_ATTR, attr.get(MenuCard.TYPE_ATTR));
			attributes.put(MenuCard.ELEMENT_OBJECT_ID_ATTR, attr.get(MenuCard.ELEMENT_OBJECT_ID_ATTR));
			attributes.put(MenuCard.ELEMENT_CLASS_ID_ATTR, attr.get(MenuCard.ELEMENT_CLASS_ID_ATTR));
			attributes.put(MenuCard.GROUP_NAME_ATTR, attr.get(MenuCard.GROUP_NAME_ATTR));
		}
		return attributes;
	}


	// The card factory must be initialized with the proxed ITable
	public CardFactory cards() {
		return new CardFactoryImpl(this, UserContext.systemContext());
	}

	public void save() throws ORMException {
		throw ORMExceptionType.ORM_READ_ONLY_TABLE.createException();
	}
}
