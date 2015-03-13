(function() {

	Ext.define('CMDBuild.view.administration.configuration.RelationGraphPanel', {
		extend: 'CMDBuild.view.administration.configuration.BasePanel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Main}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		configFileName: 'graph',

		initComponent: function() {
			Ext.apply(this, {
				title: this.baseTitle + this.titleSeparator + CMDBuild.Translation.relationGraph,
				items: [
					{
						xtype: 'xcheckbox',
						name: CMDBuild.core.proxy.CMProxyConstants.ENABLED,
						fieldLabel: CMDBuild.Translation.enabled
					},
					{
						xtype: 'numberfield',
						name: CMDBuild.core.proxy.CMProxyConstants.BASE_LEVEL,
						fieldLabel: CMDBuild.Translation.defaultLevel,
						allowBlank: false,
						minValue: 1,
						maxValue: 5
					},
					{
						xtype: 'numberfield',
						name: CMDBuild.core.proxy.CMProxyConstants.EXTENSION_MAXIMUM_LEVEL,
						fieldLabel: CMDBuild.Translation.maximumLevel,
						allowBlank: false,
						minValue: 1,
						maxValue: 5
					},
					{
						xtype: 'numberfield',
						name: CMDBuild.core.proxy.CMProxyConstants.CLUSTERING_THRESHOLD,
						fieldLabel: CMDBuild.Translation.thresholdForClusteringNodes,
						allowBlank: false,
						minValue: 2,
						maxValue: 20
					}
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @param {Object} saveDataObject
		 *
		 * @override
		 */
		afterSubmit: function(saveDataObject) {}
	});

})();