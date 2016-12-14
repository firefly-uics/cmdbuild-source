(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.common.field.report.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.administration.taskManager.task.common.field.report.Grid'
		],

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.common.field.report.Report}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.plugin.CellEditing}
		 */
		cellEditingPlugin: undefined,

		disabled: true,
		enablePanelFunctions: true,
		margin: '0 0 0 ' + (CMDBuild.core.constants.FieldWidths.LABEL - 5),
		maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
		title: CMDBuild.Translation.parameters,

		/**
		 * @returns {View}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						flex: 1
					},
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: CMDBuild.core.constants.Proxy.EDITING_MODE,
						text: CMDBuild.Translation.cqlExpression,
						width: 100,
						align: 'center',
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						listeners: {
							scope: this,
							checkchange: function (column, rowIndex, checked, eOpts) {
								this.delegate.cmfg('onTaskManagerCommonFieldReportGridEditingModeCheckChange', rowIndex);
							}
						}
					}),
					{
						dataIndex: CMDBuild.core.constants.Proxy.VALUE,
						text: CMDBuild.Translation.value,
						flex: 1,

						editor: { xtype: 'textfield' }
					}
				],
				plugins: [
					this.cellEditingPlugin = Ext.create('Ext.grid.plugin.CellEditing', {
						clicksToEdit: 1,

						listeners: {
							scope: this,
							beforeedit: function (editor, e, eOpts) {
								this.delegate.cmfg('onTaskManagerCommonFieldReportGridBeforeEdit', {
									column: e.column,
									columnName: e.field,
									record: e.record
								});
							}
						}
					})
				],
				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.administration.taskManager.task.common.field.report.Grid',
					data: []
				})
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Object}
		 */
		getValue: function () {
			return this.delegate.cmfg('taskManagerCommonFieldReportGridValueGet');
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function () {
			return this.delegate.cmfg('taskManagerCommonFieldReportGridIsValid');
		},

		/**
		 * @returns {Void}
		 */
		reset: function () {
			this.delegate.cmfg('taskManagerCommonFieldReportGridReset');
		},

		/**
		 * @param {Array or String} value
		 *
		 * @returns {Void}
		 */
		setValue: function (value) {
			this.delegate.cmfg('taskManagerCommonFieldReportGridValueSet', value);
		}
	});

})();
