package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.logic.translation.DefaultTranslationLogic.DESCRIPTION_FOR_CLIENT;
import static org.cmdbuild.logic.translation.DefaultTranslationLogic.DIRECT_DESCRIPTION_FOR_CLIENT;
import static org.cmdbuild.logic.translation.DefaultTranslationLogic.INVERSE_DESCRIPTION_FOR_CLIENT;
import static org.cmdbuild.logic.translation.DefaultTranslationLogic.MASTER_DETAIL_LABEL_FOR_CLIENT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_DIRECT_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_INVERSE_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_MASTERDETAIL_LABEL;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DIRECT_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.INVERSE_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.MASTERDETAIL_LABEL;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.translation.DomainTranslation;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;
import org.json.JSONException;
import org.json.JSONObject;

public class DomainSerializer extends Serializer {

	private final CMDataView dataView;
	private final PrivilegeContext privilegeContext;
	private final TranslationFacade translationFacade;

	public DomainSerializer(final CMDataView dataView, final PrivilegeContext privilegeContext,
			final TranslationFacade translationFacade) {
		this.dataView = dataView;
		this.privilegeContext = privilegeContext;
		this.translationFacade = translationFacade;
	}

	public JSONObject toClient(final CMDomain domain, final boolean activeOnly) throws JSONException {
		return toClient(domain, activeOnly, null);
	}

	public JSONObject toClient(final CMDomain domain, final boolean activeOnly, final String wrapperLabel)
			throws JSONException {
		final JSONObject jsonDomain = new JSONObject();
		jsonDomain.put("idDomain", domain.getId());
		final String localName = domain.getIdentifier().getLocalName();
		jsonDomain.put("name", localName);
		jsonDomain.put("origName", localName);

		final TranslationObject translationObjectForDescription = DomainTranslation.newInstance() //
				.withField(DESCRIPTION_FOR_CLIENT) //
				.withName(localName) //
				.build();
		final String translatedDescription = translationFacade.read(translationObjectForDescription);
		jsonDomain.put(DESCRIPTION, defaultIfNull(translatedDescription, domain.getDescription()));
		jsonDomain.put(DEFAULT_DESCRIPTION, domain.getDescription());

		final TranslationObject translationObjectForDirectDescription = DomainTranslation.newInstance() //
				.withField(DIRECT_DESCRIPTION_FOR_CLIENT) //
				.withName(localName) //
				.build();
		final String translatedDirectDescription = translationFacade.read(translationObjectForDirectDescription);
		jsonDomain.put(DIRECT_DESCRIPTION, defaultIfNull(translatedDirectDescription, domain.getDescription1()));
		jsonDomain.put(DEFAULT_DIRECT_DESCRIPTION, domain.getDescription1());

		final TranslationObject translationObjectForInverseDescription = DomainTranslation.newInstance() //
				.withField(INVERSE_DESCRIPTION_FOR_CLIENT) //
				.withName(localName) //
				.build();
		final String translatedInverseDescription = translationFacade.read(translationObjectForInverseDescription);
		jsonDomain.put(INVERSE_DESCRIPTION, defaultIfNull(translatedInverseDescription, domain.getDescription2()));
		jsonDomain.put(DEFAULT_INVERSE_DESCRIPTION, domain.getDescription2());

		final CMClass class1 = domain.getClass1();
		if (class1 != null) {
			jsonDomain.put("class1", domain.getClass1().getIdentifier().getLocalName());
			jsonDomain.put("class1id", domain.getClass1().getId());
		}

		final CMClass class2 = domain.getClass2();
		if (class2 != null) {
			jsonDomain.put("class2", domain.getClass2().getIdentifier().getLocalName());
			jsonDomain.put("class2id", domain.getClass2().getId());
		}

		jsonDomain.put("md", domain.isMasterDetail());

		final TranslationObject translationObjectForMasterDetailLabel = DomainTranslation.newInstance() //
				.withField(MASTER_DETAIL_LABEL_FOR_CLIENT) //
				.withName(localName) //
				.build();
		final String translatedMasterDetailLabel = translationFacade.read(translationObjectForMasterDetailLabel);
		jsonDomain.put(MASTERDETAIL_LABEL,
				defaultIfNull(translatedMasterDetailLabel, domain.getMasterDetailDescription()));
		jsonDomain.put(DEFAULT_MASTERDETAIL_LABEL, domain.getMasterDetailDescription());

		jsonDomain.put("active", domain.isActive());
		jsonDomain.put("cardinality", domain.getCardinality());
		// FIXME should not be used in this way
		final AttributeSerializer attributeSerializer = AttributeSerializer.newInstance() //
				.withDataView(dataView) //
				.build();
		jsonDomain.put("attributes", attributeSerializer.toClient(domain.getAttributes(), activeOnly));

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

	private String getClassType(final String className) {
		// TODO do it better
		final CMClass target = dataView.findClass(className);
		if (dataView.getActivityClass().isAncestorOf(target)) {
			return "processclass";
		} else {
			return "class";
		}
	}

	private void addAccessPrivileges(final JSONObject jsonObject, final CMDomain domain) throws JSONException {
		final boolean writePrivilege = privilegeContext.hasWriteAccess(domain);
		final boolean createPrivilege = writePrivilege;
		jsonObject.put("priv_write", writePrivilege);
		jsonObject.put("priv_create", createPrivilege);
	}

	public JSONObject toClient(final CMDomain domain, final String className) throws JSONException {
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
		return class1.getName().equals(className) || class2.getName().equals(className);
	}
}
