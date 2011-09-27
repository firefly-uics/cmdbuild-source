(function() {
	var STATE = "state",
		STATE_VALUE_OPEN = "open.running",
		STATE_VALUE_SUSPENDED = "open.not_running.suspended",
		STATE_VALUE_COMPLETED = "closed.completed",
		STATE_VALUE_ALL = "all", // Not existent

		tr = CMDBuild.Translation.management.modworkflow;
	
	Ext.define("CMDBuild.view.management.workflow.CMActivityGrid", {
		extend: "CMDBuild.view.management.common.CMCardGrid",

		cmStoreUrl: "services/json/management/modworkflow/getactivitylist",
		
		constructor: function() {

			this.statusCombo = new  Ext.form.field.ComboBox({
				store: buildProcessStateStore.call(this),
				name : "state",
				hiddenName : "state",
				valueField : "code",
				displayField : "description",
				queryMode: "local",
				allowBlank : false,
				editable: false,
				value : STATE_VALUE_OPEN,
				isStateOpen: function() {
					return this.getValue() == STATE_VALUE_OPEN;
				}
			});

			this.addCardButton = new CMDBuild.AddCardMenuButton({
				classId: undefined,
				baseText: tr.add_card,
				textPrefix: tr.add_card
			});

			this.tbar = [this.addCardButton, this.statusCombo];

			this.callParent(arguments);
		},

		setStatusToOpen: function() {
			this.setStatus(STATE_VALUE_OPEN);
		},

		setStatus: function(value) {
			this.statusCombo.setValue(value);
			this.updateStatusParamInStoreProxyConfiguration();
		},

		updateStatusParamInStoreProxyConfiguration: function() {
			this.store.proxy.extraParams[STATE] = this.statusCombo.getValue();
		},

		getStoreExtraParams: function() {
			var ep = this.callParent(arguments);
			ep[STATE] = this.statusCombo.getValue();

			return ep;
		},

		// override
		_onGetPositionSuccessForcingTheFilter: function(p, position, resText) {
			this.setStatus(resText.FlowStatus);
			this.callParent(arguments);
		},

		// private and overridden in CMActivityGrid
		_onGetPositionFailureWithoutForcingTheFilter: function(resText) {
			var flowStatusOfSearchedCard = resText.FlowStatus;
			if (flowStatusOfSearchedCard == STATE_VALUE_COMPLETED) {
				this.fireEvent("processTerminated");
			} else {
				this.callParent(arguments);
			}
		},

		onEntrySelected: function(entry) {
			var id = entry.get("id");

			this.openFilterButton.enable();
			this.addCardButton.updateForEntry(entry);
			this.clearFilterButton.disable();

			this.updateStoreForClassId(id, {
				cb: function cbUpdateStoreForClassId() {
					this.loadPage(1, {
						cb: function cbLoadPage() {
							try {
								this.getSelectionModel().select(0);
							} catch (e) {/* if empty*/}
						}
					});
				}
			});
		}
	});

	function buildProcessStateStore() {
		var tr = CMDBuild.Translation.management.modworkflow.statuses;

		var wfStatuses = [
			[STATE_VALUE_OPEN, tr[STATE_VALUE_OPEN]],
			[STATE_VALUE_SUSPENDED, tr[STATE_VALUE_SUSPENDED]],
			[STATE_VALUE_COMPLETED, tr[STATE_VALUE_COMPLETED]],
			[STATE_VALUE_ALL, tr[STATE_VALUE_ALL]]
		];

		var store = Ext.create('Ext.data.ArrayStore', {
			autoDestroy: true,
			fields: [ "code", "description" ],
			data: wfStatuses
		});

		return store;
	}
})();