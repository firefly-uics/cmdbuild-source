(function() {

	Ext.define('CMDBuild.view.common.field.LanguageCombo', {
		alternateClassName: 'CMDBuild.field.LanguageCombo', // Legacy class name
		extend: 'CMDBuild.view.common.field.CMIconCombo',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {Boolean}
		 */
		enableChangeLanguage: true,

		displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
		iconClsField: CMDBuild.core.proxy.CMProxyConstants.TAG,
		valueField: CMDBuild.core.proxy.CMProxyConstants.TAG,

		initComponent: function() {
			Ext.apply(this, {
				store: CMDBuild.core.proxy.Localizations.getLanguagesStore(),
				queryMode: 'local'
			});

			this.callParent(arguments);

			this.store.on('load', function() {
				this.setValue(this.getCurrentLanguage());
			}, this);
		},

		listeners: {
			select: function(field, newValue, oldValue, eOpts) {
				newValue = newValue[0] || null;

				if (this.enableChangeLanguage)
					this.changeLanguage(newValue[0].get(CMDBuild.core.proxy.CMProxyConstants.TAG));
			}
		},

		/**
		 * @param {String} lang
		 */
		changeLanguage: function(lang) {
			window.location = Ext.String.format('?language={0}', lang);
		},

		/**
		 * @return {String}
		 */
		getCurrentLanguage: function() {
			return Ext.urlDecode(window.location.search.substring(1))[CMDBuild.core.proxy.CMProxyConstants.LANGUAGE] || CMDBuild.Config.cmdbuild.language;
		}
	});

})();