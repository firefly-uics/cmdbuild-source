(function() {
	var LOOKUP_FIELDS = CMDBuild.ServiceProxy.LOOKUP_FIELDS;
	var tr = CMDBuild.Translation.administration.modLookup.lookupGrid;
	
	Ext.define("CMDBuild.view.administration.lookup.CMLookupGrid", {
		extend: "Ext.grid.Panel",
		alias: "widget.lookupgrid",
		
//	initComponent:function() {

//	   
//	    this.on({
//	      render: this.ddRender,
//	      beforedestroy: function(g) { Ext.dd.ScrollManager.unregister(g.getView().getEditorParent()); }
//	    });

//  	},

	constructor: function() {
		var pageSize = parseInt(CMDBuild.Config.cmdbuild.referencecombolimit);
		this.store = CMDBuild.ServiceProxy.lookup.getLookupGridStore(pageSize);

		this.columns = [{
			hideable: false,
			hidden: true,
			dataIndex : LOOKUP_FIELDS.Index
		},{
			header: tr.description,
			dataIndex: LOOKUP_FIELDS.Description,
			flex: 1
		},{
			header : tr.code,
			dataIndex : LOOKUP_FIELDS.Code,
			flex: 1
		},{
			header : tr.parentdescription,
			dataIndex : LOOKUP_FIELDS.ParentDescription,
			flex: 1
		},{
			header : tr.active,
			dataIndex : LOOKUP_FIELDS.Active,
			width: 50
		}]

		this.addButton = new Ext.button.Button({	
			iconCls : 'add',
			text : tr.add_lookup
		});

		this.tbar = [this.addButton];

		this.bbar = Ext.create('Ext.PagingToolbar', {
			store: this.store,
			displayInfo: true,
			displayMsg: 'Displaying topics {0} - {1} of {2}',
			emptyMsg: "No topics to display"
		});

		this.callParent(arguments);
	},

	onSelectLookupType: function(lookupType) {
		if (lookupType) {
			this.lookupType = lookupType;
		}

		this.loadData();
	},
	
	ddRender: function(g) {
		var ddrow = new Ext.ux.dd.GridReorderDropTarget(g, {
			copy : false,
			listeners : {
				beforerowmove : function(objThis, oldIndex, newIndex, records) {
					var g = objThis.getGrid();
					var gStore = g.getStore();

					//change the number of moved record
					var recordMoved = records[0];
					var rowList = [{
						id: recordMoved.json.Id, 
						index: gStore.getAt(newIndex).json.Number
					}];

					//change the number of the records between the new and old index
					var fillRowList = function(firstIndex, lastIndex, direction) {
						for (var i=firstIndex; i<lastIndex; ++i) {
							var rec = gStore.getAt(i);
							var index = rec.json.Number;
							rowList.push({
								id: rec.json.Id, 
								index: index + direction
							});
						}
					};
					if (oldIndex > newIndex) {
						fillRowList(newIndex, oldIndex, 1);
					} else {
						fillRowList(oldIndex+1, newIndex+1, -1);
					}

					var savePosition = function(type, rowList) {
						CMDBuild.Ajax.request({
							url: 'services/json/schema/modlookup/reorderlookup',
							method: 'POST',
							params: {
								type: type ,
								lookuplist: Ext.util.JSON.encode(rowList)
							},
							callback: function(response,options) {
								gStore.load();
								g.getEl().unmask();
								g.getView().restoreScroll(scrollState);
							}
						});
					}(records[0].json.Type, rowList);
					
					//manage scrollState
					var scrollState = g.getView().getScrollState();
					g.getView().restoreScroll(scrollState);
		            
				}
			}
		});

		Ext.dd.ScrollManager.register(g.getView().getEditorParent());
	},
	
	loadData: function(lookupIdToSelectAfterLoad) {
		var sm;
		
		if (lookupIdToSelectAfterLoad) {
			sm = this.getSelectionModel();
		}
		
		if (this.lookupType) {
			this.store.load({
				params : {
					type: this.lookupType.id,
					start: 0,
					limit: this.pageSize
				}, 
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