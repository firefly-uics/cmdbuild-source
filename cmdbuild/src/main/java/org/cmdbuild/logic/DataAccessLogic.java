package org.cmdbuild.logic;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
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

	public GetRelationListResponse getRelationList(int classId, int cardId) {
		return new GetRelationList(view).exec(classId, cardId);
	}
}
