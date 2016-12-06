(function () {

	Ext.define('CMDBuild.controller.administration.localization.Localization', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLocalizationModuleInit = onModuleInit'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {Mixed}
		 */
		sectionController: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.LocalizationView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Object} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localization.LocalizationView', { delegate: this });
		},

		/**
		 * @param {String} sectionIdentifier
		 *
		 * @returns {Mixed}
		 *
		 * @private
		 */
		buildSectionController: function (sectionIdentifier) {
			switch (sectionIdentifier) {
				case 'advancedTranslationsTable':
					return Ext.create('CMDBuild.controller.administration.localization.advancedTable.AdvancedTable', { parentDelegate: this });

				case 'importExport':
					return Ext.create('CMDBuild.controller.administration.localization.ImportExport', { parentDelegate: this });

				case 'configuration':
				default:
					return Ext.create('CMDBuild.controller.administration.localization.Configuration', { parentDelegate: this });
			}
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {Object} parameters
		 * @param {CMDBuild.model.common.Accordion} parameters.node
		 *
		 * @override
		 */
		onLocalizationModuleInit: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			if (Ext.isObject(parameters.node) && !Ext.Object.isEmpty(parameters.node)) {
				this.sectionController = this.buildSectionController(parameters.node.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]);

				this.view.removeAll(true);

				this.setViewTitle(parameters.node.get(CMDBuild.core.constants.Proxy.TEXT));

				this.view.add(this.sectionController.getView());

				this.sectionController.getView().fireEvent('show'); // Manual show event fire

				this.onModuleInit(parameters); // Custom callParent() implementation
			}
		}
	});

})();
