package org.cmdbuild.elements.proxy;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.auth.UserContext;

public class AttributeProxy extends AttributeForwarder {
	protected UserContext userCtx;

	public AttributeProxy(IAttribute a, UserContext userCtx) {
		super(a);
		this.userCtx = userCtx;
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
	public void setBaseDSP(boolean isBaseDSP) {
		userCtx.privileges().assureAdminPrivilege();
		super.setBaseDSP(isBaseDSP);
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		userCtx.privileges().assureAdminPrivilege();
		super.setDefaultValue(defaultValue);
	}

	@Override
	public void setDescription(String description) {
		userCtx.privileges().assureAdminPrivilege();
		super.setDescription(description);
	}

	@Override
	public void setFieldMode(String modeName) {
		userCtx.privileges().assureAdminPrivilege();
		super.setFieldMode(modeName);
	}

	@Override
	public void setIndex(int index) {
		userCtx.privileges().assureAdminPrivilege();
		super.setIndex(index);
	}

	@Override
	public void setIsReferenceDirect(boolean isReferenceDirect) {
		userCtx.privileges().assureAdminPrivilege();
		super.setIsReferenceDirect(isReferenceDirect);
	}

	@Override
	public void setLength(int length) {
		userCtx.privileges().assureAdminPrivilege();
		super.setLength(length);
	}

	@Override
	public void setLookupType(String lookupName) {
		userCtx.privileges().assureAdminPrivilege();
		super.setLookupType(lookupName);
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
	public void setNotNull(boolean isNotNull) {
		userCtx.privileges().assureAdminPrivilege();
		super.setNotNull(isNotNull);
	}

	@Override
	public void setPrecision(int precision) {
		userCtx.privileges().assureAdminPrivilege();
		super.setPrecision(precision);
	}

	@Override
	public void setReferenceDomain(IDomain domain) {
		userCtx.privileges().assureAdminPrivilege();
		super.setReferenceDomain(domain);
	}

	@Override
	public void setReferenceDomain(int idDomain) throws NotFoundException {
		userCtx.privileges().assureAdminPrivilege();
		super.setReferenceDomain(idDomain);
	}

	@Override
	public void setReferenceDomain(String domainName) throws NotFoundException {
		userCtx.privileges().assureAdminPrivilege();
		super.setReferenceDomain(domainName);
	}

	@Override
	public void setReferenceType(String referenceType) {
		userCtx.privileges().assureAdminPrivilege();
		super.setReferenceType(referenceType);
	}

	@Override
	public void setScale(int scale) {
		userCtx.privileges().assureAdminPrivilege();
		super.setScale(scale);
	}

	@Override
	public void setSchema(BaseSchema schema) {
		userCtx.privileges().assureAdminPrivilege();
		super.setSchema(schema);
	}

	@Override
	public void setStatus(SchemaStatus status) {
		userCtx.privileges().assureAdminPrivilege();
		super.setStatus(status);
	}

	@Override
	public void setUnique(boolean isUnique) {
		userCtx.privileges().assureAdminPrivilege();
		super.setUnique(isUnique);
	}
}
