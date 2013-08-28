package org.cmdbuild.spring.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.servlets.json.serializers.CardSerializer;
import org.cmdbuild.servlets.json.serializers.ClassSerializer;
import org.cmdbuild.servlets.json.serializers.DomainSerializer;
import org.cmdbuild.servlets.json.serializers.RelationAttributeSerializer;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Serialization {

	@Autowired
	private LookupStore lookupStore;

	@Autowired
	private CMDataView systemDataView;

	@Autowired
	private SystemDataAccessLogicBuilder systemDataAccessLogicBuilder;

	@Autowired
	private SystemWorkflowLogicBuilder systemWorkflowLogicBuilder;

	@Autowired
	private UserStore userStore;

	@Bean
	public CardSerializer cardSerializer() {
		return new CardSerializer(systemDataAccessLogicBuilder, relationAttributeSerializer());
	}

	@Bean
	@Scope("prototype")
	public ClassSerializer classSerializer() {
		return new ClassSerializer(systemDataView, systemWorkflowLogicBuilder, userStore.getUser()
				.getPrivilegeContext());
	}

	@Bean
	@Scope("prototype")
	public DomainSerializer domainSerializer() {
		return new DomainSerializer(systemDataView, userStore.getUser().getPrivilegeContext());
	}

	@Bean
	public RelationAttributeSerializer relationAttributeSerializer() {
		return new RelationAttributeSerializer(lookupStore);
	}

}
