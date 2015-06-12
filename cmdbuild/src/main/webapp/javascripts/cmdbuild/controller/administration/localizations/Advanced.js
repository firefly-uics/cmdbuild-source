(function() {

	Ext.define('CMDBuild.controller.administration.localizations.Advanced', {
		extend: 'CMDBuild.controller.common.AbstractController',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Localizations}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [ // TODO
//			'onLocalizationsAdvancedAbortButtonClick',
//			'onLocalizationsAdvancedSaveButtonClick',
//			'onLocalizationsImportButtonClick',
//			'onLocalizationsExportButtonClick'
		],

		/**
		 * @cfg {CMDBuild.view.administration.localizations.AdvancedPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localizations.Localizations} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localizations.AdvancedPanel', {
				delegate: this
			});
		}
	});

})();