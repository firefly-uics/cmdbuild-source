(function() {

	Ext.define('CMDBuild.view.administration.widget.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.widget.DefinitionGrid'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.Widget}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmborderbottom',
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				viewConfig: {
					plugins: {
						ptype: 'gridviewdragdrop',
						dragGroup: 'widgetsDDGroup',
						dropGroup: 'widgetsDDGroup'
					},
					listeners: {
						scope: this,
						drop: function(node, data, overModel, dropPosition, eOpts) {
							this.delegate.cmfg('onClassTabWidgetItemDrop');
						}
					}
				},
				columns: [
					{
						dataIndex: CMDBuild.core.constants.Proxy.TYPE,
						text: CMDBuild.Translation.type,
						sortable: false,
						flex: 1,

						renderer: function(value) {
							switch (value) {
								case '.Calendar':
									return CMDBuild.Translation.calendar;

								case '.CreateModifyCard':
									return CMDBuild.Translation.createModifyCard;

								case '.OpenReport':
									return CMDBuild.Translation.createReport;

								case '.Ping':
									return CMDBuild.Translation.ping;

								case '.Workflow':
									return CMDBuild.Translation.startWorkflow;

								default:
									return value;
							}
						}
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.LABEL,
						text: CMDBuild.Translation.buttonLabel,
						sortable: false,
						flex: 2
					},
					Ext.create('Ext.ux.grid.column.Active', {
						dataIndex: CMDBuild.core.constants.Proxy.ACTIVE,
						text: CMDBuild.Translation.active,
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true
					})
				],
				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.widget.DefinitionGrid',
					data: []
				})
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onClassTabWidgetItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmfg('onClassTabWidgetRowSelected');
			}
		}
	});

})();