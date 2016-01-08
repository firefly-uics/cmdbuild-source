(function() {

	Ext.define('CMDBuild.view.administration.configuration.RelationGraphPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

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
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationRelationGraphSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationRelationGraphAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					{
						xtype: 'checkbox',
						name: CMDBuild.core.constants.Proxy.ENABLED,
						fieldLabel: CMDBuild.Translation.enabled,
						inputValue: true,
						uncheckedValue: false
					},
					{
						xtype: 'numberfield',
						name: CMDBuild.core.constants.Proxy.BASE_LEVEL,
						fieldLabel: CMDBuild.Translation.defaultLevel,
						allowBlank: false,
						minValue: 1,
						maxValue: 5
					},
					{
						xtype: 'numberfield',
						name: CMDBuild.core.constants.Proxy.EXTENSION_MAXIMUM_LEVEL,
						fieldLabel: CMDBuild.Translation.maximumLevel,
						allowBlank: false,
						minValue: 1,
						maxValue: 5
					},
					{
						xtype: 'numberfield',
						name: CMDBuild.core.constants.Proxy.CLUSTERING_THRESHOLD,
						fieldLabel: CMDBuild.Translation.thresholdForClusteringNodes,
						allowBlank: false,
						minValue: 2,
						maxValue: 20
					}
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onConfigurationRelationGraphTabShow');
			}
		}
	});

})();