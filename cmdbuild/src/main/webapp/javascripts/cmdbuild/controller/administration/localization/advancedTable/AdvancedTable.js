(function() {

	Ext.define('CMDBuild.controller.administration.localization.advancedTable.AdvancedTable', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.localization.advancedTable.TreeStore'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.Localization}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLocalizationAdvancedTableBuildColumns',
			'onLocalizationAdvancedTableBuildStore',
			'onLocalizationAdvancedTableCollapseAll',
			'onLocalizationAdvancedTableExpandAll',
			'onLocalizationAdvancedTableTabCreation'
		],

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionClass}
		 */
		sectionControllerClasses: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionDomain}
		 */
		sectionControllerDomains: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionFilter}
		 */
		sectionControllerFilters: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionLookup}
		 */
		sectionControllerLookup: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionMenu}
		 */
		sectionControllerMenu: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionProcess}
		 */
		sectionControllerProcesses: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionReport}
		 */
		sectionControllerReports: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionView}
		 */
		sectionControllerViews: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localization.advancedTable.AdvancedTableView}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localization.Localization} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localization.advancedTable.AdvancedTableView', {
				delegate: this
			});

			// Build tabs (in display order)
			this.sectionControllerClasses = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionClass', { parentDelegate: this });
			this.sectionControllerProcesses = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionProcess', { parentDelegate: this });
			this.sectionControllerDomains = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionDomain', { parentDelegate: this });
			this.sectionControllerViews = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionView', { parentDelegate: this });
			this.sectionControllerFilters = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionFilter', { parentDelegate: this });
			this.sectionControllerLookup = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionLookup', { parentDelegate: this });
			this.sectionControllerReports = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionReport', { parentDelegate: this });
			this.sectionControllerMenu = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionMenu', { parentDelegate: this });

			this.view.setActiveTab(0);

			this.view.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
		},

		/**
		 * @param {CMDBuild.model.localization.Localization} languageObject
		 *
		 * @return {Ext.grid.column.Column} or null
		 */
		buildColumn: function(languageObject) {
			if (!Ext.isEmpty(languageObject)) {
				return Ext.create('Ext.grid.column.Column', {
					dataIndex: languageObject.get(CMDBuild.core.constants.Proxy.TAG),
					languageDescription: languageObject.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
					text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/'
						+ languageObject.get(CMDBuild.core.constants.Proxy.TAG) + '.png" /> '
						+ languageObject.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
					width: 300,
					sortable: false,
					draggable: false,

					editor: { xtype: 'textfield' }
				});
			}

			return null;
		},

		/**
		 * Build TreePanel columns only with languages with translations
		 *
		 * @return {Array} columnsArray
		 */
		onLocalizationAdvancedTableBuildColumns: function() {
			var enabledLanguages = CMDBuild.configuration.localization.getEnabledLanguages();
			var columnsArray = [
				{
					xtype: 'treecolumn',
					dataIndex: CMDBuild.core.constants.Proxy.TEXT,
					text: '@@ Translation object',
					width: 300,
					// locked: true, // There is a performance issue in ExtJs 4.2.0 without locked columns all is fine
					sortable: false,
					draggable: false
				},
				{
					dataIndex: CMDBuild.core.constants.Proxy.DEFAULT,
					text: '@@ Default translation',
					width: 300,
					sortable: false,
					draggable: false
				}
			];
			var languagesColumnsArray = [];

			Ext.Object.each(enabledLanguages, function(key, value, myself) {
				languagesColumnsArray.push(this.buildColumn(value));
			}, this);

			// Sort languages columns with alphabetical sort order
			CMDBuild.core.Utils.objectArraySort(languagesColumnsArray, 'languageDescription');

			return Ext.Array.push(columnsArray, languagesColumnsArray);
		},

		/**
		 * @return {Ext.data.TreeStore}
		 */
		onLocalizationAdvancedTableBuildStore: function() {
			return Ext.create('Ext.data.TreeStore', {
				model: 'CMDBuild.model.localization.advancedTable.TreeStore',

				root: {
					text: 'ROOT',
					expanded: true,
					children: []
				}
			});
		},

		/**
		 * @param {CMDBuild.view.administration.localization.common.AdvancedTableGrid}
		 */
		onLocalizationAdvancedTableCollapseAll: function(gridPanel) {
			CMDBuild.LoadMask.get().show();
			Ext.Function.defer(function() { // HACK: to fix expandAll bug that don't displays loeadMask
				gridPanel.collapseAll(function() {
					CMDBuild.LoadMask.get().hide();
				});
			}, 100, this);
		},

		/**
		 * @param {CMDBuild.view.administration.localization.common.AdvancedTableGrid}
		 */
		onLocalizationAdvancedTableExpandAll: function(gridPanel) {
			CMDBuild.LoadMask.get().show();
			Ext.Function.defer(function() { // HACK: to fix expandAll bug that don't displays loeadMask
				gridPanel.expandAll(function() {
					CMDBuild.LoadMask.get().hide();
				});
			}, 100, this);
		},

		/**
		 * @param {Mixed} panel
		 */
		onLocalizationAdvancedTableTabCreation: function(panel) {
			if (!Ext.isEmpty(panel))
				this.view.add(panel);
		}
	});

})();