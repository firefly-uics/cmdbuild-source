(function() {
	var LOOKUP_FIELDS = CMDBuild.ServiceProxy.LOOKUP_FIELDS;
	var tr = CMDBuild.Translation.administration.modLookup.lookupGrid;
	
	Ext.define("CMDBuild.view.administration.lookup.CMLookupGrid", {
		extend: "Ext.grid.Panel",
		alias: "widget.lookupgrid",
		
		initComponent: function() {
			Ext.apply(this, {
				viewConfig: {
					loadMask: false,
					plugins : {
						ptype : 'gridviewdragdrop',
						dragGroup : 'dd',
						dropGroup : 'dd'
					},
					listeners : {
						scope: this,
						drop : function(node, data, dropRec, dropPosition) {
							this.fireEvent("cm_lookup_moved", {
								node: node,
								data: data,
								dropRec: dropRec,
								dropPosition: dropPosition
							});
						}
					}
				}
			});

			this.callParent(arguments);
		},

	constructor: function() {
		this.store = CMDBuild.ServiceProxy.lookup.getLookupGridStore();

		this.columns = [{
			hideable: false,
			hidden: true,
			dataIndex : LOOKUP_FIELDS.Index
		},{
			header : tr.code,
			dataIndex : LOOKUP_FIELDS.Code,
			flex: 1
		},{
			header: tr.description,
			dataIndex: LOOKUP_FIELDS.Description,
			flex: 2
		},{
			header : tr.parentdescription,
			dataIndex : LOOKUP_FIELDS.ParentDescription,
			flex: 2
		},
		new Ext.ux.CheckColumn( {
			header : tr.active,
			dataIndex : LOOKUP_FIELDS.Active,
			width: 90,
			cmReadOnly: true
		})];

		this.addButton = new Ext.button.Button({	
			iconCls : 'add',
			text : tr.add_lookup
		});

		this.tbar = [this.addButton];

		this.bbar = Ext.create('Ext.PagingToolbar', {
			store: this.store,
			displayInfo: true,
			displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of+' {2}',
			emptyMsg: CMDBuild.Translation.common.display_topic_none
		});

		this.callParent(arguments);
	},

	onSelectLookupType: function(lookupType) {
		if (lookupType) {
			this.lookupType = lookupType;
		}

		this.store.proxy.extraParams.type = this.lookupType.id;

		this.loadData();
	},

	loadData: function(lookupIdToSelectAfterLoad) {
		var sm;

		if (lookupIdToSelectAfterLoad) {
			sm = this.getSelectionModel();
		}

		if (this.lookupType) {
			this.store.load({
				callback: function() {
					if (lookupIdToSelectAfterLoad) {
						var selRecord = this.findRecord("Id", lookupIdToSelectAfterLoad);
						if (selRecord) {
							sm.select(selRecord);
						}
					}
				}
			});
		}
	}

});

})();