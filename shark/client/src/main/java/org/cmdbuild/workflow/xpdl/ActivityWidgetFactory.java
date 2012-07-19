package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.workflow.CMActivity.CMActivityWidget;

/**
 * Creates an activity widget given its serialization.
 */
public interface ActivityWidgetFactory {

	CMActivityWidget createWidget(String serialization);
}
