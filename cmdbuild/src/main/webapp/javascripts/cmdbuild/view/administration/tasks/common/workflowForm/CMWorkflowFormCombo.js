(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormCombo', {
		extend: 'Ext.form.field.ComboBox',

		delegate: undefined,

		// Required
		name: undefined,

		valueField: CMDBuild.ServiceProxy.parameter.NAME,
		displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
		store: CMDBuild.core.proxy.CMProxyTasks.getStoreAllWorkflow(),
		width: CMDBuild.ADM_BIG_FIELD_WIDTH,
		forceSelection: true,
		editable: false,

		listeners: {
			select: function(combo, records, eOpts) {
				this.delegate.cmOn('onSelectWorkflow', this.getValue());
			}
		},

		initComponent: function() {
			this.callParent(arguments);
		}
	});

})();