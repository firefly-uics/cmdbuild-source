(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormCombo', {
		extend: 'Ext.form.field.ComboBox',

		delegate: undefined,

		fieldLabel: tr.workflow,
		valueField: CMDBuild.ServiceProxy.parameter.NAME,
		displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
		store: CMDBuild.core.proxy.CMProxyTasks.getWorkflowsStore(),
		width: CMDBuild.CFG_BIG_FIELD_WIDTH,
		labelWidth: CMDBuild.LABEL_WIDTH,
		forceSelection: true,
		editable: false,
		allowBlank: false,

		listeners: {
			select: function() {
				this.delegate.onWorkflowSelected(this.getValue());
			}
		},

		initComponent: function() {
			this.callParent(arguments);
		}
	});

})();