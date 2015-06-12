(function() {

	Ext.define('CMDBuild.controller.administration.localizations.Base', {
		extend: 'CMDBuild.controller.common.AbstractController',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Localizations}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [ // TODO
//			'onLocalizationsBaseAbortButtonClick',
//			'onLocalizationsBaseSaveButtonClick'
		],

		/**
		 * @cfg {CMDBuild.view.administration.localizations.BasePanel}
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

			this.view = Ext.create('CMDBuild.view.administration.localizations.BasePanel', {
				delegate: this
			});
		}
	});

})();