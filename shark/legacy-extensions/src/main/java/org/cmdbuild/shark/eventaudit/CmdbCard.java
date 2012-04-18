/**
 * 
 */
package org.cmdbuild.shark.eventaudit;

import java.util.List;

import org.cmdbuild.workflow.utils.SimpleXMLDoc;
import org.cmdbuild.workflow.utils.SimpleXMLNode;


public class CmdbCard {
	int cardId;
	String cmdbClass;
	List<CmdbAttr> attrs;
	public CmdbCard(int id, String cls, List<CmdbAttr> attrs){this.cardId = id; this.cmdbClass = cls; this.attrs=attrs;}
	
	public SimpleXMLDoc toSimpleXML() {
		SimpleXMLDoc out = new SimpleXMLDoc("Card");
		out.getRoot().put("CardId", cardId).put("CmdbuildClass", cmdbClass);
		SimpleXMLNode nodeattrs = out.getRoot().createChild("Attributes");
		for(CmdbAttr attr : attrs) {
			attr.putNode(nodeattrs);
		}
		return out;
	}
}