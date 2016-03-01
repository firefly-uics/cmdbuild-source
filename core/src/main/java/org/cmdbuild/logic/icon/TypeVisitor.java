package org.cmdbuild.logic.icon;

import org.cmdbuild.logic.icon.Types.ClassType;

public interface TypeVisitor {

	void visit(ClassType type);

}
