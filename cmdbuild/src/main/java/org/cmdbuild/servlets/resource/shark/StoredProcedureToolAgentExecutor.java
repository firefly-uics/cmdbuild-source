package org.cmdbuild.servlets.resource.shark;

import java.util.List;

import org.cmdbuild.logger.Log;
import org.cmdbuild.servlets.resource.OutSimpleXML;
import org.cmdbuild.servlets.resource.RESTExported;
import org.cmdbuild.servlets.resource.RESTExported.RestMethod;
import org.cmdbuild.servlets.utils.URIParameter;
import org.cmdbuild.workflow.utils.SimpleXMLDoc;
import org.cmdbuild.workflow.utils.SimpleXMLNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StoredProcedureToolAgentExecutor extends AbstractSharkResource {

	public String baseURI() {
		return "executestoredprocedure";
	}
	
	@SuppressWarnings("unchecked")
	@RESTExported(
			httpMethod=RestMethod.POST)
	public SimpleXMLDoc executeStoredProcedure(
			Document doc,
			@URIParameter(1) String storedProcedureName,
			@OutSimpleXML("StoredProcedureResult") SimpleXMLDoc out) throws Exception {
		boolean isRs = Boolean.parseBoolean(doc.getDocumentElement().getAttribute("ResultSetType"));
		
		NodeList inNodes = doc.getElementsByTagName("Input");
		Object[] inObjs = new Object[inNodes.getLength()];
		for(int i=0;i<inNodes.getLength();i++) {
			Node node = inNodes.item(i);
			Object obj = XMLAttributeHelper.parseNode(node);
			inObjs[i] = obj;
		}
		
		NodeList outNodes = doc.getElementsByTagName("OutClass");
		Class[] outCls = new Class[outNodes.getLength()];
		for(int i=0;i<outNodes.getLength();i++) {
			outCls[i] = Class.forName( outNodes.item(i).getTextContent() );
		}

		List<List<Object>> res = null;
		if(isRs) {
			res = StoredProcedureFacade.callStoredProcedureRS(storedProcedureName, inObjs, outCls);
		} else {
			res = StoredProcedureFacade.callStoredProcedure(storedProcedureName, inObjs, outCls);
		}
		
		for(List<Object> row : res) {
			int idx = 0;
			SimpleXMLNode resultNode = out.getRoot().createChild("Result");
			for(Object item : row) {
				SimpleXMLNode itemNode = resultNode.createChild("Item");
				itemNode.put("idx", idx);
				XMLAttributeHelper.serializeObject(item, itemNode);
				idx++;
			}
		}
		
		Log.WORKFLOW.debug("sp name: " +storedProcedureName);
		Log.WORKFLOW.debug("xml:\n" + out.toString());
		return out;
	}
}
