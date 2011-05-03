(function() {
	Ext.ns("CMDBuild.administration.domain");

	var translation = CMDBuild.Translation.administration.modClass.attributeProperties;

	CMDBuild.administration.domain.CMDomainAttributeGrid = Ext.extend(Ext.grid.GridPanel, {
		remoteSort: false,
		filtering: false,

		initComponent:function() {
			var fakeStore = new Ext.data.JsonStore({
				url: "",
				root: "rows",
				totalProperty: 'results',
				fields: [],
				remoteSort: true
			});

			this.addButton = new Ext.Button({	
				iconCls: 'add',
				text: translation.add_attribute
			});
			
			this.frame = false;
			this.border = false;
			this.tbar = [this.addButton];
			this.columns = getColumns();
			this.store = fakeStore;
			this.viewConfig = { forceFit: true };
			CMDBuild.administration.domain.CMDomainAttributeGrid.superclass.initComponent.apply(this, arguments);
		},
		changeStore: function(store) {
			var cm = getColumnModel();
			this.reconfigure(store, cm);
		}
	});
	
	function getColumns() {
		return [{
			id: 'name',
			header: translation.name,
			dataIndex: 'name'
		}, {
			id: 'description',
			header: translation.description,
			dataIndex: 'description'
		}, {
			header: translation.type,
			dataIndex: 'type'
		},
		new Ext.grid.CheckColumn( {
			header: translation.isbasedsp,
			dataIndex: 'shownAsGridColumn'
		}),
		new Ext.grid.CheckColumn( {
			header: translation.isunique,
			dataIndex: 'unique'
		}),
		new Ext.grid.CheckColumn( {
			header: translation.isnotnull,
			dataIndex: 'notnull'
		}),
		new Ext.grid.CheckColumn( {
			header: translation.isactive,
			dataIndex: 'active'
		}), {
			header: translation.field_visibility,
			dataIndex: 'editingMode',
			renderer: renderEditingMode 
		}];
	}
	
	function getColumnModel() {
		return new Ext.grid.ColumnModel({
			columns: getColumns()
		});
	}
	
	function renderEditingMode(val) {
		return translation["field_" + val];
	}
})();