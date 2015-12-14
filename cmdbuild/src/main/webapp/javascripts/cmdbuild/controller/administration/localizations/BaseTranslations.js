(function() {

	Ext.define('CMDBuild.controller.administration.localizations.BaseTranslations', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

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
				case 'onBaseAbortButtonClick':
					return this.onBaseAbortButtonClick();

				case 'onBaseSaveButtonClick':
					return this.onBaseSaveButtonClick();

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

		onBaseAbortButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.BaseTranslations ABORT');
		},

		onBaseSaveButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.BaseTranslations SAVE');
		}
	});

})();