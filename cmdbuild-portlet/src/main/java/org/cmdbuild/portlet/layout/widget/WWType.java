package org.cmdbuild.portlet.layout.widget;

import org.cmdbuild.services.soap.WorkflowWidgetDefinition;

public enum WWType {
    
    createModifyCard {
        public WorkflowWidget createWidget(WorkflowWidgetDefinition d) {
            return new CreateModifyCardWidget(d);
        }
    },
    openNotes {
        public WorkflowWidget createWidget(WorkflowWidgetDefinition d) {
            return new OpenNotesWidget(d);
        }
    },
    openAttachment {
        public WorkflowWidget createWidget(WorkflowWidgetDefinition d) {
            return new OpenAttachmentWidget(d);
        }
    },
    createReport {
        public WorkflowWidget createWidget(WorkflowWidgetDefinition d) {
            return new CreateReportWidget(d);
        }
    },
    linkCards {
        public WorkflowWidget createWidget(WorkflowWidgetDefinition d) {
            return new LinkCardsWidget(d);
        }
    };

    public static WorkflowWidget create(WorkflowWidgetDefinition d) {
        WWType e = WWType.valueOf(d.getType());
        return e.createWidget(d);
    }

    abstract public WorkflowWidget createWidget(WorkflowWidgetDefinition d);
}