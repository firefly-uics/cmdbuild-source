package org.cmdbuild.portlet.layout.widget;

import org.cmdbuild.services.soap.WorkflowWidgetDefinition;

public enum WWType {

	createModifyCard {
		@Override
		public WorkflowWidget createWidget(final WorkflowWidgetDefinition d) {
			return new CreateModifyCardWidget(d);
		}
	},
	openNotes {
		@Override
		public WorkflowWidget createWidget(final WorkflowWidgetDefinition d) {
			return new OpenNotesWidget(d);
		}
	},
	openAttachment {
		@Override
		public WorkflowWidget createWidget(final WorkflowWidgetDefinition d) {
			return new OpenAttachmentWidget(d);
		}
	},
	createReport {
		@Override
		public WorkflowWidget createWidget(final WorkflowWidgetDefinition d) {
			return new CreateReportWidget(d);
		}
	},
	linkCards {
		@Override
		public WorkflowWidget createWidget(final WorkflowWidgetDefinition d) {
			return new LinkCardsWidget(d);
		}
	};

	public static WorkflowWidget create(final WorkflowWidgetDefinition d) {
		final WWType e = WWType.valueOf(d.getType());
		return e.createWidget(d);
	}

	abstract public WorkflowWidget createWidget(WorkflowWidgetDefinition d);
}