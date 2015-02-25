package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.logic.widget.WidgetLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Widget {

	@Autowired
	private Data data;

	@Bean
	@Scope(PROTOTYPE)
	public WidgetLogic widgetLogic() {
		return new WidgetLogic(data.systemDataView());
	}

}
