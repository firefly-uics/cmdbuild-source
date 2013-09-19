package org.cmdbuild.services.bim.connector;

import javax.sql.DataSource;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.springframework.jdbc.core.JdbcTemplate;

public class BimMapper extends DefaultMapper {
	
	public BimMapper(CMDataView dataView, LookupLogic lookupLogic,
			DataSource dataSource) {
		super(dataView, BimMapperRules.INSTANCE, BimCardDiffer.buildBimCardDiffer(dataView, lookupLogic,
				new JdbcTemplate(dataSource)));
	}
}
