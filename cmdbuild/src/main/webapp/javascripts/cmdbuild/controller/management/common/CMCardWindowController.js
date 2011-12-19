Ext.define("CMDBuild.controller.management.common.CMCardWindowController", {

	extend: "CMDBuild.controller.management.classes.CMBaseCardPanelController",

	mixins: {
		observable : "Ext.util.Observable"
	},

	/**
	 * conf: {
	 * 	entryType: id of the entry type,
	 *  card: id of the card,
	 *  cmEditMode: boolean
	 * }
	 * */
	constructor: function(view, conf) {
		if (typeof conf.entryType == "undefined") {
			return;
		}

		this.callParent([view]);
		this.onEntryTypeSelected(_CMCache.getEntryTypeById(conf.entryType));

		var me = this;
		this.mon(me.view, "show", function() {
			me.loadFields(conf.entryType, function() {
				if (conf.card) {
					me.loadCard(loadRemote=true, {
						Id: conf.card,
						IdClass: conf.entryType
					}, function(card) {
						me.card = card;
						me.view.loadCard(card);
						if (me.widgetControllerManager) {
							me.widgetControllerManager.buildControllers(card);
						}
						if (conf.cmEditMode) {
							me.view.editMode();
						} else {
							me.view.displayMode();
						}
					});
				} else {
					if (conf.cmEditMode) {
						me.view.editMode();
					} else {
						me.view.displayMode();
					}
				}
			});
		});
	},

	getForm: function() {
		return this.view.cardPanel.getForm();
	},

	onSaveCardClick: function() {
		var form = this.getForm(),
			params = this.buildSaveParams();

		this.beforeRequest(form);

		if (form.isValid()) {
			this.doFormSubmit(params);
		}
	},

	onAbortCardClick: function() {
		this.view.destroy();
	},

	onEntryTypeSelected: function(entryType) {
		this.callParent(arguments);
		this.view.setTitle(this.entryType.get("text"));
	},

	// private, overridden in subclasses
	buildSaveParams: function() {
		return {
			IdClass: this.entryType.get("id"),
			Id: this.card ? this.card.get("Id") : -1
		};
	},

	// private, overridden in subclasses
	onSaveSuccess: function(form, action) {
		CMDBuild.LoadMask.get().hide();
		_CMCache.onClassContentChanged(this.entryType.get("id"));
		this.view.destroy();
	},

	// template to override in subclass
	beforeRequest: Ext.emptyFn
});