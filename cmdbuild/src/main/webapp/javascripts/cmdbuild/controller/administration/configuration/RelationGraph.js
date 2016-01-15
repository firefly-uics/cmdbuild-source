(function() {

	Ext.define('CMDBuild.controller.administration.configuration.RelationGraph', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.RelationGraph'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationRelationGraphSaveButtonClick',
			'onConfigurationRelationGraphTabShow = onConfigurationRelationGraphAbortButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.RelationGraphPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.configuration.RelationGraphPanel', { delegate: this });
		},

		onConfigurationRelationGraphSaveButtonClick: function() {
			CMDBuild.core.proxy.configuration.RelationGraph.update({
				params: this.view.getData(true),
				scope: this,
				success: function(response, options, decodedResponse) {
					this.cmfg('onConfigurationRelationGraphTabShow');

					CMDBuild.core.Message.success();
				}
			});
		},

		onConfigurationRelationGraphTabShow: function() {
			CMDBuild.core.proxy.configuration.RelationGraph.read({
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					if (!Ext.isEmpty(decodedResponse))
						this.view.loadRecord(Ext.create('CMDBuild.model.configuration.relationGraph.Form', decodedResponse));
				}
			});
		}
	});

})();