(function() {
	var ATTR_TO_SKIP = "Notes";
	var translation = CMDBuild.Translation.administration.modClass.attributeProperties;
	
Ext.define("CMDBuild.view.administration.classes.CMAttributeGrid", {
	extend: "Ext.grid.Panel",
	alias: "attributegrid",

	remoteSort: false,
	filtering: false,
	eventtype : 'class', 

	hideNotNull: false, // for processes
	
	hideMode: "offsets",

	constructor:function() {

		this.addAttributeButton = new Ext.button.Button( {
			iconCls : 'add',
			text : translation.add_attribute
		});

		this.orderButton = new Ext.button.Button({	
			iconCls : 'order',
			text : translation.set_sorting_criteria
		});

		this.inheriteFlag = new Ext.form.Checkbox({
			boxLabel : CMDBuild.Translation.administration.modClass.include_inherited,
			checked : true,
			scope : this,
			handler : function(obj, checked) {
				this.filterInherited(!checked);
			}
		});

		this.buildStore();
		this.buildColumnConf();
		this.buildTBar();

		this.callParent(arguments);
	},
	
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
					beforedrop: function() {
						// it is not allowed to reorder the attribute if
						// there are also the inherited attrs
						return this.inheriteFlag.checked;
					},
					drop : function(node, data, dropRec, dropPosition) {
						this.fireEvent("cm_attribute_moved", arguments);
					}
				}
			}
		});
		
		this.callParent(arguments);
		
		this.getStore().on('load', function(store, records, opt) {
			this.filterInherited(this.filtering);
			for (var i=0, l=records.length; i<l; ++i) {
				var r = records[i];
				if (r.data.name == ATTR_TO_SKIP) {
                    try {
                        store.removeAt(i);
                    } catch (e) {
                        // sometimes there is a problem with afterRemove and I don't know why
                        _debug("fail removing a record to attributes grid", r);
                    }
				}
			}
		}, this);
	},
	
	// private
	buildColumnConf: function() {
		this.columns = [ {
			hideable : false,
			hidden : true,
			dataIndex : 'index',
			flex: 1
		}, {
			header : translation.name,
			dataIndex : 'name',
			flex: 1
		}, {
			header : translation.description,
			dataIndex : 'description',
			flex: 1
		}, {
			header : translation.type,
			dataIndex : 'type',
			flex: 1
		},
		new Ext.ux.CheckColumn( {
			header : translation.isbasedsp,
			dataIndex : 'isbasedsp',
			cmReadOnly: true
		}),
		new Ext.ux.CheckColumn( {
			header : translation.isunique,
			dataIndex : 'isunique',
			cmReadOnly: true
		}),
		new Ext.ux.CheckColumn( {
			header : translation.isnotnull,
			dataIndex : 'isnotnull',
			cmReadOnly: true
		}),
		new Ext.ux.CheckColumn( {
			header : translation.inherited,
			hidden : true,
			dataIndex : 'inherited',
			cmReadOnly: true
		}), 
		new Ext.ux.CheckColumn( {
			header : translation.isactive,
			dataIndex : 'isactive',
			cmReadOnly: true
		}), {
			header : translation.field_visibility,
			dataIndex : 'fieldmode',
			renderer : renderEditingMode,
			flex: 1
		}, {
			header : translation.group,
			dataIndex : 'group',
			hidden : true,
			flex: 1
		}];
	},
	
	buildStore: function() {
		this.store = new Ext.data.Store({
			fields: [
				"index", "name", "description", "type", "isunique",
				"isbasedsp", "isnotnull","inherited", 'fieldmode',
				'isactive', "group"
			],
			autoLoad : false,
			proxy : {
				type : 'ajax',
				url : 'services/json/schema/modclass/getattributelist',
				reader : {
					type : 'json',
					root : 'rows'
				}
			},
			sorters : [ {
				property : 'index',
				direction : "ASC"
			}]
		});
	},
	
	buildTBar: function() {
		this.tbar = [this.addAttributeButton, this.orderButton, '->', this.inheriteFlag ];
	},
	
	onClassSelected: function(idClass) {
		this.refreshStore(idClass, idAttributeToSelectAfter = null)
	},

	refreshStore: function(idClass, indexAttributeToSelectAfter) {
		var sm = this.getSelectionModel();

		this.store.load({
			params: {
				idClass : idClass || -1
			},
			scope: this,
			callback: function(records, opt, success) {
				this.filterInherited(this.filtering);
                if (this.rendered) {
                    this.selectRecordAtIndexOrTheFirst(indexAttributeToSelectAfter);
                }
            }
		});
	},

	filterInherited: function(filter) {
		this.filtering = filter;
		if (filter) {
			this.getStore().filterBy(function(record){return ! record.get("inherited")});
		} else {
			this.getStore().filterBy(function(record){return true});
		}
	},

	selectFirstRow: function() {
		var _this = this;
		Ext.Function.defer(function() {
			if (_this.store.getCount() > 0 && _this.isVisible()) {
				var sm = _this.getSelectionModel();
				if (! sm.hasSelection()) {
					sm.select(0);
				}
			}
		}, 200);
	},
	
    selectRecordAtIndexOrTheFirst: function (indexAttributeToSelectAfter) {
        if (indexAttributeToSelectAfter) {
            var r = this.store.findRecord("index", indexAttributeToSelectAfter);
            if (r) {
                this.getSelectionModel().select(r);
            }
        } else {
            try {
                if (this.store.count() != 0) {
                    this.getSelectionModel().select(0);
                }
            } catch (e) {
                // fail if the grid is not rendered	
            }
        }
    },

	onAddAttributeClick: function() {
		this.getSelectionModel().deselectAll();
	}
});

function renderEditingMode(val) {
	return translation["field_" + val];
}

})();