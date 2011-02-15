package org.cmdbuild.elements.proxy;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.meta.MetadataMap;
import org.cmdbuild.services.meta.MetadataService;

public class DomainProxy extends DomainForwarder {
	protected UserContext userCtx;

	public DomainProxy(IDomain domain, UserContext userCtx) {
		super(domain);
		this.userCtx = userCtx;
	}

	@Override
	public ITable getClass1() {
	    return new TableProxy(super.getClass1(), userCtx);
	}

	@Override
	public ITable getClass2() {
	    return new TableProxy(super.getClass2(), userCtx);
	}

	@Override
	public void delete() {
		userCtx.privileges().assureAdminPrivilege();
		super.delete();
	}

	@Override
	public void save() {
		userCtx.privileges().assureAdminPrivilege();
		super.save();
	}

	@Override
	public MetadataMap getMetadata() {
		MetadataMap xp = super.getMetadata();
		xp.put(MetadataService.RUNTIME_PRIVILEGES_KEY, userCtx.privileges().getPrivilege(this.d));
		xp.put(MetadataService.RUNTIME_USERNAME_KEY, userCtx.getUser().getName());
		xp.put(MetadataService.RUNTIME_DEFAULTGROUPNAME_KEY, userCtx.getDefaultGroup().getName());
		return xp;
	}

	/*
	 * TODO: NEEDS REFACTOR TO BE CLEANED UP
	 */

	@Override
	public void setCardinality(String cardinality) {
		userCtx.privileges().assureAdminPrivilege();
		super.setCardinality(cardinality);
	}

	@Override
	public void setClass1(ITable table) {
		userCtx.privileges().assureAdminPrivilege();
		super.setClass1(table);
	}

	@Override
	public void setClass2(ITable table) {
		userCtx.privileges().assureAdminPrivilege();
		super.setClass2(table);
	}

	@Override
	public void setDescription(String description) {
		userCtx.privileges().assureAdminPrivilege();
		super.setDescription(description);
	}

	@Override
	public void setDescriptionDirect(String descriptionDirect) {
		userCtx.privileges().assureAdminPrivilege();
		super.setDescriptionDirect(descriptionDirect);
	}

	@Override
	public void setDescriptionInverse(String descriptionInverse) {
		userCtx.privileges().assureAdminPrivilege();
		super.setDescriptionInverse(descriptionInverse);
	}

	@Override
	public void setMasterDetail(boolean isMasterDetail) {
		userCtx.privileges().assureAdminPrivilege();
		super.setMasterDetail(isMasterDetail);
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
	public void setOpenedRows(int openedRows) {
		userCtx.privileges().assureAdminPrivilege();
		super.setOpenedRows(openedRows);
	}

	@Override
	public void setStatus(SchemaStatus status) {
		userCtx.privileges().assureAdminPrivilege();
		super.setStatus(status);
	}

	@Override
	public void addAttribute(IAttribute attribute) {
		userCtx.privileges().assureAdminPrivilege();
		super.addAttribute(attribute);
	}

	@Override
	public void reloadCache() {
		userCtx.privileges().assureAdminPrivilege();
		super.reloadCache();
	}
}
