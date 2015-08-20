(function() {

	Ext.define('CMDBuild.controller.administration.localization.advancedTable.AdvancedTable', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.Constants',
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
			'onAdvancedTableBuildColumns',
			'onAdvancedTableBuildStore',
			'onAdvancedTableCollapseAll',
			'onAdvancedTableExpandAll',
			'onAdvancedTableTabCreation'
		],

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionClasses}
		 */
		sectionControllerClasses: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionDomains}
		 */
		sectionControllerDomains: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionFilters}
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
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionProcesses}
		 */
		sectionControllerProcesses: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionReports}
		 */
		sectionControllerReports: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.SectionViews}
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
			this.sectionControllerClasses = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionClasses', { parentDelegate: this });
			this.sectionControllerProcesses = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionProcesses', { parentDelegate: this });
			this.sectionControllerDomains = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionDomains', { parentDelegate: this });
			this.sectionControllerViews = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionViews', { parentDelegate: this });
			this.sectionControllerFilters = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionFilters', { parentDelegate: this });
			this.sectionControllerLookup = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionLookup', { parentDelegate: this });
			this.sectionControllerReports = Ext.create('CMDBuild.controller.administration.localization.advancedTable.SectionReports', { parentDelegate: this });
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
					dataIndex: languageObject.get(CMDBuild.core.proxy.Constants.TAG),
					languageDescription: languageObject.get(CMDBuild.core.proxy.Constants.DESCRIPTION),
					text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/'
						+ languageObject.get(CMDBuild.core.proxy.Constants.TAG) + '.png" /> '
						+ languageObject.get(CMDBuild.core.proxy.Constants.DESCRIPTION),
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
		onAdvancedTableBuildColumns: function() {
			var enabledLanguages = CMDBuild.configuration[CMDBuild.core.proxy.Constants.LOCALIZATION].getEnabledLanguages();
			var columnsArray = [
				{
					xtype: 'treecolumn',
					dataIndex: CMDBuild.core.proxy.Constants.TEXT,
					text: '@@ Translation object',
					width: 300,
					// locked: true, // There is a performance issue in ExtJs 4.2.0 without locked columns all is fine
					sortable: false,
					draggable: false
				},
				{
					dataIndex: CMDBuild.core.proxy.Constants.DEFAULT,
					text: '@@ defaultTranslation',
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
		onAdvancedTableBuildStore: function() {
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
		onAdvancedTableCollapseAll: function(gridPanel) {
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
		onAdvancedTableExpandAll: function(gridPanel) {
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
		onAdvancedTableTabCreation: function(panel) {
			if (!Ext.isEmpty(panel))
				this.view.add(panel);
		}
	});

})();