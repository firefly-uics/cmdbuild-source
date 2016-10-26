(function () {

	/**
	 * Class mapping
	 */
	Ext.define('CMDBuild.view.administration.taskManager.task.connector.Step4View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.administration.taskManager.task.connector.MappingClass'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.connector.Step4}
		 */
		delegate: undefined,

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
						name: CMDBuild.core.constants.Proxy.CLASS_MAPPING,
						fieldLabel: CMDBuild.Translation.classMapping,
						minWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						storeModelName: 'CMDBuild.model.administration.taskManager.task.connector.MappingClass',
						enableFireEventRowRemove: true,

						columns: [
							Ext.create('Ext.grid.column.Column', {
								dataIndex: CMDBuild.core.constants.Proxy.SOURCE_NAME,
								text: CMDBuild.Translation.externalEntity,
								flex: 1,

								editor: {
									xtype: 'textfield',
									allowBlank: false,

									listeners: {
										scope: this,
										blur: function (field, The, eOpts) {
											this.delegate.cmfg('onTaskManagerFormTaskConnectorStep4Show');
										}
									}
								}
							}),
							Ext.create('Ext.grid.column.Column', {
								dataIndex: CMDBuild.core.constants.Proxy.CLASS_NAME,
								text: CMDBuild.Translation.cmdBuildClass,
								flex: 1,

								editor: {
									xtype: 'combo',
									displayField: CMDBuild.core.constants.Proxy.TEXT,
									valueField: CMDBuild.core.constants.Proxy.NAME,
									forceSelection: true,
									allowBlank: false,

									store: CMDBuild.proxy.administration.taskManager.task.Connector.getStoreClasses(),
									queryMode: 'local',

									listeners: {
										scope: this,
										select: function (field, records, eOpts) {
											this.delegate.cmfg('onTaskManagerFormTaskConnectorStep4Show');
										}
									}
								}
							}),
							Ext.create('Ext.grid.column.CheckColumn', {
								text: CMDBuild.Translation.createLabel,
								dataIndex: CMDBuild.core.constants.Proxy.CREATE,
								width: 60,
								align: 'center',
								sortable: false,
								hideable: false,
								menuDisabled: true,
								fixed: true
							}),
							Ext.create('Ext.grid.column.CheckColumn', {
								text: CMDBuild.Translation.updateLabel,
								dataIndex: CMDBuild.core.constants.Proxy.UPDATE,
								width: 60,
								align: 'center',
								sortable: false,
								hideable: false,
								menuDisabled: true,
								fixed: true
							}),
							Ext.create('Ext.grid.column.CheckColumn', {
								text: CMDBuild.Translation.deleteLabel,
								dataIndex: CMDBuild.core.constants.Proxy.DELETE,
								width: 60,
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
				this.delegate.cmfg('onTaskManagerFormTaskConnectorStep4Show');
			}
		}
	});

})();
