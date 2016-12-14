(function () {

	Ext.require([
		'CMDBuild.core.constants.FieldWidths',
		'CMDBuild.core.constants.Proxy',
		'CMDBuild.proxy.administration.taskManager.task.common.field.Workflow'
	]);

	/**
	 * NOTES:
	 * 	- values set is done from controller
	 * 	- values get is done directly inside sub fields
	 */
	Ext.define('CMDBuild.view.administration.taskManager.task.common.field.workflow.WorkflowView', {
		extend: 'Ext.form.FieldContainer',

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @property {CMDBuild.controller.administration.taskManager.task.common.field.workflow.Workflow}
		 */
		delegate: undefined,

		/**
		 * @cfg {Object}
		 */
		config: {},

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		fieldCombo: undefined,

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.common.field.workflow.GridPanel}
		 */
		fieldGrid: undefined,

		border: false,
		frame: false,
		fieldLabel: CMDBuild.Translation.workflow,
		labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
		submitValue: false,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			this.delegate = Ext.create('CMDBuild.controller.administration.taskManager.task.common.field.workflow.Workflow', { view: this });

			Ext.apply(this, {
				items: [
					this.fieldCombo = Ext.create('Ext.form.field.ComboBox', {
						name: this.config.comboName,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						displayField: CMDBuild.core.constants.Proxy.TEXT,
						forceSelection: true,
						editable: false,

						store: CMDBuild.proxy.administration.taskManager.task.common.field.Workflow.getStore(),
						queryMode: 'local',

						listeners: {
							scope: this,
							select: function (field, records, eOpts) {
								this.delegate.cmfg('onTaskManagerCommonFieldWorkflowComboSelect');
							}
						}
					}),
					this.fieldGrid = Ext.create('CMDBuild.view.administration.taskManager.task.common.field.workflow.GridPanel', {
						delegate: this.delegate,
						name: this.config.gridName
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function () {
			return this.delegate.cmfg('taskManagerCommonFieldWorkflowIsValid');
		},

		/**
		 * @returns {Void}
		 */
		reset: function () {
			this.delegate.cmfg('taskManagerCommonFieldWorkflowReset');
		},

		/**
		 * @param {Array or String} value
		 *
		 * @returns {Void}
		 */
		setValue: function (value) {
			return this.delegate.cmfg('taskManagerCommonFieldWorkflowValueSet', value);
		}
	});

})();
