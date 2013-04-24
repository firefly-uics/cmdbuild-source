package org.cmdbuild.elements.interfaces;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.exception.NotFoundException;

@OldDao
@Deprecated
public interface ITableFactory {

	ITable create();
	ITable get(int classId) throws NotFoundException;
	ITable get(String className) throws NotFoundException;

	Iterable<ITable> list();
	Iterable<ITable> list(CMTableType type);

	TableTree tree();
	TableTree fullTree();
}
