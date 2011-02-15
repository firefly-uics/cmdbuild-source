package org.cmdbuild.servlets.resource.shark;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.logger.Log;
import org.cmdbuild.workflow.utils.SimpleXMLNode;
import org.cmdbuild.workflow.WorkflowAttributeType;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.w3c.dom.Node;

@SuppressWarnings("unchecked")
public enum XMLAttributeHelper {

	BOOLEAN(WorkflowAttributeType.BOOLEAN,Boolean.class) {
		@Override
		public void serialize(Object arg0, SimpleXMLNode arg1) {
			arg1.set(arg0);
		}
		@Override
		public void serialize(Object arg0, Element arg1) {
			arg1.setText(arg0.toString());
		}
		@Override
		public Object parse(Node arg0) {
			return Boolean.parseBoolean(arg0.getTextContent());
		}
	},
	STRING(WorkflowAttributeType.STRING,String.class){
		@Override
		public void serialize(Object arg0, SimpleXMLNode arg1) {
			arg1.set(arg0);
		}
		@Override
		public void serialize(Object arg0, Element arg1) {
			arg1.setText(arg0.toString());
		}
		@Override
		public Object parse(Node arg0) {
			return arg0.getTextContent();
		}
	},
	DOUBLE(WorkflowAttributeType.DOUBLE,Double.class){
		@Override
		public void serialize(Object arg0, SimpleXMLNode arg1) {
			arg1.set(arg0);
		}
		@Override
		public void serialize(Object arg0, Element arg1) {
			arg1.setText(arg0.toString());
		}
		@Override
		public Object parse(Node arg0) {
			return Double.parseDouble(arg0.getTextContent());
		}
	},
	INTEGER(WorkflowAttributeType.INTEGER,Integer.class){
		@Override
		public void serialize(Object arg0, SimpleXMLNode arg1) {
			arg1.set(arg0);
		}
		@Override
		public void serialize(Object arg0, Element arg1) {
			arg1.setText(arg0.toString());
		}
		@Override
		public Object parse(Node arg0) {
			return Integer.parseInt(arg0.getTextContent());
		}
	},
	LOOKUP(WorkflowAttributeType.LOOKUP,LookupType.class){
		@Override
		public void serialize(Object arg0, SimpleXMLNode arg1) {
			LookupType lkp = (LookupType)arg0;
			arg1.put("class", "org.cmdbuild.workflow.type.LookupType")
			.createChild("Id").set(lkp.getId()).parent()
			.createChild("Description").set(lkp.getDescription()).parent()
			.createChild("Type").set(lkp.getType());
		}
		@Override
		public void serialize(Object arg0, Element arg1) {
			LookupType lkp = (LookupType)arg0;
			arg1.addAttribute("class", "org.cmdbuild.workflow.type.LookupType");
			Element id,type,desc;
			
			id = DocumentHelper.createElement("Id");
			type = DocumentHelper.createElement("Type");
			desc = DocumentHelper.createElement("Description");
			
			id.setParent(arg1); type.setParent(arg1); desc.setParent(arg1);
			
			id.setText(lkp.getId() + "");
			type.setText(lkp.getType());
			desc.setText(lkp.getDescription());
		}
		@Override
		public Object parse(Node arg0) {
			return null;
		}
	},
	REFERENCE(WorkflowAttributeType.REFERENCE,ReferenceType.class){
		@Override
		public void serialize(Object arg0, SimpleXMLNode arg1) {
			ReferenceType ref = (ReferenceType)arg0;
			arg1.put("class", "org.cmdbuild.workflow.type.ReferenceType")
			.createChild("Id").set(ref.getId()).parent()
			.createChild("Description").set(ref.getDescription()).parent()
			.createChild("IdClass").set(ref.getIdClass());
		}
		@Override
		public void serialize(Object arg0, Element arg1) {
			ReferenceType ref = (ReferenceType)arg0;
			arg1.addAttribute("class", "org.cmdbuild.workflow.type.ReferenceType");
			Element id,idcl,desc;
			
			id = DocumentHelper.createElement("Id");
			idcl = DocumentHelper.createElement("IdClass");
			desc = DocumentHelper.createElement("Description");
			
			id.setParent(arg1); idcl.setParent(arg1); desc.setParent(arg1);
			
			id.setText(ref.getId() + "");
			idcl.setText(ref.getIdClass() + "");
			desc.setText(ref.getDescription());
		}
		@Override
		public Object parse(Node arg0) {
			return null;
		}
	},
	TIMESTAMP(WorkflowAttributeType.TIMESTAMP,Date.class){
		@Override
		public void serialize(Object arg0, SimpleXMLNode arg1) {
			Date dt = null;
			if(arg0 instanceof Date) {
				dt = (Date)arg0;
			} else if(arg0 instanceof Calendar) {
				dt = ((Calendar)arg0).getTime();
			}
			if(dt != null) {
				arg1.set(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(dt));
			}
		}
		@Override
		public void serialize(Object arg0, Element arg1) {
			Date dt = null;
			if(arg0 instanceof Date) {
				dt = (Date)arg0;
			} else if(arg0 instanceof Calendar) {
				dt = ((Calendar)arg0).getTime();
			}
			if(dt != null) {
				arg1.setText(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(dt));
			}
		}
		@Override
		public Object parse(Node arg0) throws Exception {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(arg0.getTextContent());
		}
	},
	DATE(WorkflowAttributeType.DATE,Date.class){
		@Override
		public void serialize(Object arg0, SimpleXMLNode arg1) {
			Date dt = null;
			if(arg0 instanceof Date) {
				dt = (Date)arg0;
			} else if(arg0 instanceof Calendar) {
				dt = ((Calendar)arg0).getTime();
			}
			if(dt != null) {
				arg1.set(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(dt));
			}
		}
		@Override
		public void serialize(Object arg0, Element arg1) {
			Date dt = null;
			if(arg0 instanceof Date) {
				dt = (Date)arg0;
			} else if(arg0 instanceof Calendar) {
				dt = ((Calendar)arg0).getTime();
			}
			if(dt != null) {
				arg1.setText(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(dt));
			}
		}
		@Override
		public Object parse(Node arg0) throws Exception {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(arg0.getTextContent());
		}
	};
	
	WorkflowAttributeType wat;
	Class parsed;
	private XMLAttributeHelper(WorkflowAttributeType wat,Class parsed){
		this.wat = wat;
		this.parsed = parsed;
	}
	public abstract void serialize( Object value, Element node );
	public abstract void serialize( Object value, SimpleXMLNode node);
	public abstract Object parse( Node node ) throws Exception;
	
	public static void serializeValue( AttributeValue av, SimpleXMLNode node ) {
		for(XMLAttributeHelper xas :  values()) {
			if(xas.wat.isType(av)) {
				xas.serialize(xas.wat.get(av.getObject()), node);
			}
		}
	}
	public static Object parseNode( Node node ) throws Exception{
		int type = Integer.parseInt(node.getAttributes().getNamedItem("type").getNodeValue());
		for(XMLAttributeHelper xah : values()) {
			if(xah.wat.getSharkType() == type) {
				return xah.parse(node);
			}
		}
		return null;
	}
	public static void serializeObject(Object obj, SimpleXMLNode node) {
		if(obj == null){
			Log.WORKFLOW.debug("object to serialize in xml null!");
			return;
		}
		for(XMLAttributeHelper xah : values()) {
			if(xah.parsed.equals(obj.getClass())) {
				node.put("type", xah.wat.getSharkType());
				xah.serialize(obj, node);
			}
		}
	}
	public static void serializeObject(Object obj, Element node) {
		if(obj == null){
			Log.WORKFLOW.debug("object to serialize in xml null!");
			return;
		}
		for(XMLAttributeHelper xah : values()) {
			if(xah.parsed.equals(obj.getClass())) {
				node.addAttribute("type", xah.wat.getSharkType() + "");
				xah.serialize(obj, node);
			}
		}
	}
}
