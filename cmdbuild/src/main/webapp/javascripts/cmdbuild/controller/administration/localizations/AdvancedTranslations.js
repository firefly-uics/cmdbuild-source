(function() {

	Ext.define('CMDBuild.controller.administration.localizations.AdvancedTranslations', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.AdvancedTranslationsPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.localizations.AdvancedTranslationsPanel', {
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
				case 'onAdvancedAbortButtonClick':
					return this.onAdvancedAbortButtonClick();

				case 'onAdvancedSaveButtonClick':
					return this.onAdvancedSaveButtonClick();

				case 'onExportButtonClick':
					return this.onExportButtonClick();

				case 'onImportButtonClick':
					return this.onImportButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return {CMDBuild.view.administration.localizations.AdvancedTranslationsPanel}
		 */
		getView: function() {
			return this.view;
		},

		onAdvancedAbortButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslations ABORT');
		},

		onAdvancedSaveButtonClick: function() {
			_debug('CMDBuild.controller.administration.localizations.AdvancedTranslations SAVE');
		},

		onExportButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslations EXPORT');
		},

		onImportButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslations IMPORT');
		}
	});

})();