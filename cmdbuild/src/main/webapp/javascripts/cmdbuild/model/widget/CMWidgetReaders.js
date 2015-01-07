Ext.define("CMDBuild.management.model.widget.ManageRelationConfigurationReader", {
	statics: {
		id: function(w) {
			return w.id;
		},
		domainName: function(w) {
			return w.domainName;
		},
		className: function(w) {
			return w.className;
		},
		cardCQLSelector: function(w) {
			return w.cardCQLSelector;
		},
		required: function(w) {
			return w.required;
		},
		multiSelection: function(w) {
			return w.multiSelection;
		},
		singleSelection: function(w) {
			return w.singleSelection;
		},
		canCreateRelation: function(w) {
			return w.canCreateRelation;
		},
		canModifyARelation: function(w) {
			return w.canModifyARelation;
		},
		canRemoveARelation: function(w) {
			return w.canRemoveARelation;
		},
		canCreateAndLinkCard: function(w) {
			return w.canCreateAndLinkCard;
		},
		canModifyALinkedCard: function(w) {
			return w.canCreateAndLinkCard;
		},
		canDeleteALinkedCard: function(w) {
			return w.canDeleteALinkedCard;
		},
		source: function(w) {
			return w.source;
		},
		label: function(w) {
			return w.label;
		}
	}
});

Ext.define("CMDBuild.controller.management.common.widgets.CMCalendarControllerWidgetReader", {
	getStartDate : function(w) {
		return w.startDate;
	},
	getEndDate : function(w) {
		return w.endDate;
	},
	getTitle : function(w) {
		return w.eventTitle;
	},
	getEventClass : function(w) {
		return w.eventClass;
	},
	getFilterVarName : function(w) {
		return "filter";
	},
	getDefaultDate : function(w) {
		return w.defaultDate;
	}
});