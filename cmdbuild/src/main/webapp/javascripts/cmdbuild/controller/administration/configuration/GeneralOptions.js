(function() {

	Ext.define('CMDBuild.controller.administration.configuration.GeneralOptions', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.GeneralOptions',
			'CMDBuild.model.configuration.instance.Form'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationGeneralOptionsSaveButtonClick',
			'onConfigurationGeneralOptionsTabShow = onConfigurationGeneralOptionsAbortButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.GeneralOptionsPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.configuration.GeneralOptionsPanel', { delegate: this });
		},

		onConfigurationGeneralOptionsSaveButtonClick: function() {
			CMDBuild.core.proxy.configuration.GeneralOptions.update({
				params: CMDBuild.model.configuration.instance.Form.convertToLegacy(this.view.getData(true)),
				scope: this,
				success: function(response, options, decodedResponse) {
					this.onConfigurationGeneralOptionsTabShow();

					CMDBuild.view.common.field.translatable.Utils.commit(this.view);

					CMDBuild.core.Message.success();
				}
			});
		},

		onConfigurationGeneralOptionsTabShow: function() {
			CMDBuild.core.proxy.configuration.GeneralOptions.read({
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					this.view.loadRecord(Ext.create('CMDBuild.model.configuration.instance.Form', CMDBuild.model.configuration.instance.Form.convertFromLegacy(decodedResponse)));

					Ext.get('instance_name').dom.innerHTML = decodedResponse[CMDBuild.core.constants.Proxy.INSTANCE_NAME];

					this.view.instanceNameField.translationsRead(); // Custom function call to read translations data
				}
			});
		}
	});

})();