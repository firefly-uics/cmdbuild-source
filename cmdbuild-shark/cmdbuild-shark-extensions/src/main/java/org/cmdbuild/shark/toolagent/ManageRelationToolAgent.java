package org.cmdbuild.shark.toolagent;

import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Relation;
import org.cmdbuild.shark.util.CmdbuildUtils;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

/**
 * Relations management tool.
 * 
 */
public class ManageRelationToolAgent extends AbstractCmdbuildWSToolAgent {

	private static final String PARAM_CARD_ID = "CardId";
	private static final String PARAM_CLASS_NAME = "ClassName";
	private static final String PARAM_DOMAIN_NAME = "DomainName";
	private static final String PARAM_REF = "Ref";
	private static final String PARAM_CLASS_NAME_1 = "ClassName1";
	private static final String PARAM_CLASS_NAME_2 = "ClassName2";
	private static final String PARAM_OBJ_ID_1 = "ObjId1";
	private static final String PARAM_OBJ_ID_2 = "ObjId2";
	private static final String PARAM_OBJ_REFERENCE_1 = "ObjReference1";
	private static final String PARAM_OBJ_REFERENCE_2 = "ObjReference2";

	private enum ToolApplications {
		selectRelations, selectRelationsByReference, createRelation, createRelationRefs, createRelation1Ref, createRelation2Ref, deleteRelation, deleteRelationByReference
	}
	
	@Override
	protected void invokeWebService(final Private stub, final AppParameter[] params, final String toolInfoID)
			throws Exception {
		final ToolApplications toolApplication = ToolApplications.valueOf(toolInfoID);

		Object out = null;

		switch (toolApplication) {

		case selectRelations: {
			final Long cardId = getParameterValue(params, PARAM_CARD_ID);
			final String className = getParameterValue(params, PARAM_CLASS_NAME);
			final String domainName = getParameterValue(params, PARAM_DOMAIN_NAME);
			out = selectRelation(stub, cardId, className, domainName);
		}
			break;

		case selectRelationsByReference: {
			final ReferenceType referenceType = getParameterValue(params, PARAM_REF);
			final String domainName = getParameterValue(params, PARAM_DOMAIN_NAME);
			out = selectRelationByRef(stub, referenceType, domainName);
		}
			break;

		case createRelation: {
			final String domainName = getParameterValue(params, PARAM_DOMAIN_NAME);
			final String className1 = getParameterValue(params, PARAM_CLASS_NAME_1);
			final String className2 = getParameterValue(params, PARAM_CLASS_NAME_2);
			final Integer objId1 = getParameterValue(params, PARAM_OBJ_ID_1);
			final Integer objId2 = getParameterValue(params, PARAM_OBJ_ID_2);
			out = createRelation(stub, domainName, className1, className2, objId1, objId2);
		}
			break;

		case createRelationRefs: {
			final String domainName = getParameterValue(params, PARAM_DOMAIN_NAME);
			final ReferenceType referenceType1 = getParameterValue(params, PARAM_OBJ_REFERENCE_1);
			final ReferenceType referenceType2 = getParameterValue(params, PARAM_OBJ_REFERENCE_2);
			out = createRelationRefs(stub, domainName, referenceType1, referenceType2);
		}
			break;

		case createRelation1Ref: {
			final String domainName = getParameterValue(params, PARAM_DOMAIN_NAME);
			final ReferenceType referenceType1 = getParameterValue(params, PARAM_OBJ_REFERENCE_1);
			final String className2 = getParameterValue(params, PARAM_CLASS_NAME_2);
			final Integer objId2 = getParameterValue(params, PARAM_OBJ_ID_2);
			out = createRelation1Ref(stub, domainName, referenceType1, className2, objId2);
		}
			break;

		case createRelation2Ref: {
			final String domainName = getParameterValue(params, PARAM_DOMAIN_NAME);
			final String className1 = getParameterValue(params, PARAM_CLASS_NAME_1);
			final Integer objId1 = getParameterValue(params, PARAM_OBJ_ID_1);
			final ReferenceType referenceType2 = getParameterValue(params, PARAM_OBJ_REFERENCE_2);
			out = createRelation2Ref(stub, domainName, className1, objId1, referenceType2);
		}
			break;

		case deleteRelation: {
			final String domainName = getParameterValue(params, PARAM_DOMAIN_NAME);
			final String className1 = getParameterValue(params, PARAM_CLASS_NAME_1);
			final String className2 = getParameterValue(params, PARAM_CLASS_NAME_2);
			final Integer objId1 = getParameterValue(params, PARAM_OBJ_ID_1);
			final Integer objId2 = getParameterValue(params, PARAM_OBJ_ID_2);
			out = deleteRelation(stub, domainName, className1, className2, objId1, objId2);
		}
			break;

		case deleteRelationByReference: {
			final String domainName = getParameterValue(params, PARAM_DOMAIN_NAME);
			final ReferenceType referenceType1 = getParameterValue(params, PARAM_OBJ_REFERENCE_1);
			final ReferenceType referenceType2 = getParameterValue(params, PARAM_OBJ_REFERENCE_2);
			out = deleteRelationByReferences(stub, domainName, referenceType1, referenceType2);
		}
			break;

		default:
			throw new Exception("Unhandled tool: " + toolInfoID);
		}

		setOutputValue(params, out);
	}

	@Override
	protected boolean returnOnException(final Exception exception, final String toolInfoID,
			final AppParameter[] parameters) {
		final ToolApplications ap = ToolApplications.valueOf(toolInfoID);
		switch (ap) {
		case createRelation:
		case createRelationRefs:
		case createRelation1Ref:
		case createRelation2Ref:
			setOutputValue(parameters, false);
			return true;
		}
		return super.returnOnException(exception, toolInfoID, parameters);
	}

	private void setOutputValue(final AppParameter[] params, final Object out) {
		for (final AppParameter param : params) {
			if ("OUT".equals(param.the_mode)) {
				param.the_value = out;
			}
		}
	}

	private ReferenceType[] selectRelation(final Private stub, final long cardId, final String className,
			final String domainName) throws Exception {
		final List<ReferenceType> references = new LinkedList<ReferenceType>();
		final List<Relation> relations = stub.getRelationList(domainName, className, (int) cardId);

		// get the card having relation with master card
		if (relations != null) {
			for (final Relation relation : relations) {
				// get the right id (we don't know who is class1 is)
				references.add(createReferenceType(relation, cardId));
			}
		}

		final ReferenceType[] referenceArray = new ReferenceType[relations.size()];
		return references.toArray(referenceArray);
	}

	private ReferenceType[] selectRelationByRef(final Private stub, final ReferenceType referenceType,
			final String domainName) throws Exception {
		final int cardId = referenceType.getId();
		final String className = CmdbuildUtils.getInstance().getStructureFromId(referenceType.getIdClass()).getName();
		return selectRelation(stub, cardId, className, domainName);
	}

	private ReferenceType createReferenceType(final Relation relation, final long cardId) throws Exception {
		final ReferenceType reference = new ReferenceType();
		if ((int) cardId == relation.getCard1Id()) {
			reference.setId(relation.getCard2Id());
			reference.setIdClass(CmdbuildUtils.getInstance().getStructureFromName(relation.getClass2Name()).getId());
		} else {
			reference.setId(relation.getCard1Id());
			reference.setIdClass(CmdbuildUtils.getInstance().getStructureFromName(relation.getClass1Name()).getId());
		}
		// TODO gets the description and the classId, needs another call to the
		// ws
		reference.setDescription("");
		return reference;
	}

	private boolean createRelation(final Private stub, final String domainName, final String className1,
			final String className2, final int classId1, final int classId2) throws Exception {
		final Relation relation = new Relation();
		relation.setDomainName(domainName);
		relation.setClass1Name(className1);
		relation.setClass2Name(className2);
		relation.setCard1Id(classId1);
		relation.setCard2Id(classId2);
		return stub.createRelation(relation);
	}

	private boolean createRelationRefs(final Private stub, final String domainName, final ReferenceType referenceType1,
			final ReferenceType referenceType2) throws Exception {
		final String className1 = CmdbuildUtils.getInstance().getStructureFromId(referenceType1.getIdClass()).getName();
		final String className2 = CmdbuildUtils.getInstance().getStructureFromId(referenceType2.getIdClass()).getName();
		return createRelation(stub, domainName, className1, className2, referenceType1.getId(), referenceType2.getId());
	}

	private boolean createRelation1Ref(final Private stub, final String domainName, final ReferenceType referenceType1,
			final String className2, final int objId2) throws Exception {
		final String className1 = CmdbuildUtils.getInstance().getStructureFromId(referenceType1.getIdClass()).getName();
		return createRelation(stub, domainName, className1, className2, referenceType1.getId(), objId2);
	}

	private boolean createRelation2Ref(final Private stub, final String domainName, final String className1,
			final int objId1, final ReferenceType referenceType2) throws Exception {
		final String className2 = CmdbuildUtils.getInstance().getStructureFromId(referenceType2.getIdClass()).getName();
		return createRelation(stub, domainName, className1, className2, objId1, referenceType2.getId());
	}

	private boolean deleteRelation(final Private stub, final String domainName, final String className1,
			final String className2, final int classId1, final int classId2) {
		final Relation relation = new Relation();
		relation.setDomainName(domainName);
		relation.setClass1Name(className1);
		relation.setClass2Name(className2);
		relation.setCard1Id(classId1);
		relation.setCard2Id(classId2);
		return stub.deleteRelation(relation);
	}

	private boolean deleteRelationByReferences(final Private stub, final String domainName,
			final ReferenceType referenceType1, final ReferenceType referenceType2) throws Exception {
		final String className1 = CmdbuildUtils.getInstance().getStructureFromId(referenceType1.getIdClass()).getName();
		final String className2 = CmdbuildUtils.getInstance().getStructureFromId(referenceType2.getIdClass()).getName();
		return deleteRelation(stub, domainName, className1, className2, referenceType1.getId(), referenceType2.getId());
	}

}
