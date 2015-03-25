(function() {

	Ext.define('CMDBuild.view.administration.configuration.GisPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Main}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		configFileName: 'gis',

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
			width: CMDBuild.CFG_MEDIUM_FIELD_WIDTH
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					{
						xtype: 'xcheckbox',
						name: CMDBuild.core.proxy.CMProxyConstants.ENABLED,
						fieldLabel: CMDBuild.Translation.enable
					},
					{
						xtype: 'numberfield',
						name: 'center.lat',
						decimalPrecision: 6,
						fieldLabel: CMDBuild.Translation.initialLatitude
					},
					{
						xtype: 'numberfield',
						name: 'center.lon',
						decimalPrecision: 6,
						fieldLabel: CMDBuild.Translation.initialLongitude
					},
					{
						xtype: 'numberfield',
						name: CMDBuild.core.proxy.CMProxyConstants.INITIAL_ZOOM_LEVEL,
						fieldLabel: CMDBuild.Translation.initialZoomLevel,
						minValue: 0,
						maxValue: 25
					}
				],
				dockedItems: [
					{
						xtype: 'toolbar',
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
									this.delegate.cmOn('onConfigurationSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmOn('onConfigurationAbortButtonClick');
								}
							})
						]
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
		afterSubmit: function(saveDataObject) {
			// TODO: refactor in better way when possible
			CMDBuild.Config.gis = Ext.apply(CMDBuild.Config.gis, saveDataObject);
			CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);

			if (CMDBuild.Config.gis.enabled) {
				_CMMainViewportController.enableAccordionByName('gis');
			} else {
				_CMMainViewportController.disableAccordionByName('gis');
			}
		}
	});

})();