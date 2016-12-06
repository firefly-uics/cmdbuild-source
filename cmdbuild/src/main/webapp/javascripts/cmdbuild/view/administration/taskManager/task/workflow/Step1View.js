(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.workflow.Step1View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.workflow.Step1}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.common.field.workflow.WorkflowView}
		 */
		fieldWorkflow: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,
		overflowY: 'auto',

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
			Ext.apply(this, {
				items: [
					Ext.create('Ext.form.field.Display', {
						name: CMDBuild.core.constants.Proxy.TYPE,
						fieldLabel: CMDBuild.Translation.type,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						value: CMDBuild.Translation.workflow
					}),
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
						allowBlank: false
					}),
					Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.constants.Proxy.ACTIVE,
						fieldLabel: CMDBuild.Translation.startOnSave,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
					}),
					this.fieldWorkflow = Ext.create('CMDBuild.view.administration.taskManager.task.common.field.workflow.WorkflowView', {
						name: CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,

						config: {
							comboName: CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME,
							gridName: CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES
						}
					}),
					Ext.create('Ext.form.field.Hidden', { name: CMDBuild.core.constants.Proxy.ID })
				]
			});

			this.callParent(arguments);
		}
	});

})();
