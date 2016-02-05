(function() {

	Ext.define('CMDBuild.view.administration.configuration.GisPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Gis}
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
									this.delegate.cmfg('onGisSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onGisAbortButtonClick');
								}
							})
						]
					})
				],
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
				_CMMainViewportController.enableAccordionByName(this.delegate.configFileName);
			} else {
				_CMMainViewportController.disableAccordionByName(this.delegate.configFileName);
			}
		}
	});

})();