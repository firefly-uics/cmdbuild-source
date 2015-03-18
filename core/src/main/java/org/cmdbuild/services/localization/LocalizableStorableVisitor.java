package org.cmdbuild.services.localization;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.services.store.menu.MenuElement;

public interface LocalizableStorableVisitor {

	void visit(Lookup lookup);

	void visit(MenuElement menuElement);

}
