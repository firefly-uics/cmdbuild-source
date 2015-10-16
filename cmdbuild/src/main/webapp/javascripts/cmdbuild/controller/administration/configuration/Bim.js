(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Bim', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.Bim',
			'CMDBuild.model.configuration.bim.Form'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationBimAbortButtonClick',
			'onConfigurationBimSaveButtonClick',
			'onConfigurationBimTabShow'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.BimPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.configuration.BimPanel', { delegate: this });
		},

		onConfigurationBimAbortButtonClick: function() {
			this.onConfigurationBimTabShow();
		},

		onConfigurationBimSaveButtonClick: function() {
			CMDBuild.core.proxy.configuration.Bim.update({
				params: this.view.getData(true),
				scope: this,
				success: function(response, options, decodedResponse) {
					this.onConfigurationBimTabShow();

					CMDBuild.core.Message.success();
				}
			});
		},

		/**
		 * NOTE: Readed enabled response parameter must be decoded by utils function because is a string, not a boolean
		 */
		onConfigurationBimTabShow: function() {
			CMDBuild.core.proxy.configuration.Bim.read({
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					this.view.loadRecord(Ext.create('CMDBuild.model.configuration.bim.Form', decodedResponse));

					_CMMainViewportController.findAccordionByCMName('bim').setDisabled(
						!CMDBuild.core.Utils.decodeAsBoolean(decodedResponse[CMDBuild.core.constants.Proxy.ENABLED])
					);

					/**
					 * @deprecated (CMDBuild.configuration.bim)
					 */
					CMDBuild.Config.workflow.enabled = decodedResponse[CMDBuild.core.constants.Proxy.ENABLED];
				}
			});
		}
	});

})();