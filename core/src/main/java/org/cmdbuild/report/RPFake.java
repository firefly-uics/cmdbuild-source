package org.cmdbuild.report;

import net.sf.jasperreports.engine.design.JRDesignParameter;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;

public class RPFake extends ReportParameter {
	
	public RPFake(String name) {
		JRDesignParameter jrParameter = new JRDesignParameter();
		jrParameter.setName(name);
		jrParameter.setDescription(name);
		setJrParameter(jrParameter);		
	}
	
	@Override
	public boolean isRequired() {
		return false;
	}
	
	@Override
	public CMAttributeType<?> getCMAttributeType() {
		return new StringAttributeType(100);
	}

}