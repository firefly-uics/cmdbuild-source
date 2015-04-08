package org.cmdbuild.services.localization;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.model.view.View;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.store.menu.MenuElement;
import org.cmdbuild.services.store.FilterStore.Filter;

public interface LocalizableStorableVisitor {

	void visit(Lookup lookup);

	void visit(MenuElement menuElement);

	void visit(View view);

	void visit(Widget widget);

	void visit(Filter filter);

}
