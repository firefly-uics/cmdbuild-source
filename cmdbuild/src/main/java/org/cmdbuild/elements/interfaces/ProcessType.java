package org.cmdbuild.elements.interfaces;

import org.cmdbuild.common.Constants;
import org.cmdbuild.common.annotations.OldDao;

@OldDao
@Deprecated
public interface ProcessType extends ITable {

	static final String BaseTable = Constants.BASE_PROCESS_CLASS_NAME;

	@Override
	ProcessFactory cards();

}
