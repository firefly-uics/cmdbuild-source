(function() {

	Ext.define('CMDBuild.controller.administration.localizations.BaseTranslations', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
//			'CMDBuild.core.proxy.CMProxyConstants'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.BaseTranslationsPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localizations.Main} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.administration.localizations.BaseTranslationsPanel', {
				delegate: this
			});
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();

				case 'onSaveButtonClick':
					return this.onSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return {CMDBuild.view.administration.localizations.BaseTranslationsPanel}
		 */
		getView: function() {
			return this.view;
		},

		onAbortButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.BaseTranslations ABORT');
		},

		onSaveButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.BaseTranslations SAVE');
		}
	});

})();