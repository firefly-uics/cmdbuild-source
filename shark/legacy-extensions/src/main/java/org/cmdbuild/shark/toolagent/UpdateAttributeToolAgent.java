package org.cmdbuild.shark.toolagent;

import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.shark.util.CmdbuildUtils;
import org.cmdbuild.shark.util.CmdbuildUtils.CmdbuildTableStruct;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

/**
 * Update an attribute.<br/>
 * Parameters:
 * <ol>
 * <li>ClassName:string</li>
 * <li>AttributeName:string</li>
 * <li>ObjId:int</li>
 * <li>AttributeValue:string</li>
 * </ol>
 * 
 * Or:
 * <ol>
 * <li>ObjRef:Reference</li>
 * <li>AttributeName:string</li>
 * <li>AttributeValue:string</li>
 * </ol>
 * 
 * Output is boolean done
 * 
 */
// TODO: again, UpdateAttribute should use original type instead of strings
public class UpdateAttributeToolAgent extends AbstractCmdbuildWSToolAgent {

	@Override
	protected void invokeWebService(final Private stub, final AppParameter[] params, final String toolInfoID)
			throws Exception {

		if ((params.length == 6 && params[1].the_formal_name.equals("ClassName")
				&& params[2].the_formal_name.equals("AttributeName") && params[3].the_formal_name.equals("ObjId") && params[4].the_formal_name
				.equals("AttributeValue"))
				|| (params.length == 5 && params[1].the_formal_name.equals("ObjRef")
						&& params[2].the_formal_name.equals("AttributeName") && params[3].the_formal_name
						.equals("AttributeValue"))) {
			singleAttributeUpdateCall(stub, params);
		} else {
			multiAttributeUpdateCall(stub, params);
		}
	}

	private void singleAttributeUpdateCall(final Private stub, final AppParameter[] params) throws Exception {
		String attrName, attrValue;
		final Card card = new Card();
		final Attribute attr = new Attribute();
		CmdbuildTableStruct tableStruct = null;
		if (params.length == 6) {
			final String className = get(params, 1);
			attrName = get(params, 2);
			final int objId = getInt(params, 3);
			attrValue = get(params, 4);
			card.setClassName(className);
			card.setId(objId);
			tableStruct = CmdbuildUtils.getInstance().getStructureFromName(className);
		} else if (params.length == 5) {
			final ReferenceType objRef = get(params, 1);
			attrName = get(params, 2);
			attrValue = get(params, 3);
			card.setClassName(CmdbuildUtils.getInstance().getStructureFromId(objRef.getIdClass()).getName());
			card.setId(objRef.getId());
			tableStruct = CmdbuildUtils.getInstance().getStructureFromId(objRef.getIdClass());
		} else {
			throw new Exception("Wrong arguments size for UpdateAttribute (5 or 4 params permitted) called with "
					+ (params.length - 1));
		}

		final String orig = attrValue;
		attrValue = resolveParameter(stub, card, attrName, attrValue, tableStruct);
		if (attrValue != null) {
			attr.setName(attrName);
			attr.setValue(attrValue);
			card.getAttributeList().add(attr);
			params[params.length - 1].the_value = stub.updateCard(card);
		} else {
			System.out.println("Card update failed: no card or lookup with specified description (" + orig
					+ ") found for field: " + attrName + "!");
		}
	}

	private void multiAttributeUpdateCall(final Private stub, final AppParameter[] params) throws Exception {
		final Card card = new Card();
		int start = 0;
		CmdbuildTableStruct tableStruct = null;

		// resolve classname/id and objid
		if (params[1].the_class == String.class && params[2].the_class == Long.class) {
			final String cname = get(params, 1);
			final int objid = getInt(params, 2);
			card.setClassName(cname);
			card.setId(objid);
			start = 3;
			tableStruct = CmdbuildUtils.getInstance().getStructureFromName(cname);
		} else if (params[1].the_class == ReferenceType.class) {
			final ReferenceType ref = get(params, 1);
			card.setClassName(CmdbuildUtils.getInstance().getStructureFromId(ref.getIdClass()).getName());
			card.setId(ref.getId());
			start = 2;
			tableStruct = CmdbuildUtils.getInstance().getStructureFromId(ref.getIdClass());
		} else {
			throw new Exception(
					"Cannot understand which card to update (use ClassName/ObjId or ObjRef as first parameters)");
		}

		AppParameter out = null;
		// iterate on params
		for (int i = start; i < params.length; i++) {
			if (params[i].the_mode.equals("OUT") && params[i].the_class == Boolean.class) {
				out = params[i];
			} else {
				final String attrName = params[i].the_formal_name;
				final Object attrValue = params[i].the_value;
				final String serialized = AbstractWSToolAgent.sharkAttributeToWSString(stub, tableStruct, card,
						attrName, attrValue);
				if (serialized != null) {
					final Attribute attr = new Attribute();
					attr.setName(attrName);
					attr.setValue(serialized);
					card.getAttributeList().add(attr);
				}
			}
		}

		final boolean tmp = stub.updateCard(card);
		if (out != null) {
			out.the_value = tmp;
		}
	}

	@Override
	protected boolean returnOnException(final Exception exception, final String toolInfoID,
			final AppParameter[] parameters) {
		parameters[parameters.length - 1].the_value = false;
		return true;
	}

}
