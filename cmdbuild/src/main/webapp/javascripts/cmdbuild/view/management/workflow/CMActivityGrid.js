(function() {
	var STATE = "state",
		STATE_VALUE_OPEN = "open.running",
		STATE_VALUE_TERMINATED = "closed.terminated", 
		STATE_VALUE_ABORTED = "closed.aborted",

		tr = CMDBuild.Translation.management.modworkflow;
	
	Ext.define("CMDBuild.view.management.workflow.CMActivityGrid", {
		extend: "CMDBuild.view.management.common.CMCardGrid",

		cmStoreUrl: "services/json/management/modworkflow/getactivitylist",
		
		constructor: function() {

			this.statusCombo = new  Ext.form.field.ComboBox({
				store: buildStoreOfProcessState.call(this),
				name : 'stete',
				hiddenName : 'state',
				valueField : 'code',
				displayField : 'name',
				triggerAction: 'all',
				allowBlank : false,
				editable: false,
				grow: true
			});

			this.addCardButton = new CMDBuild.AddCardMenuButton({
				classId: undefined,
				baseText: tr.add_card,
				textPrefix: tr.add_card
			});

			this.tbar = [this.addCardButton, this.statusCombo];

			this.callParent(arguments);
		},

		updateStatusParamInStoreProxyConfiguration: function() {
			this.store.proxy.extraParams[STATE] = this.statusCombo.getValue();
		},

		setStatusToOpen: function() {
			this.statusCombo.setValue(STATE_VALUE_OPEN);
		},

		getStoreExtraParams: function() {
			var ep = this.callParent(arguments);
			ep[STATE] = this.statusCombo.getValue();

			return ep;
		},
		
		onEntrySelected: function(entry) {
			var id = entry.get("id");
			this.setStatusToOpen();
			this.addCardButton.updateForEntry(entry);
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
	
	function buildStoreOfProcessState() {
		var storeOfState = new Ext.data.JsonStore({
			fields : ['code', 'name', 'id'],
			proxy: {
				type: "ajax",
				url: "services/json/schema/modworkflow/statuses",
				reader: {
					type: "json",
					root: "rows"
				}
			},
			sorters: {
				property: 'code',
				direction: 'ASC'
			},
			autoLoad : true
		});

		storeOfState.on('load', function(store, records, options){
			for (var i = 0, l = records.length ; i<l ; i++) {        		
				if (records[i].data.code == STATE_VALUE_TERMINATED || records[i].data.code == STATE_VALUE_ABORTED) {
					store.remove(records[i]);
				} else {
					localizeStatusName(records[i], store);
				}
			}
		}, this);

		return storeOfState;
	}

	function localizeStatusName(record, storeOfState) {
		var code = record.data.code;
		storeOfState.remove(record);
		record.data.name = CMDBuild.Translation.management.modworkflow.statuses[code];
		storeOfState.add(record);
	};
})();