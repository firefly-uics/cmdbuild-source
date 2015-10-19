(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Alfresco', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.Alfresco',
			'CMDBuild.model.configuration.alfresco.Form'
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
		 * @property {CMDBuild.view.administration.configuration.AlfrescoPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.configuration.AlfrescoPanel', { delegate: this });
		},

		onConfigurationAlfrescoSaveButtonClick: function() {
			CMDBuild.core.proxy.configuration.Alfresco.update({
				params: CMDBuild.model.configuration.alfresco.Form.convertToLegacy(this.view.getData(true)),
				scope: this,
				success: function(response, options, decodedResponse) {
					this.onConfigurationAlfrescoTabShow();

					CMDBuild.core.Message.success();
				}
			});
		},

		onConfigurationAlfrescoTabShow: function() {
			CMDBuild.core.proxy.configuration.Alfresco.read({
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					this.view.loadRecord(Ext.create('CMDBuild.model.configuration.alfresco.Form', CMDBuild.model.configuration.alfresco.Form.convertFromLegacy(decodedResponse)));
				}
			});
		}
	});

})();