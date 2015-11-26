(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormCombo', {
		extend: 'Ext.form.field.ComboBox',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 *
		 * @required
		 */
		name: undefined,

		valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
		displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
		maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
		forceSelection: true,
		editable: false,

		store: CMDBuild.core.proxy.CMProxyTasks.getStoreAllWorkflow(),
		queryMode: 'local',

		listeners: {
			select: function(combo, records, eOpts) {
				this.delegate.cmOn('onSelectWorkflow', true);
			}
		}
	});

})();