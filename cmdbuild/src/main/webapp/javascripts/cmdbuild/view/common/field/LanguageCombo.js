(function() {

	Ext.define('CMDBuild.view.common.field.LanguageCombo', {
		extend: 'CMDBuild.view.common.field.CMIconCombo',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.localization.Localization'
		],

		/**
		 * @cfg {Boolean}
		 */
		enableChangeLanguage: true,

		displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
		editable: false,
		forceSelection: true,
		iconClsField: CMDBuild.core.constants.Proxy.TAG,
		valueField: CMDBuild.core.constants.Proxy.TAG,

		initComponent: function() {
			Ext.apply(this, {
				store: CMDBuild.core.proxy.localization.Localization.getLanguagesStore(),
				queryMode: 'local'
			});

			this.callParent(arguments);

			this.getStore().on('load', function() {
				this.setValue(this.getCurrentLanguage());
			}, this);
		},

		listeners: {
			select: function(field, records, eOpts) {
				if (this.enableChangeLanguage)
					this.changeLanguage(records[0].get(CMDBuild.core.constants.Proxy.TAG));
			}
		},

		/**
		 * @param {String} language
		 */
		changeLanguage: function(language) {
			window.location = Ext.String.format('?' + CMDBuild.core.constants.Proxy.LANGUAGE + '={0}', language);
		},

		/**
		 * @returns {String}
		 */
		getCurrentLanguage: function() {
			// Step 1: check URL
			if (!Ext.isEmpty(window.location.search))
				return Ext.Object.fromQueryString(window.location.search)[CMDBuild.core.constants.Proxy.LANGUAGE];

			// Step 2: check CMDBuild configuration
			if (
				!Ext.isEmpty(CMDBuild)
				&& !Ext.isEmpty(CMDBuild.configuration)
				&& !Ext.isEmpty(CMDBuild.configuration.localization)
			) {
				return CMDBuild.configuration.localization.get(CMDBuild.core.constants.Proxy.LANGUAGE);
			}

			// Step 3: use a default language tag
			return 'en';
		}
	});

})();