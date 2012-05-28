package org.cmdbuild.workflow.xpdl;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class XPDLPackageDescriptor {
	boolean userStoppable;
	String processClassName;
	List<XPDLAttribute> attributes;
	List<XPDLParticipant> participants;
	List<XPDLApplicationDescriptor> applications;

	public XPDLPackageDescriptor() {
		userStoppable = false;
		processClassName = "";
		attributes = new LinkedList<XPDLAttribute>();
		participants = new LinkedList<XPDLParticipant>();
		applications = new LinkedList<XPDLApplicationDescriptor>();
	}
	
	public XPDLPackageDescriptor(boolean userStoppable,
			String processClassName, List<XPDLAttribute> attributes,
			List<XPDLParticipant> participants,
			List<XPDLApplicationDescriptor> applications) {
		super();
		this.userStoppable = userStoppable;
		this.processClassName = processClassName;
		this.attributes = attributes;
		this.participants = participants;
		this.applications = applications;
	}

	public boolean isUserStoppable() {
		return userStoppable;
	}

	public void setUserStoppable(boolean userStoppable) {
		this.userStoppable = userStoppable;
	}

	public String getProcessClassName() {
		return processClassName;
	}

	public void setProcessClassName(String processClassName) {
		this.processClassName = processClassName;
	}

	public List<XPDLAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<XPDLAttribute> attributes) {
		this.attributes = attributes;
		Collections.sort(this.attributes, new Comparator<XPDLAttribute>() {
			public int compare(XPDLAttribute o1, XPDLAttribute o2) {
				return o1.id.compareTo(o2.id);
			}
		});
	}

	public List<XPDLParticipant> getParticipants() {
		return participants;
	}

	public void setParticipants(List<XPDLParticipant> participants) {
		this.participants = participants;
	}

	public List<XPDLApplicationDescriptor> getApplications() {
		return applications;
	}

	public void setApplications(List<XPDLApplicationDescriptor> applications) {
		this.applications = applications;
	}
	
	public void addParticipant(XPDLParticipant part) {
		this.participants.add(part);
	}
	public void addAttribute(XPDLAttribute attr) {
		this.attributes.add(attr);
	}
	public void addApplication(XPDLApplicationDescriptor app) {
		this.applications.add(app);
	}
	
}
