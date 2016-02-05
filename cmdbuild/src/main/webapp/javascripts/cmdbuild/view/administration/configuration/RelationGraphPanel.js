(function() {

	Ext.define('CMDBuild.view.administration.configuration.RelationGraphPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.RelationGraph}
		 */
		delegate: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		fieldDefaults: {
			labelAlign: 'left',
			labelWidth: CMDBuild.CFG_LABEL_WIDTH,
			maxWidth: CMDBuild.CFG_MEDIUM_FIELD_WIDTH
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onRelationGraphSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onRelationGraphAbortButtonClick');
								}
							})
						]
					})
				],
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
		}
	});

})();