(function () {

	/**
	 * Attribute mapping
	 */
	Ext.define('CMDBuild.view.administration.taskManager.task.connector.Step5View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.administration.taskManager.task.connector.MappingAttribute'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.connector.Step5}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.column.Column}
		 */
		columnClassName: undefined,

		/**
		 * @property {Ext.grid.column.Column}
		 */
		columnSourceName: undefined,

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,
		layout: 'fit',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.taskManager.task.common.field.editableGrid.EditableGridView', {
						parentDelegate: this.delegate,
						name: CMDBuild.core.constants.Proxy.ATTRIBUTE_MAPPING,
						fieldLabel: CMDBuild.Translation.attributeMapping,
						minWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						storeModelName: 'CMDBuild.model.administration.taskManager.task.connector.MappingAttribute',
						enableFireEventBeforeEdit: true,

						columns: [
							this.columnSourceName = Ext.create('Ext.grid.column.Column', {
								dataIndex: CMDBuild.core.constants.Proxy.SOURCE_NAME,
								text: CMDBuild.Translation.externalEntity,
								flex: 1,

								editor: {
									xtype: 'combo',
									disabled: true
								}
							}),
							Ext.create('Ext.grid.column.Column', {
								dataIndex: CMDBuild.core.constants.Proxy.SOURCE_ATTRIBUTE,
								text: CMDBuild.Translation.externalAttribute,
								flex: 1,

								editor: {
									xtype: 'textfield',
									allowBlank: false
								}
							}),
							this.columnClassName = Ext.create('Ext.grid.column.Column', {
								dataIndex: CMDBuild.core.constants.Proxy.CLASS_NAME,
								text: CMDBuild.Translation.cmdBuildClass,
								flex: 1,

								editor: {
									xtype: 'combo',
									disabled: true
								}
							}),
							this.columnClassAttribute = Ext.create('Ext.grid.column.Column', {
								dataIndex: CMDBuild.core.constants.Proxy.CLASS_ATTRIBUTE,
								text: CMDBuild.Translation.cmdBuildAttribute,
								flex: 1,

								editor: {
									xtype: 'combo',
									disabled: true
								}
							}),
							Ext.create('Ext.grid.column.CheckColumn', {
								text: CMDBuild.Translation.isKey,
								dataIndex: CMDBuild.core.constants.Proxy.IS_KEY,
								width: 50,
								align: 'center',
								sortable: false,
								hideable: false,
								menuDisabled: true,
								fixed: true
							})
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (view, eOpts) {
				this.delegate.cmfg('onTaskManagerFormTaskConnectorStep5Show');
			}
		}
	});

})();
