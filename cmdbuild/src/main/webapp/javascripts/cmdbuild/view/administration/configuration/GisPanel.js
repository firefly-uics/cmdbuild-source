(function() {

	Ext.define('CMDBuild.view.administration.configuration.GisPanel', {
		extend: 'CMDBuild.view.administration.configuration.BasePanel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Main}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		configFileName: 'gis',

		initComponent: function() {
			Ext.apply(this, {
				title: this.baseTitle + this.titleSeparator + CMDBuild.Translation.gis,
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
				_CMMainViewportController.enableAccordionByName('gis');
			} else {
				_CMMainViewportController.disableAccordionByName('gis');
			}
		}
	});

})();