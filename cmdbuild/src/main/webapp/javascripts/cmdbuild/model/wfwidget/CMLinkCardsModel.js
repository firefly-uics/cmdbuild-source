Ext.define("CMDBuild.Management.LinkCardsModel", {

	extend: "Ext.util.Observable",

	constructor: function(config) {
		config = config || {};
		this.selections = {};
		this._freezed = {};
		this.singleSelect = config.singleSelect;

		this.addEvents({
			"select" : true,
			"deselect" : true
		});

		this.callParent(arguments);
	},

	select: function(selection) {
		_debug("LinkCardsModel - select " + selection);
		if (this._silent) {
			return;
		}

		if (this.isSelected(selection)) {
			return;
		} else {
			if (this.singleSelect) {
				this.reset();
			}
			this.selections[selection] = true;
			this.fireEvent("select", selection);
		}
	},

	deselect : function(selection) {
		_debug("LinkCardsModel - deselect " + selection);
		if (this._silent) {
			return;
		}

		if (this.isSelected(selection)) {
			delete this.selections[selection];
			this.fireEvent("deselect", selection);
		}
	},

	getSelections : function() {
		var selections = [];
		for ( var selection in this.selections) {
			selections.push(selection);
		}
		return selections;
	},

	isSelected: function(selection) {
		return this.selections[selection];
	},

	freeze: function() {
		this._freezed = Ext.apply({}, this.selections);
	},

	defreeze: function() {
		this.selections = Ext.apply({}, this._freezed);
	},

	reset: function() {
		for (var selection in this.selections) {
			this.deselect(selection);
		}
	},

	hasSelection: function() {
		return this.getSelections().length > 0;
	},

	length: function() {
		return this.getSelections().length;
	}
});

// TODO move to a separate file with the readers

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
		outputName: function(w) {
			return w.outputName;
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