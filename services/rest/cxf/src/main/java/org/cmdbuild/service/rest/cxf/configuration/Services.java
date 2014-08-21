package org.cmdbuild.service.rest.cxf.configuration;

import org.cmdbuild.service.rest.cxf.CxfAttributes;
import org.cmdbuild.service.rest.cxf.CxfCards;
import org.cmdbuild.service.rest.cxf.CxfClassAttributes;
import org.cmdbuild.service.rest.cxf.CxfClasses;
import org.cmdbuild.service.rest.cxf.CxfDomains;
import org.cmdbuild.service.rest.cxf.CxfLookupTypes;
import org.cmdbuild.service.rest.cxf.CxfLookups;
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
		return new CxfAttributes(utilities.defaultErrorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory());
	}

	@Bean
	public CxfCards cxfCards() {
		return new CxfCards(utilities.defaultErrorHandler(), helper.userDataAccessLogic(), helper.systemDataView(),
				helper.userDataView());
	}

	@Bean
	public CxfClassAttributes cxfClassAttributes() {
		return new CxfClassAttributes(utilities.defaultErrorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory());
	}

	@Bean
	public CxfClasses cxfClasses() {
		return new CxfClasses(utilities.defaultErrorHandler(), helper.userDataAccessLogic(), helper.userWorkflowLogic());
	}

	@Bean
	public CxfDomains cxfDomains() {
		return new CxfDomains(helper.userDataAccessLogic());
	}

	@Bean
	public CxfLookups cxfLookups() {
		return new CxfLookups(helper.lookupLogic());
	}

	@Bean
	public CxfLookupTypes cxfLookupTypes() {
		return new CxfLookupTypes(helper.lookupLogic());
	}

	@Bean
	public CxfMenu cxfMenu() {
		return new CxfMenu(helper.currentGroupNameSupplier(), helper.menuLogic());
	}

}
