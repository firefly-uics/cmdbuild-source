package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.services.SessionVars;
import org.json.JSONException;
import org.json.JSONObject;

public class DomainSerializer extends Serializer {

	public static JSONObject toClient(final IDomain domain, final boolean activeOnly) throws JSONException {
		return toClient(domain, activeOnly, null);
	}

	/**
	 * @deprecated use serialize(CMDomain) instead.
	 */
	@Deprecated
	public static JSONObject toClient(final IDomain domain, final boolean activeOnly, final String wrapperLabel)
			throws JSONException {
		final JSONObject jsonDomain = new JSONObject();
		jsonDomain.put("idDomain", domain.getId());
		jsonDomain.put("name", domain.getName());
		jsonDomain.put("origName", domain.getName());
		jsonDomain.put("description", domain.getDescription());
		jsonDomain.put("descrdir", domain.getDescriptionDirect());
		jsonDomain.put("descrinv", domain.getDescriptionInverse());
		jsonDomain.put("class1", domain.getTables()[0].toString());
		jsonDomain.put("class1id", domain.getTables()[0].getId());
		jsonDomain.put("class2", domain.getTables()[1].toString());
		jsonDomain.put("class2id", domain.getTables()[1].getId());
		jsonDomain.put("md", domain.isMasterDetail());
		jsonDomain.put("md_label", domain.getMDLabel());
		jsonDomain.put("classType", getClassType(domain.getTables()[0].getName()));
		jsonDomain.put("active", domain.getStatus().isActive());
		jsonDomain.put("cardinality", domain.getCardinality());
		jsonDomain.put("attributes", AttributeSerializer.serializeAttributeList(domain, activeOnly));
		addMetadataAndAccessPrivileges(jsonDomain, domain);

		if (wrapperLabel != null) {
			final JSONObject out = new JSONObject();
			out.put(wrapperLabel, jsonDomain);
			return out;
		} else {
			return jsonDomain;
		}
	}

	public static JSONObject toClient(final CMDomain domain, final boolean activeOnly) throws JSONException {
		return toClient(domain, activeOnly, null);
	}

	public static JSONObject toClient(final CMDomain domain, final boolean activeOnly, final String wrapperLabel)
			throws JSONException {
		final JSONObject jsonDomain = new JSONObject();
		jsonDomain.put("idDomain", domain.getId());
		jsonDomain.put("name", domain.getName());
		jsonDomain.put("origName", domain.getName());
		jsonDomain.put("description", domain.getDescription());
		jsonDomain.put("descrdir", domain.getDescription1());
		jsonDomain.put("descrinv", domain.getDescription2());
		jsonDomain.put("class1", domain.getClass1().getName());
		jsonDomain.put("class1id", domain.getClass1().getId());
		jsonDomain.put("class2", domain.getClass2().getName());
		jsonDomain.put("class2id", domain.getClass2().getId());
		jsonDomain.put("md", domain.isMasterDetail());
		jsonDomain.put("md_label", domain.getMasterDetailDescription());
		jsonDomain.put("classType", getClassType(domain.getName()));
		jsonDomain.put("active", domain.isActive());
		jsonDomain.put("cardinality", domain.getCardinality());
		// FIXME should not be used in this way
		final CMDataView view = TemporaryObjectsBeforeSpringDI.getSystemView();
		jsonDomain.put("attributes", AttributeSerializer.of(view).toClient(domain.getAttributes(), activeOnly));

		addAccessPrivileges(jsonDomain, domain);
		// TODO: complete ...
		// addMetadata(jsonDomain, domain);

		if (wrapperLabel != null) {
			final JSONObject out = new JSONObject();
			out.put(wrapperLabel, jsonDomain);
			return out;
		} else {
			return jsonDomain;
		}
	}

	private static void addAccessPrivileges(final JSONObject jsonObject, final CMDomain domain) throws JSONException {
		final OperationUser user = new SessionVars().getUser();
		final boolean writePrivilege = user.hasWriteAccess(domain);
		final boolean createPrivilege = writePrivilege;
		jsonObject.put("priv_write", writePrivilege);
		jsonObject.put("priv_create", createPrivilege);
	}

	public static JSONObject toClient(final CMDomain domain, final String className) throws JSONException {
		final JSONObject jsonDomain = toClient(domain, false);
		jsonDomain.put("inherited", !isDomainDefinedForClass(domain, className));
		return jsonDomain;
	}

	/**
	 * @return true if the domain is defined for the class with provided
	 *         classId, false otherwise (it is defined for a superclass)
	 */
	private static boolean isDomainDefinedForClass(final CMDomain domain, final String className) {
		final CMClass class1 = domain.getClass1();
		final CMClass class2 = domain.getClass2();
		return class1.getName().equals(className) || class2.getId().equals(className);
	}
}
