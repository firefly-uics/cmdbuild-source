(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.common.field.workflow.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.administration.taskManager.task.common.field.workflow.Grid'
		],

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.common.field.workflow.Workflow}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.plugin.CellEditing}
		 */
		pluginGridEditor: undefined,

		border: true,
		disabled: true,
		enablePanelFunctions: true,
		frame: false,
		minWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
		title: CMDBuild.Translation.workflowAttributes,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onTaskManagerCommonFieldWorkflowGridAddButtonClick');
								}
							})
						]
					})
				],
				columns: [
					{
						header: CMDBuild.Translation.name,
						dataIndex: CMDBuild.core.constants.Proxy.NAME,
						flex: 1,

						editor: { xtype: 'textfield' }
					},
					{
						header: CMDBuild.Translation.value,
						dataIndex: CMDBuild.core.constants.Proxy.VALUE,
						flex: 1,

						editor: { xtype: 'textfield' }
					},
					Ext.create('Ext.grid.column.Action', {
						align: 'center',
						width: 30,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Remove', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.remove,
								scope: this,

								handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onTaskManagerCommonFieldWorkflowGridRemoveButtonClick', record);
								}
							})
						]
					})
				],
				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.administration.taskManager.task.common.field.workflow.Grid',
					data: []
				}),
				plugins: [
					this.pluginGridEditor = Ext.create('Ext.grid.plugin.CellEditing', {
						clicksToEdit: 1,

						listeners: {
							scope: this,
							beforeedit: function (editor, e, eOpts) {
								this.delegate.cmfg('onTaskManagerCommonFieldWorkflowGridBeforeEdit', {
									name: e.field,
									rowIndex: e.rowIdx
								});
							}
						}
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Boolean}
		 */
		enable: function () {
			if (this.delegate.cmfg('taskManagerCommonFieldWorkflowGridEnable'))
				this.callParent(arguments);
		},

		/**
		 * @returns {Object}
		 */
		getValue: function () {
			return this.delegate.cmfg('taskManagerCommonFieldWorkflowGridValueGet');
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function () {
			return true;
		},

		/**
		 * @returns {Void}
		 */
		reset: function () {
			this.delegate.cmfg('taskManagerCommonFieldWorkflowGridReset');
		},

		/**
		 * @param {Array or String} value
		 *
		 * @returns {Void}
		 */
		setValue: function (value) {
			return this.delegate.cmfg('taskManagerCommonFieldWorkflowGridValueSet', value);
		}
	});

})();
