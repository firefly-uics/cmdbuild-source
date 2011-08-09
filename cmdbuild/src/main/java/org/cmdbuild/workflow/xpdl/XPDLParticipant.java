package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.workflow.utils.SimpleXMLNode;
import org.cmdbuild.workflow.CmdbuildProcessInfo;

public class XPDLParticipant {

	String id;
	CmdbuildProcessInfo.PerformerType performerType;
	
	public XPDLParticipant( String id, CmdbuildProcessInfo.PerformerType type ) {
		this.id = id;
		this.performerType = type;
	}
	
	public void putSimpleXML( SimpleXMLNode parent ) {
		parent.createChild("Participant")
		.put("Id", id).copy("Id", "Name")
		.createChild("ParticipantType").put("Type", this.performerType.name());
	}
}
