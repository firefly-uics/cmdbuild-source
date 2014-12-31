(function() {

	Ext.define('CMDBuild.view.common.field.LanguageCombo', {
		alternateClassName: 'CMDBuild.field.LanguageCombo', // Legacy class name
		extend: 'CMDBuild.field.CMIconCombo',

		// TODO: to use in future
//		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		initComponent: function() {
			Ext.apply(this, {
				valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
				displayField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				store: CMDBuild.ServiceProxy.setup.getLanguageStore(),
				queryMode: 'local'
			});

			this.callParent(arguments);

			this.on('select', function(eventName, args) {
				this.changeLanguage(args[0].get(CMDBuild.core.proxy.CMProxyConstants.NAME));
			}, this);

			this.store.on('load', function() {
				this.setValue(this.getCurrentLanguage());
			}, this);
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