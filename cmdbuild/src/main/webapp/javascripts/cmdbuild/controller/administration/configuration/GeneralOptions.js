( function () {

	Ext.define('CMDBuild.controller.administration.configuration.GeneralOptions', {
		extend: 'CMDBuild.controller.common.abstract.Base',

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
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.configuration.Configuration} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.configuration.GeneralOptionsPanel', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onConfigurationGeneralOptionsSaveButtonClick: function () {
			CMDBuild.core.proxy.configuration.GeneralOptions.update({
				params: CMDBuild.model.configuration.instance.Form.convertToLegacy(this.view.getData(true)),
				scope: this,
				success: function (response, options, decodedResponse) {
					this.cmfg('onConfigurationGeneralOptionsTabShow');

					CMDBuild.view.common.field.translatable.Utils.commit(this.view);

					CMDBuild.core.Message.success();
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onConfigurationGeneralOptionsTabShow: function () {
			CMDBuild.core.proxy.configuration.GeneralOptions.read({
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					if (!Ext.isEmpty(decodedResponse)) {
						this.view.loadRecord(Ext.create('CMDBuild.model.configuration.instance.Form', CMDBuild.model.configuration.instance.Form.convertFromLegacy(decodedResponse)));

						this.cmfg('mainViewportInstanceNameSet', decodedResponse[CMDBuild.core.constants.Proxy.INSTANCE_NAME]);

						this.view.instanceNameField.translationsRead(); // Custom function call to read translations data

						Ext.create('CMDBuild.core.configurationBuilders.Instance'); // Rebuild configuration model
					}
				}
			});
		}
	});

})();
