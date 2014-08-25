package org.cmdbuild.service.rest.cxf.configuration;

import org.cmdbuild.service.rest.cxf.CxfAttributes;
import org.cmdbuild.service.rest.cxf.CxfCards;
import org.cmdbuild.service.rest.cxf.CxfClassAttributes;
import org.cmdbuild.service.rest.cxf.CxfClassCards;
import org.cmdbuild.service.rest.cxf.CxfClasses;
import org.cmdbuild.service.rest.cxf.CxfDomainAttributes;
import org.cmdbuild.service.rest.cxf.CxfDomains;
import org.cmdbuild.service.rest.cxf.CxfLookupTypeValues;
import org.cmdbuild.service.rest.cxf.CxfLookupTypes;
import org.cmdbuild.service.rest.cxf.CxfLookupValues;
import org.cmdbuild.service.rest.cxf.CxfMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Services {

	@Autowired
	private Helper helper;

	@Autowired
	private Utilities utilities;

	@Bean
	public CxfAttributes cxfAttributes() {
		return new CxfAttributes(utilities.defaultErrorHandler(), cxfClassAttributes(), cxfDomainAttributes());
	}

	@Bean
	public CxfCards cxfCards() {
		return new CxfCards(utilities.defaultErrorHandler(), cxfClassCards());
	}

	@Bean
	public CxfClassAttributes cxfClassAttributes() {
		return new CxfClassAttributes(utilities.defaultErrorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory());
	}

	@Bean
	public CxfClassCards cxfClassCards() {
		return new CxfClassCards(utilities.defaultErrorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.userDataView());
	}

	@Bean
	public CxfClasses cxfClasses() {
		return new CxfClasses(utilities.defaultErrorHandler(), helper.userDataAccessLogic(), helper.userWorkflowLogic());
	}

	@Bean
	public CxfDomainAttributes cxfDomainAttributes() {
		return new CxfDomainAttributes(utilities.defaultErrorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory());
	}

	@Bean
	public CxfDomains cxfDomains() {
		return new CxfDomains(utilities.defaultErrorHandler(), helper.userDataAccessLogic());
	}

	@Bean
	public CxfLookupTypes cxfLookupTypes() {
		return new CxfLookupTypes(helper.lookupLogic());
	}

	@Bean
	public CxfLookupTypeValues cxfLookupTypeValues() {
		return new CxfLookupTypeValues(helper.lookupLogic());
	}

	@Bean
	public CxfLookupValues cxfLookupValues() {
		return new CxfLookupValues(cxfLookupTypeValues());
	}

	@Bean
	public CxfMenu cxfMenu() {
		return new CxfMenu(helper.currentGroupNameSupplier(), helper.menuLogic());
	}

}
