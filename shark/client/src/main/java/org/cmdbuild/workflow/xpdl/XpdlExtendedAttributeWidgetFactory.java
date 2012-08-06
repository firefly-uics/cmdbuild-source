package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.workflow.CMActivity.CMActivityWidget;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttribute;


public interface XpdlExtendedAttributeWidgetFactory {

	CMActivityWidget createWidget(XpdlExtendedAttribute xa, CMValueSet processInstanceVariables);
}
