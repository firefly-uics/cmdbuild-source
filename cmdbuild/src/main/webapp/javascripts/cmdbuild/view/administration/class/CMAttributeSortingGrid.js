(function() {

	var tr = CMDBuild.Translation.administration.modClass.attributeProperties; // Path to translation

	Ext.define('CMDBuild.view.administration.class.CMAttributeSortingGrid', {
		extend: 'Ext.grid.Panel',

		filtering: false,

		initComponent: function() {
			this.store = Ext.create('Ext.data.Store', {
				fields: [
					CMDBuild.core.constants.Proxy.NAME,
					CMDBuild.core.constants.Proxy.DESCRIPTION,
					CMDBuild.core.constants.Proxy.ABSOLUTE_CLASS_ORDER,
					CMDBuild.core.constants.Proxy.CLASS_ORDER_SIGN
				],
				autoLoad: false,
				proxy: {
					type: 'ajax',
					url: 'services/json/schema/modclass/getattributelist',
					reader: {
						type: 'json',
						root: 'attributes'
					}
				},
				sorters: [
					{
						property: CMDBuild.core.constants.Proxy.ABSOLUTE_CLASS_ORDER,
						direction: 'ASC'
					}
				]
			});

			var comboOrderSign = Ext.create('Ext.form.field.ComboBox', {
				valueField: CMDBuild.core.constants.Proxy.VALUE,
				displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
				listClass: 'x-combo-list-small',
				typeAhead: true,
				triggerAction: 'all',
				selectOnTab: true,
				forceSelection: true,
				editable: false,

				queryMode: 'local',
				store: Ext.create('Ext.data.Store', {
					fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
					data: [
						{
							value: 1,
							description: tr.direction.asc
						},
						{
							value: -1,
							description: tr.direction.desc
						},
						{
							value: 0,
							description: tr.not_in_use
						}
					]
				})
			});

			this.columns = [
				{
					id: CMDBuild.core.constants.Proxy.ABSOLUTE_CLASS_ORDER,
					hideable: false,
					hidden: true,
					dataIndex: CMDBuild.core.constants.Proxy.ABSOLUTE_CLASS_ORDER
				},
				{
					id: CMDBuild.core.constants.Proxy.NAME,
					header: tr.name,
					dataIndex: CMDBuild.core.constants.Proxy.NAME,
					flex: 1
				},
				{
					id: CMDBuild.core.constants.Proxy.DESCRIPTION,
					header: tr.description,
					dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
					flex: 1
				},
				{
					header: tr.criterion,
					dataIndex: CMDBuild.core.constants.Proxy.CLASS_ORDER_SIGN,
					renderer: Ext.Function.bind(comboRender, this, [], true),
					flex: 1,
					editor: comboOrderSign
				}
			];

			this.plugins = [
				Ext.create('Ext.grid.plugin.CellEditing', {
					clicksToEdit: 1
				})
			];

			this.viewConfig = {
				plugins: {
					ptype: 'gridviewdragdrop',
					dragGroup: 'dd',
					dropGroup: 'dd'
				}
			};

			this.callParent(arguments);

			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.idClass);

			this.getStore().load({
				params: params
			});

			this.getEditor = function() {
				return comboOrderSign;
			};
		}
	});

	function comboRender(value, meta, record, rowIndex, colIndex, store) {
		if (value > 0) {
			return '<span>' + tr.direction.asc + '</span>';
		} else if (value < 0) {
			return '<span>' + tr.direction.desc + '</span>';
		} else {
			return '<span>' + tr.not_in_use + '</span>';
		}
	}

})();