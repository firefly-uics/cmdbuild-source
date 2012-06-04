package org.cmdbuild.shark.eventaudit;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.BOOLEAN_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.DATETIME_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.EXTERNAL_REFERENCE_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.FLOAT_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.INTEGER_TYPE;
import static org.enhydra.shark.api.client.wfmc.wapi.WMAttribute.STRING_TYPE;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public enum CmdbAttrXMLParser {
	STRING(STRING_TYPE){
		public Object parse(Node node) {
			return node.getTextContent();
		};
	},
	INT(INTEGER_TYPE){
		@Override
		public Object parse(Node node) {
			return Integer.parseInt(node.getTextContent());
		}
	},
	BOOL(BOOLEAN_TYPE){
		@Override
		public Object parse(Node node) {
			return Boolean.parseBoolean(node.getTextContent());
		}
	},
	DATE(DATETIME_TYPE){
		@Override
		public Object parse(Node node) {
			try {
				Date dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(node.getTextContent());
				Calendar out = Calendar.getInstance();
				out.setTime(dt);
				return out;
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			System.err.println("failed to format date: " + node.getTextContent());
			return null;
		}
	},
	FLOAT(FLOAT_TYPE){
		@Override
		public Object parse(Node node) {
			return Double.parseDouble(node.getTextContent());
		}
	},
	EXTERNAL(EXTERNAL_REFERENCE_TYPE){
		@SuppressWarnings("unchecked")
		@Override
		public Object parse(Node node) {
			String className = node.getAttributes().getNamedItem("class").getNodeValue();
			try {
				Class cls = Class.forName(className);
				if(cls.equals(LookupType.class)) {
					return createLookupType(node);
				} else if(cls.equals(ReferenceType.class)) {
					return createReferenceType(node);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		private LookupType createLookupType(Node node){
			int id = -1;
			String type = "";
			String code = "";
			String desc = "";
			NodeList nl = node.getChildNodes();
			for(int i=0;i<nl.getLength();i++){
				Node n = nl.item(i);
				if(n.getNodeName().equals("Id")){ id = Integer.parseInt(n.getTextContent()); }
				else if(n.getNodeName().equals("Type")){ type = n.getTextContent(); }
				else if(n.getNodeName().equals("Code")){ code = n.getTextContent(); }
				else if(n.getNodeName().equals("Description")){ desc = n.getTextContent(); }
			}
			return new LookupType(id,type,desc, code);
		}
		private ReferenceType createReferenceType(Node node){
			int id = -1;
			int idClass = -1;
			String desc = "";
			NodeList nl = node.getChildNodes();
			for(int i=0;i<nl.getLength();i++){
				Node n = nl.item(i);
				if(n.getNodeName().equals("Id")){ id = Integer.parseInt(n.getTextContent()); }
				else if(n.getNodeName().equals("IdClass")){ idClass = Integer.parseInt(n.getTextContent()); }
				else if(n.getNodeName().equals("Description")){ desc = n.getTextContent(); }
			}
			return new ReferenceType(id,idClass,desc);
		}
	};
	
	private int[] type;
	private CmdbAttrXMLParser(int type){
		this.type = new int[]{type};
	}
	private CmdbAttrXMLParser(int[] type){
		this.type = type;
	}
	private boolean contains(int t){
		for(int it:type){
			if(it==t) return true;
		}
		return false;
	}
	public static Object resolve(Node node) {
		if(node==null) return null;
		NamedNodeMap nnm=node.getAttributes();
		Node nt = nnm.getNamedItem("type");
		if(nt==null) return null;
		String value= nt.getNodeValue();
		if(value==null) return null;
		int type = Integer.parseInt(value);
		for(CmdbAttrXMLParser p : values()){
			if(p.contains(type)){
				return p.parse(node);
			}
		}
		return null;
	}
	public abstract Object parse(Node node);
}
