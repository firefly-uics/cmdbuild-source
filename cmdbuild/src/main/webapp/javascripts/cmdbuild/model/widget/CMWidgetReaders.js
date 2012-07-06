Ext.define("CMDBuild.management.model.widget.LinkCardsConfigurationReader", {
	statics: {
		id: function(w) {
			return w.id;
		},
		filter: function(w) {
			return w.filter;
		},
		className: function(w) {
			return w.className;
		},
		defaultSelection: function(w) {
			return w.defaultSelection;
		},
		readOnly: function(w) {
			return w.readOnly;
		},
		singleSelect: function(w) {
			return w.singleSelect;
		},
		allowCardEditing: function(w) {
			return w.allowCardEditing;
		},
		required: function(w) {
			return w.required;
		},
		enableMap: function(w) {
			return w.enableMap;
		},
		mapLatitude: function(w) {
			return w.mapLatitude;
		},
		mapLongitude: function(w) {
			return w.mapLongitude;
		},
		mapZoom: function(w) {
			return w.mapZoom;
		},
		label: function(w) {
			return w.label;
		},
		templates: function(w) {
			return w.templates || {};
		}
	}
});


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

Ext.define("CMDBuild.management.model.widget.ManageEmailConfigurationReader", {
	statics: {
		FIELDS: {
			ID: 'Id',
			STATUS: 'EmailStatus_value',
			BEGIN_DATE: 'BeginDate',
			FROM_ADDRESS: 'FromAddress',
			TO_ADDRESS: 'toAddresses',
			CC_ADDRESS: 'ccAddresses',
			SUBJECT: 'subject',
			CONTENT: 'content',
			CONDITION: 'condition'
		},

		id: function(w) {
			return w.id;
		},
		required: function(w) {
			return w.required;
		},
		readOnly: function(w) {
			return w.readOnly;
		},
		label: function(w) {
			return w.label;
		},
		templates: function(w) {
			return w.templates || {};
		},
		emailTemplates: function(w) {
			return w.emailTemplates || {};
		}
	}
});