package org.cmdbuild.servlets.resource.shark;

import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.operation.management.LookupOperation;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.resource.OutSimpleXML;
import org.cmdbuild.servlets.resource.RESTExported;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.cmdbuild.workflow.utils.SimpleXMLDoc;
import org.cmdbuild.workflow.utils.SimpleXMLNode;

public class CmdbuildStruct extends AbstractSharkResource {

	public String baseURI() {
		return "cmdbuildstruct";
	}
	
	@RESTExported(subResource="tableInfo")
	public SimpleXMLDoc tableInfo(
			@OutSimpleXML("TableInfo") SimpleXMLDoc out,
			@Parameter(value="classid",required=false) int id,
			@Parameter(value="classname",required=false) String name) throws Exception {

		ITable table;
		if (name != null) {
			table = UserContext.systemContext().tables().get(name);
		} else if(id > 0) {
			table = UserContext.systemContext().tables().get(id);
		} else {
			throw new Exception("cmdbuildstruct/tableInfo needs classid or classname parameters!");
		}
		
		out.getRoot().put("name", table.getName())
		.put("id", table.getId());
		
		SimpleXMLNode attrs = out.getRoot().createChild("Attributes");
		
		for(String aname : table.getAttributes().keySet()) {
			IAttribute attr = table.getAttribute(aname);
			SimpleXMLNode attrNode = attrs.createChild("Attribute");
			attrNode.put("name", attr.getName())
			.put("type", attr.getType().name());
			
			if(attr.getType() == IAttribute.AttributeType.LOOKUP) {
				if (attr.getLookupType() != null) {
					attrNode.put("lookupType", attr.getLookupType().getType());
				}
			} else if(attr.getType() == IAttribute.AttributeType.REFERENCE) {
				attrNode.put("referenceClass", attr.getReferenceTarget().getName());
			}
		}
		return out;
	}
	
	@RESTExported(subResource="loadreference")
	public SimpleXMLDoc loadReference(
			@OutSimpleXML("Reference") SimpleXMLDoc out,
			@Parameter(value="classid", required=false) int classId,
			@Parameter(value="classname", required=false) String className,
			@Parameter("attributename") String attributeName,
			@Parameter("referenceid") int referenceId) throws Exception {
		
		ITable table;
		if(className != null) {
			table = UserContext.systemContext().tables().get(className);
		} else if(classId > 0) {
			table = UserContext.systemContext().tables().get(classId);
		} else {
			throw new Exception("cmdbuildstruct/loadReference needs classid or classname parameters!");
		}
		
		IAttribute referenceAttr = table.getAttribute(attributeName);
		if(!(AttributeType.REFERENCE.equals(referenceAttr.getType()))) {
			throw new Exception("cmdbuildstruct/loadReference needs attributeName to be the name of a reference!");
		}
		ITable refTable;
		if(referenceAttr.isReferenceDirect()) {
			refTable = referenceAttr.getReferenceDomain().getClass2();
		} else {
			refTable = referenceAttr.getReferenceDomain().getClass1();
		}
		
		ICard refCard = refTable.cards().get(referenceId);
		
		ReferenceType refType = new ReferenceType(refCard.getId(),refCard.getIdClass(),refCard.getDescription());
		XMLAttributeHelper.serializeObject(refType, out.getRoot());
		
		return out;
	}
	
	@RESTExported(subResource="loadlookup")
	public SimpleXMLDoc loadLookup(
			@OutSimpleXML("Lookup") SimpleXMLDoc out,
			@Parameter("lookupid") int lookupId) throws Exception {
		LookupOperation lop = new LookupOperation(UserContext.systemContext());
		Lookup lookup = lop.getLookupById(lookupId);
		LookupType lkpType = new LookupType(lookup.getId(),lookup.getType(),lookup.getDescription(), lookup.getCode());
		XMLAttributeHelper.serializeObject(lkpType, out.getRoot());
		
		return out;
	}

}
