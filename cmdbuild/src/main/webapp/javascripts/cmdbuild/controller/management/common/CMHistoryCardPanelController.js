(function() {
	Ext.define("CMDBuild.controller.management.classes.CMCardHistoryPanelController", {
		extend: "CMDBuild.controller.management.classes.CMModCardSubController",

		onEntryTypeSelected: function(entryType) {
			this.callParent(arguments);
			this.view.disable();
		},

		onCardSelected: function(card) {
			this.callParent(arguments);

			if (card) {
				if (this.entryType.get("tableType") != CMDBuild.Constants.cachedTableType.simpletable) {
					var existingCard = (!!this.card);
					this.view.setDisabled(!existingCard);

					if (this.view.tabIsActive(this.view)) {
						this.load();
					} else {
						this.mon(this.view, "activate", this.load, this, {single: true});
					}
				} else {
					this.view.disable();
				}
			}
		},

		onAddCardButtonClick: function() {
			this.view.disable();
		},

		load: function() {
			var me = this;
			this.view.getStore().load({
				params : {
					IdClass: me.card.get("IdClass"),
					Id: me.card.get("Id")
				}
			});
		}
	});

	Ext.define("CMDBuild.controller.management.workflow.CMWorkflowHistoryPanelController", {

		extend: "CMDBuild.controller.management.classes.CMCardHistoryPanelController",

		mixins: {
			wfStateDelegate: "CMDBuild.state.CMWorkflowStateDelegate"
		},

		constructor: function() {
			this.callParent(arguments);
			_CMWFState.addDelegate(this);

			this.mon(this.view, "activate", function() {
				if (!this._loaded) {
					this.load();
				}
			}, this);
		},

		// wfStateDelegate
		onProcessInstanceChange: function(processInstance) {
			this._loaded = false;

			if (processInstance.isNew()) {
				this.view.disable();
			} else {
				this.view.enable();
			}

			if (this.view.isVisible()) {
				this.load();
			}
		},

		// override
		load: function() {
			this._loaded = true;
			var processInstance = _CMWFState.getProcessInstance();
			if (processInstance) {
				this.view.getStore().load({
					params : {
						IdClass: processInstance.get("classId"),
						Id: processInstance.get("id")
					}
				});
			}
		},

		// override
		buildCardModuleStateDelegate: Ext.emptyFn,
		onEntryTypeSelected: Ext.emptyFn,
		onCardSelected: Ext.emptyFn
	});
})();