package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.logic.filter.DefaultFilterLogic;
import org.cmdbuild.logic.filter.FilterLogic;
import org.cmdbuild.services.localization.LocalizedStorableConverter;
import org.cmdbuild.services.store.DataViewFilterStore;
import org.cmdbuild.services.store.FilterConverter;
import org.cmdbuild.services.store.FilterStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Filter {

	@Autowired
	private Data data;

	@Autowired
	private Report report;

	@Autowired
	private Translation translation;

	@Autowired
	private UserStore userStore;

	@Bean
	@Scope(PROTOTYPE)
	public FilterLogic defaultFilterLogic() {
		return new DefaultFilterLogic(dataViewFilterStore());
	}

	@Bean
	@Scope(PROTOTYPE)
	protected DataViewFilterStore dataViewFilterStore() {
		return new DataViewFilterStore(data.systemDataView(), operationUser(), converter());
	}

	private StorableConverter<FilterStore.Filter> converter() {
		return new LocalizedStorableConverter<FilterStore.Filter>(baseConverter(), translation.translationFacade(),
				data.systemDataView(), report.reportLogic());
	}

	private StorableConverter<org.cmdbuild.services.store.FilterStore.Filter> baseConverter() {
		return new FilterConverter(data.systemDataView());
	}

	@Bean
	@Scope(PROTOTYPE)
	protected OperationUser operationUser() {
		return userStore.getUser();
	}

}
