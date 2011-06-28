(function() {

Ext.define("CMDBuild.Administration.AttributeSortingGrid", {
	extend: "Ext.grid.Panel",
	translation: CMDBuild.Translation.administration.modClass.attributeProperties,
	filtering: false,

	initComponent:function() {

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
				property : 'absoluteClassOrder',
				direction : "ASC"
			}]
		});

		this.columns = [
			{
				id : 'index',
				hideable : false,
				hidden : true,
				dataIndex : 'index'
			},
			{
				id : 'absoluteClassOrder',
				hideable : false,
				hidden : true,
				dataIndex : 'absoluteClassOrder'
			},
			{
				id : 'name',
				header : this.translation.name,
				dataIndex : 'name',
				flex: 1
			},
			{
				id : 'description',
				header : this.translation.description,
				dataIndex : 'description',
				flex: 1
			},
			{
				header : this.translation.criterion,
				dataIndex : 'classOrderSign',
				renderer : Ext.Function.bind(comboRender, this, [], true),
				flex: 1,
				field: {
					xtype : 'combobox',
					typeAhead : true,
					triggerAction : 'all',
					selectOnTab : true,
					store : [
						[1,this.translation.direction.asc ],
						[-1,this.translation.direction.desc ],
						[0,this.translation.not_in_use ]
					],
					lazyRender : true,
					listClass : 'x-combo-list-small'
				}
			}
		];
		
		this.plugins = [Ext.create('Ext.grid.plugin.CellEditing', {
			clicksToEdit: 1
		})];

		this.viewConfig = {
			plugins : {
				ptype : 'gridviewdragdrop',
				dragGroup : 'dd',
				dropGroup : 'dd'
			}
		}

		this.callParent(arguments);

		this.getStore().load({
			params: {
				idClass : this.idClass
			}
		});
	}
});

function comboRender(value, meta, record, rowIndex, colIndex, store) {
	if (value > 0)
		return '<span>'+ this.translation.direction.asc +'</span>';
	else if (value < 0)
		return '<span>'+ this.translation.direction.desc +'</span>';
	else
		return '<span>'+ this.translation.not_in_use +'</span>';
}

})();