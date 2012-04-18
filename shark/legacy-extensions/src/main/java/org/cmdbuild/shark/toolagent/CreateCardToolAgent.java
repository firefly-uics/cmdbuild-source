package org.cmdbuild.shark.toolagent;

import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.shark.util.CmdbuildUtils;
import org.cmdbuild.shark.util.CmdbuildUtils.CmdbuildTableStruct;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.xpdl.elements.ExtendedAttributes;

/**
 * Create a card.<br/>
 * Possible parameters combinations:
 * 
 * <ol>
 * <li>ClassName:string</li>
 * <li>CardCode:string</li>
 * <li>CardDescription:string</li>
 * </ol>
 * 
 * Output: cardId:int or cardRef:Reference
 * 
 * @author cheng
 * 
 */
public class CreateCardToolAgent extends AbstractCmdbuildWSToolAgent {

	@Override
	protected void invokeWebService(final Private stub, final AppParameter[] params, final String toolInfoID)
			throws Exception {

		if (params[1].the_formal_name.equals("ClassName") && params[2].the_formal_name.equals("CardCode")
				&& params[3].the_formal_name.equals("CardDescription")
				&& (params[4].the_formal_name.equals("CardId") || params[4].the_formal_name.equals("CardReference"))) {
			standardCreate(stub, params);
		} else {
			customCreate(stub, params);
		}
	}

	private void standardCreate(final Private stub, final AppParameter[] params) throws Exception {
		final String className = get(params, 1);
		final String code = get(params, 2);
		final String descr = get(params, 3);

		final Card card = new Card();
		card.setClassName(className);
		card.getAttributeList().add(createAttribute("Code", code));
		card.getAttributeList().add(createAttribute("Description", descr));

		final int newCardId = stub.createCard(card);

		final AppParameter parmOut = params[params.length - 1];
		if (parmOut.the_class == Long.class) {
			parmOut.the_value = newCardId;
		} else if (parmOut.the_class == ReferenceType.class) {
			final ReferenceType ref = new ReferenceType();
			ref.setId(newCardId);
			ref.setIdClass(CmdbuildUtils.getInstance().getStructureFromName(className).getId());
			ref.setDescription(descr);

			parmOut.the_value = ref;
		}
	}

	private Attribute createAttribute(final String name, final String value) {
		final Attribute attr = new Attribute();
		attr.setName(name);
		attr.setValue(value);
		return attr;
	}

	private void customCreate(final Private stub, final AppParameter[] params) throws Exception {
		final ExtendedAttributes eas = this.readParamsFromExtAttributes((String) parameters[0].the_value);
		String className = null;
		int startIndex = 1;
		if (eas.getFirstExtendedAttributeForName("ClassName") != null) {
			className = eas.getFirstExtendedAttributeForName("ClassName").getVValue();
		} else if (params[1].the_formal_name.equalsIgnoreCase("ClassName")) {
			className = get(params, 1);
			startIndex = 2;
		} else {
			throw new Exception("Cannot find ClassName in extended attributes or parameters!");
		}

		final CmdbuildTableStruct tableStruct = CmdbuildUtils.getInstance().getStructureFromName(className);

		final Card card = new Card();
		card.setClassName(className);
		AppParameter parmOut = null;
		String descr = "";
		for (int i = startIndex; i < params.length; i++) {
			if (params[i].the_mode.equals("OUT")) {
				parmOut = params[i];
			} else {
				final String attrName = params[i].the_formal_name;
				final Object attrValue = params[i].the_value;
				final String serialized = AbstractWSToolAgent.sharkAttributeToWSString(stub, tableStruct, card,
						attrName, attrValue);
				if (serialized != null) {
					if (attrName.equals("Description")) {
						descr = serialized;
					}
					card.getAttributeList().add(createAttribute(attrName, serialized));
				}
			}
		}
		final int newCardId = stub.createCard(card);
		if (parmOut.the_class == Long.class) {
			parmOut.the_value = newCardId;
		} else if (parmOut.the_class == ReferenceType.class) {
			final ReferenceType ref = new ReferenceType();
			ref.setId(newCardId);
			ref.setIdClass(CmdbuildUtils.getInstance().getStructureFromName(className).getId());
			ref.setDescription(descr);

			parmOut.the_value = ref;
		}
	}

}
