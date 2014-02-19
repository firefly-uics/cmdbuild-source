package org.cmdbuild.cmdbf;

import org.dmtf.schemas.cmdbf._1.tns.servicedata.QNameType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RelationshipType;

public class CMDBfRelationship extends CMDBfItem {
	
	private CMDBfId source;
	private CMDBfId target;
	
	public CMDBfRelationship(CMDBfId id, CMDBfId source, CMDBfId target) {
		super(id);
		setSource(source);
		setTarget(target);
	}
	
	public CMDBfRelationship(RelationshipType relationship) {
		super(relationship.getInstanceId());
		setSource(new CMDBfId(relationship.getSource()));
		setTarget(new CMDBfId(relationship.getTarget()));
		for(RecordType record : relationship.getRecord())
			records().add(record);
		for(QNameType type : relationship.getAdditionalRecordType())
			additionalRecordTypes().add(type);
	}

	public CMDBfId getSource() {
		return source;
	}

	public void setSource(CMDBfId source) {
		this.source = source;
	}

	public CMDBfId getTarget() {
		return target;
	}

	public void setTarget(CMDBfId target) {
		this.target = target;
	}
}
