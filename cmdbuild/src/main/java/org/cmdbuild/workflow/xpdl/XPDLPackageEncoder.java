package org.cmdbuild.workflow.xpdl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.cmdbuild.workflow.utils.SimpleXMLDoc;
import org.cmdbuild.workflow.utils.SimpleXMLNode;
import org.cmdbuild.utils.StringUtils;
import org.cmdbuild.utils.StringUtils.Stringyfier;

public class XPDLPackageEncoder {

	static XPDLPackageEncoder instance = null;
	public static XPDLPackageEncoder getInstance() {
		if(instance == null){ instance = new XPDLPackageEncoder(); }
		return instance;
	}
	
	/**
	 * 
	 * @param processClassName
	 * @param user
	 * @param role
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public SimpleXMLDoc create(XPDLPackageDescriptor descriptor) {
		SimpleXMLDoc out = new SimpleXMLDoc("Package");
		putPackageInfo( descriptor.processClassName,out.getRoot() );
		putPackageHeader( out.getRoot() );
		out.getRoot().createChild("Script").put("Type", "text/java");
		putTypeDeclarations( out.getRoot() );
		SimpleXMLNode wfProcessesNode = out.getRoot().createChild("WorkflowProcesses");
		SimpleXMLNode processNode = createProcessTemplate(wfProcessesNode,descriptor.processClassName);
		
		List<XPDLAttribute> attrs = new ArrayList();
		attrs.add(new XPDLAttribute("ProcessId",XPDLAttributeType.INT));
		attrs.add(new XPDLAttribute("ProcessClass",XPDLAttributeType.STRING));
		attrs.add(new XPDLAttribute("ProcessCode",XPDLAttributeType.STRING));
		attrs.addAll(descriptor.attributes);
		putDataFields(processNode, attrs);

		putParticipants(processNode, descriptor.participants);
		
		List<XPDLApplicationDescriptor> apps = JSONApplicationReader.getApplications();
		apps.addAll(descriptor.applications);
		putApplications(processNode, apps);
		
		processNode.createChild("Activities").parent().createChild("Transitions");
		
		putProcessExtendedAttributes(processNode,descriptor.processClassName,descriptor.participants,descriptor.userStoppable);
		return out;
	}
	
	private String packId(String processClassName){
		return "Package_" + processClassName.toLowerCase();
	}
	private String procId(String processClassName){
		return "Process_" + processClassName.toLowerCase();
	}
	
	private void putPackageInfo( String processClassName,SimpleXMLNode packNode ) {
		packNode.put("xmlns","http://www.wfmc.org/2002/XPDL1.0")
		.put("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance")
		.put("xsi:schemaLocation","http://www.wfmc.org/2002/XPDL1.0 http://wfmc.org/standards/docs/TC-1025_schema_10_xpdl.xsd")
		.put("Id", packId(processClassName))
		.copy("Id","Name");
	}
	
	private void putPackageHeader( SimpleXMLNode packNode ) {
		packNode.createChild("PackageHeader")
		.createChild("XPDLVersion").set("1.0").parent()
		.createChild("Vendor").set("Togheter").parent()
		.createChild("Created").set( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()) );
	}
	
	private void putTypeDeclarations( SimpleXMLNode packNode ) {
		packNode.createChild("TypeDeclarations")
		.createChild("TypeDeclaration")
			.put("Id", "Lookup").copy("Id", "Name")
			.createChild("ExternalReference").put("location", "org.cmdbuild.workflow.type.LookupType")
			.parent().parent()
		.createChild("TypeDeclaration")
			.put("Id", "Reference").copy("Id", "Name")
			.createChild("ExternalReference").put("location", "org.cmdbuild.workflow.type.ReferenceType")
			.parent().parent()
		.createChild("TypeDeclaration")
			.put("Id", "Lookups").copy("Id", "Name")
			.createChild("ExternalReference").put("location", "org.cmdbuild.workflow.type.LookupType<>")
			.parent().parent()
		.createChild("TypeDeclaration")
			.put("Id", "References").copy("Id", "Name")
			.createChild("ExternalReference").put("location", "org.cmdbuild.workflow.type.ReferenceType<>");
	}
	
	private SimpleXMLNode createProcessTemplate( SimpleXMLNode wfProcessesNode, String processClassName ) {
		SimpleXMLNode out = wfProcessesNode.createChild("WorkflowProcess")
		.put("Id", procId(processClassName)).copy("Id","Name");
		
		out.createChild("ProcessHeader")
		.createChild("Created").set( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()) );
		
		return out;
	}
	
	private void putDataFields( SimpleXMLNode processNode, List<XPDLAttribute> attributes ) {
		SimpleXMLNode dataFields = processNode.createChild("DataFields");
		for(XPDLAttribute attr : attributes) {
			attr.putSimpleXML(dataFields);
		}
	}
	
	private void putParticipants( SimpleXMLNode processNode,
			List<XPDLParticipant> parts) {
		SimpleXMLNode participantsNode = processNode.createChild("Participants")
			.createChild("Participant")
				.put("Id", "System").copy("Id", "Name")
				.createChild("ParticipantType").put("Type", "SYSTEM")
			.parent().parent();
		for(XPDLParticipant part : parts) {
			part.putSimpleXML(participantsNode);
		}
	}
	
	private void putApplications( SimpleXMLNode processNode,
			List<XPDLApplicationDescriptor> applications) {
		SimpleXMLNode apps = processNode.createChild("Applications");
		for(XPDLApplicationDescriptor app : applications) {
			app.putSimpleXML(apps);
		}
	}
	
	private void putProcessExtendedAttributes( SimpleXMLNode processNode, 
			String processClassName,
			List<XPDLParticipant> parts,
			boolean userStoppable ) {
		SimpleXMLNode extAttrs = processNode.createChild("ExtendedAttributes");
		putExtAttr(extAttrs,"JaWE_GRAPH_END_OF_WORKFLOW","JaWE_GRAPH_PARTICIPANT_ID=System,CONNECTING_ACTIVITY_ID=,X_OFFSET=769,Y_OFFSET=82,JaWE_GRAPH_TRANSITION_STYLE=SIMPLE_ROUTING_BEZIER,TYPE=END_DEFAULT");
		putExtAttr(extAttrs,"JaWE_GRAPH_START_OF_WORKFLOW","JaWE_GRAPH_PARTICIPANT_ID=System,CONNECTING_ACTIVITY_ID=,X_OFFSET=74,Y_OFFSET=77,JaWE_GRAPH_TRANSITION_STYLE=SIMPLE_ROUTING_BEZIER,TYPE=START_DEFAULT");
		putExtAttr(extAttrs,"cmdbuildBindToClass",processClassName);
		putExtAttr(extAttrs,"userStoppable", Boolean.toString(userStoppable));
		
		//Add default SYSTEM participant
		parts.add(0,new XPDLParticipant("System",null));
		
		putExtAttr(extAttrs,"JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER",
				StringUtils.join(parts,";",new Stringyfier<XPDLParticipant>(){
					public String stringify(XPDLParticipant obj) {
						return obj.id;
					}
				}));
	}
	private void putExtAttr( SimpleXMLNode node,String name, String value ){
		node.createChild("ExtendedAttribute")
			.put("Name", name).put("Value", value);
	}
}
