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
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.constants.Proxy.TYPE,
						text: CMDBuild.Translation.type,
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
						flex: 2
					},
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: CMDBuild.core.constants.Proxy.ACTIVE,
						text: CMDBuild.Translation.active,
						width: 60,
						align: 'center',
						hideable: false,
						menuDisabled: true,
						fixed: true,
					})
				],
				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.widget.DefinitionGrid',
					data: [],
					sorters: [
						{ property: CMDBuild.core.constants.Proxy.TYPE, direction: 'ASC' },
						{ property: CMDBuild.core.constants.Proxy.LABEL, direction: 'ASC' }
					]
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