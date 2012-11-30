package org.cmdbuild.elements.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.CardFactoryImpl;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.interfaces.CardFactory;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.meta.MetadataMap;
import org.cmdbuild.services.meta.MetadataService;

public class TableProxy extends TableForwarder {
	protected UserContext userCtx;

	public TableProxy(ITable table, UserContext userCtx) {
		super(table);
		if (t instanceof TableProxy) {
			// Unwrap the TableProxy
			t = ((TableProxy)t).t;
		}
		this.userCtx = userCtx;
	}

	@Override
	public CardFactory cards() {
		return new CardFactoryImpl(this, userCtx);
	}

	@Override
	public TableTree treeBranch() {
	    return new TableTreeProxy(super.treeBranch(), userCtx);
	}

	@Override
	public MetadataMap getMetadata() {
		MetadataMap xp = super.getMetadata();
		xp.put(MetadataService.RUNTIME_PRIVILEGES_KEY, userCtx.privileges().getPrivilege(t));
		xp.put(MetadataService.RUNTIME_USERNAME_KEY, userCtx.getUser().getName());
		xp.put(MetadataService.RUNTIME_DEFAULTGROUPNAME_KEY, userCtx.getDefaultGroup().getName());
		return xp;
	}

	@Override
	public void delete() throws ORMException {
		userCtx.privileges().assureAdminPrivilege();
		super.delete();
	}

	@Override
	public void save() throws ORMException {
		userCtx.privileges().assureAdminPrivilege();
		super.save();
	}

	/*
	 * TODO: NEEDS REFACTOR TO BE CLEANED UP
	 */

	@Override
	public Map<String, IAttribute> getAttributes() {
		Map<String, IAttribute> proxedAttributes = new HashMap<String, IAttribute>();
		for (IAttribute realAttribute : super.getAttributes().values()) {
			proxedAttributes.put(realAttribute.getName(),
					new AttributeProxy(realAttribute, userCtx));
		}
		return proxedAttributes;
	}

	@Override
	public void setDescription(String description) {
		userCtx.privileges().assureAdminPrivilege();
		super.setDescription(description);
	}

	@Override
	public void addAttribute(IAttribute attribute) {
		userCtx.privileges().assureAdminPrivilege();
		super.addAttribute(attribute);
	}

	@Override
	public void setMode(String mode) {
		userCtx.privileges().assureAdminPrivilege();
		super.setMode(mode);
	}

	@Override
	public void setName(String name) {
		userCtx.privileges().assureAdminPrivilege();
		super.setName(name);
	}

	@Override
	public void setParent(Integer parent) throws NotFoundException {
		userCtx.privileges().assureAdminPrivilege();
		super.setParent(parent);
	}

	@Override
	public void setParent(ITable parent) {
		userCtx.privileges().assureAdminPrivilege();
		super.setParent(parent);
	}

	@Override
	public void setParent(String parent) throws NotFoundException {
		userCtx.privileges().assureAdminPrivilege();
		super.setParent(parent);
	}

	@Override
	public void setStatus(SchemaStatus status) {
		userCtx.privileges().assureAdminPrivilege();
		super.setStatus(status);
	}

	@Override
	public void setSuperClass(boolean isSuperClass) {
		userCtx.privileges().assureAdminPrivilege();
		super.setSuperClass(isSuperClass);
	}

	@Override
	public Iterable<IAttribute> fkDetails() {
		List<IAttribute> fkDetails = new ArrayList<IAttribute>();
		for (IAttribute fkDetail : super.fkDetails()) {
			if (userCtx.privileges().hasReadPrivilege(fkDetail)) {
				fkDetails.add(fkDetail);
			}
		}
		return fkDetails;
	}

	@Override
	public void setUserStoppable(boolean userStoppable) {
		userCtx.privileges().assureAdminPrivilege();
		super.setUserStoppable(userStoppable);
	}
}
