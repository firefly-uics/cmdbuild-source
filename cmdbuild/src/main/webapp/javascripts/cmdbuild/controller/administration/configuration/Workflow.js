(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Workflow', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.Workflow',
			'CMDBuild.model.configuration.workflow.Form'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationWorkflowSaveButtonClick',
			'onConfigurationWorkflowTabShow = onConfigurationWorkflowAbortButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.WorkflowPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.configuration.Configuration} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.configuration.WorkflowPanel', { delegate: this });
		},

		onConfigurationWorkflowSaveButtonClick: function() {
			CMDBuild.core.proxy.configuration.Workflow.update({
				params: CMDBuild.model.configuration.workflow.Form.convertToLegacy(this.view.getData(true)),
				scope: this,
				success: function(response, options, decodedResponse) {
					this.onConfigurationWorkflowTabShow();

					CMDBuild.core.Message.success();
				}
			});
		},

		onConfigurationWorkflowTabShow: function() {
			CMDBuild.core.proxy.configuration.Workflow.read({
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					this.view.loadRecord(Ext.create('CMDBuild.model.configuration.workflow.Form', CMDBuild.model.configuration.workflow.Form.convertFromLegacy(decodedResponse)));

					_CMMainViewportController.findAccordionByCMName('workflow').setDisabled(
						!CMDBuild.core.Utils.decodeAsBoolean(decodedResponse[CMDBuild.core.constants.Proxy.ENABLED])
					);

					/**
					 * @deprecated (CMDBuild.configuration.workflow)
					 */
					CMDBuild.Config.workflow.enabled = this.view.enabledCheckBox.getValue();
				}
			});
		}
	});

})();