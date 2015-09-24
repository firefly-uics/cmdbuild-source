(function() {

	Ext.define('CMDBuild.controller.administration.localization.advancedTable.SectionClass', {
		extend: 'CMDBuild.controller.administration.localization.advancedTable.SectionAbstract',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.localization.Localization'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.advancedTable.AdvancedTable}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		entityFilter: ['class'],

		/**
		 * @cfg {Array}
		 */
		entityAttributeFilter: ['notes'],

		/**
		 * @cfg {String}
		 */
		sectionId: CMDBuild.core.constants.Proxy.CLASS,

		/**
		 * @property {CMDBuild.view.administration.localization.common.AdvancedTableGrid}
		 */
		grid: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localization.advancedTable.SectionPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localization.advancedTable.AdvancedTable} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localization.advancedTable.SectionPanel', {
				delegate: this,
				title: '@@ Classes'
			});

			// Shorthand
			this.grid = this.view.grid;

			this.cmfg('onLocalizationAdvancedTableTabCreation', this.view); // Add panel to parent tab panel
		}
	});

})();