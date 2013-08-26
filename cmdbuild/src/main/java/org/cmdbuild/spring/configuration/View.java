package org.cmdbuild.spring.configuration;

import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.logic.widget.WidgetLogic;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class View {

	@Autowired
	private DBDataView systemDataView;

	@Bean
	public ViewConverter viewConverter() {
		return new ViewConverter(systemDataView);
	}

	@Bean
	@Scope("prototype")
	public WidgetLogic widgetLogic() {
		return new WidgetLogic(systemDataView);
	}

}
