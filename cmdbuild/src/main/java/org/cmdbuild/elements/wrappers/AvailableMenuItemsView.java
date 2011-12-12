package org.cmdbuild.elements.wrappers;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.CardFactoryImpl;
import org.cmdbuild.elements.interfaces.CardFactory;
import org.cmdbuild.elements.interfaces.IAttribute;
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
			attributes.put("IdClass", attr.get("IdClass"));
			attributes.put("Description", attr.get("Description"));
			attributes.put("Code", attr.get("Code"));
			attributes.put("Type", attr.get("Type"));
			attributes.put("IdElementObj", attr.get("IdElementObj"));
			attributes.put("IdElementClass", attr.get("IdElementClass"));
			attributes.put("GroupName", attr.get("GroupName"));
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
