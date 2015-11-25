(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Gis', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.Gis',
			'CMDBuild.model.configuration.gis.Form'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationGisSaveButtonClick',
			'onConfigurationGisTabShow = onConfigurationGisAbortButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.GisPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.configuration.GisPanel', { delegate: this });
		},

		onConfigurationGisSaveButtonClick: function() {
			CMDBuild.core.proxy.configuration.Gis.update({
				params: CMDBuild.model.configuration.gis.Form.convertToLegacy(this.view.getData(true)),
				scope: this,
				success: function(response, options, decodedResponse) {
					this.onConfigurationGisTabShow();

					CMDBuild.core.Message.success();
				}
			});
		},

		onConfigurationGisTabShow: function() {
			CMDBuild.core.proxy.configuration.Gis.read({
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					this.view.loadRecord(Ext.create('CMDBuild.model.configuration.gis.Form', CMDBuild.model.configuration.gis.Form.convertFromLegacy(decodedResponse)));

					_CMMainViewportController.findAccordionByCMName('gis').setDisabled(
						!CMDBuild.core.Utils.decodeAsBoolean(decodedResponse[CMDBuild.core.constants.Proxy.ENABLED])
					);

					/**
					 * @deprecated (CMDBuild.configuration.gis)
					 */
					CMDBuild.Config.gis = Ext.apply(CMDBuild.Config.gis, decodedResponse);
					CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);
				}
			});
		}
	});

})();