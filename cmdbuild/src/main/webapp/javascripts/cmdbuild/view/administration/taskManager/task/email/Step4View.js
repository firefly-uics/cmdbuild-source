(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.email.Step4View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.email.Step4}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		fieldsetWorkflow: undefined,

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
					this.fieldsetWorkflow = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.startProcess,
						checkboxName: CMDBuild.core.constants.Proxy.WORKFLOW_ACTIVE,
						checkboxToggle: true,
						checkboxUncheckedValue: false,
						checkboxValue: true,
						collapsed: true,
						collapsible: true,
						toggleOnTitleClick: true,
						overflowY: 'auto',

						items: [
							this.fieldWorkflow = Ext.create('CMDBuild.view.administration.taskManager.task.common.field.workflow.WorkflowView', {
								name: CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10,

								config: {
									comboName: CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME,
									gridName: CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES
								}
							})
						],

						listeners: {
							scope: this,
							expand: function (field, eOpts) {
								this.delegate.cmfg('onTaskManagerFormTaskEmailStep4FieldsetWorkflowExpand');
							}
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
