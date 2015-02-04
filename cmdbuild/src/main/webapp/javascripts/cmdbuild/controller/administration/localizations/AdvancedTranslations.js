(function() {

	Ext.define('CMDBuild.controller.administration.localizations.AdvancedTranslations', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
//			'CMDBuild.core.proxy.CMProxyConstants'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.AdvancedTranslationsPanel}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.localizations.AdvancedTranslationsPanel} view
		 *
		 * @override
		 */
		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange and controller setup
			this.view = view;
			this.view.delegate = this;
			this.view.importPanel.delegate = this;
			this.view.exportPanel.delegate = this;
		},

		/**
		 * Parent controller/view setup
		 *
		 * @override
		 */
		onViewOnFront: function() {
			this.parentDelegate.view.delegate = this;
			this.parentDelegate.setViewTitle('@@ Advanced translations');

			this.callParent(arguments);
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

				case 'onExportButtonClick':
					return this.onExportButtonClick();

				case 'onImportButtonClick':
					return this.onImportButtonClick();

				case 'onSaveButtonClick':
					return this.onSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onAbortButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslations ABORT');
		},

		onExportButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslations EXPORT');
		},

		onImportButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslations IMPORT');
		},

		onSaveButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslations SAVE');
		}
	});

})();