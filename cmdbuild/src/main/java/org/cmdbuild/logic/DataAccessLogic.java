package org.cmdbuild.logic;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.GetRelationList;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.services.DBService;

/**
 * Business Logic Layer for Data Access
 */
public class DataAccessLogic {

	final static DBDriver driver;
	final CMDataView view;

	static {
		driver = new PostgresDriver(DBService.getInstance().getDataSource());
	}

	public DataAccessLogic() {
		view = new DBDataView(driver); // TODO UserContextView()
	}

//	final CMDataView view;

//	public DataAccessBL(final CMDataView view) {
//		this.view = view;
//	}

	public GetRelationListResponse getRelationList(final Card src, final DomainWithSource dom) {
		Validate.notNull(src);
		return new GetRelationList(view).exec(src, dom);
	}
}
