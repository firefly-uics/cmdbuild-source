package org.cmdbuild.elements.interfaces;

import org.cmdbuild.elements.TableTree;
import org.cmdbuild.exception.NotFoundException;

public interface ProcessTypeFactory {

	ProcessType create();
	ProcessType get(int classId) throws NotFoundException;
	ProcessType get(String className) throws NotFoundException;

	Iterable<ProcessType> list();
	TableTree tree();
}
