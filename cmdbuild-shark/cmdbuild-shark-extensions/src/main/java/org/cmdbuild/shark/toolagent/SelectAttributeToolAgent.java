package org.cmdbuild.shark.toolagent;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.shark.util.CmdbuildUtils;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

/**
 * Select an attribute into a string (to be changed) Parameters:
 * <ol>
 * <li>ClassName:string</li>
 * <li>AttributeName:string</li>
 * <li>ObjId:int</li>
 * </ol>
 * 
 * or:
 * 
 * <ol>
 * <li>ObjectReference:Reference</li>
 * <li>AttributeName:string</li>
 * </ol>
 * 
 * Output: attributeValue:string
 */
public class SelectAttributeToolAgent extends AbstractCmdbuildWSToolAgent {

	@Override
	protected void invokeWebService(final Private stub, final AppParameter[] params, final String toolInfoID)
			throws Exception {
		if (params.length == 5) {
			selectAttribute(stub, params);
		} else if (params.length == 4) {
			selectAttributeFromReference(stub, params);
		} else {
			throw new Exception("Wrong arguments size for SelectAttribute (4 or 3 params permitted) called with "
					+ (params.length - 1));
		}

	}

	private void selectAttribute(final Private stub, final AppParameter[] params) throws RemoteException {
		final String cName = ((String) get(params, 1)).trim();
		final String attrName = ((String) get(params, 2)).trim();
		final int objId = getInt(params, 3);
		if (cName != null && cName.length() > 0 && attrName != null && attrName.length() > 0 && objId > 0) {

			final Attribute attr = new Attribute();
			attr.setName(attrName);
			final List<Attribute> alist = new ArrayList<Attribute>();
			alist.add(attr);

			final Card card = stub.getCard(cName, objId, alist);

			final String outputValue = getValue(card, attrName);
			setOutputValue(params, outputValue);
		} else {
			setOutputValue(params, null);
		}
	}

	private void selectAttributeFromReference(final Private stub, final AppParameter[] params) throws Exception,
			RemoteException {
		final ReferenceType oRef = get(params, 1);
		final String attrName = ((String) get(params, 2)).trim();
		if (oRef != null && oRef.checkValidity() && attrName != null && attrName.length() > 0) {

			final String classname = CmdbuildUtils.getInstance().getStructureFromId(oRef.getIdClass()).getName();
			final List<Attribute> alist = new ArrayList<Attribute>();
			final Attribute attr = new Attribute();
			attr.setName(attrName);
			alist.add(attr);
			final Card card = stub.getCard(classname, oRef.getId(), alist);
			final String outputValue = getValue(card, attrName);
			setOutputValue(params, outputValue);
		} else {
			setOutputValue(params, null);
		}
	}

	private void setOutputValue(final AppParameter[] params, final String outputValue) {
		final AppParameter out = params[params.length - 1];
		out.the_value = outputValue;
	}

	private String getValue(final Card card, final String attributeName) {
		String out;
		if ("Id".equals(attributeName)) {
			out = String.valueOf(card.getId());
		} else if ("ClassName".equals(attributeName)) {
			out = card.getClassName();
		} else if ("BeginDate".equals(attributeName)) {
			out = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(card.getBeginDate().toGregorianCalendar()
					.getTime());
		} else if ("EndDate".equals(attributeName)) {
			out = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(card.getEndDate().toGregorianCalendar()
					.getTime());
		} else if ("User".equals(attributeName)) {
			out = card.getUser();
		} else {
			final Attribute attr = card.getAttributeList().get(0);
			if (attr.getCode() != null && attr.getCode().length() > 0) {
				out = attr.getCode();
			} else {
				out = attr.getValue();
			}
		}
		return out;
	}

}
