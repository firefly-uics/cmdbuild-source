(function() {

	// TODO: to update without extends CMDynamicKeyValueGrid
	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormCombo', {
		extend: 'Ext.form.field.ComboBox',

		delegate: undefined,

		// Required
		name: undefined,

		valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
		displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
		width: CMDBuild.ADM_BIG_FIELD_WIDTH,
		forceSelection: true,
		editable: false,

		store: CMDBuild.core.proxy.CMProxyTasks.getStoreAllWorkflow(),
		queryMode: 'local',

		listeners: {
			select: function(combo, records, eOpts) {
				this.delegate.cmOn('onSelectWorkflow', this.getValue());
			}
		}
	});

})();