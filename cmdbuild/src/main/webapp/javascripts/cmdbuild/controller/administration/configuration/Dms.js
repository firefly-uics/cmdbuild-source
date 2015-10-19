(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Dms', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.Dms',
			'CMDBuild.model.configuration.dms.Form'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationAlfrescoSaveButtonClick',
			'onConfigurationAlfrescoTabShow = onConfigurationAlfrescoAbortButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.DmsPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.configuration.DmsPanel', { delegate: this });
		},

		onConfigurationAlfrescoSaveButtonClick: function() {
			CMDBuild.core.proxy.configuration.Dms.update({
				params: CMDBuild.model.configuration.dms.Form.convertToLegacy(this.view.getData(true)),
				scope: this,
				success: function(response, options, decodedResponse) {
					this.onConfigurationAlfrescoTabShow();

					CMDBuild.core.Message.success();
				}
			});
		},

		onConfigurationAlfrescoTabShow: function() {
			CMDBuild.core.proxy.configuration.Dms.read({
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					this.view.loadRecord(Ext.create('CMDBuild.model.configuration.dms.Form', CMDBuild.model.configuration.dms.Form.convertFromLegacy(decodedResponse)));
				}
			});
		}
	});

})();