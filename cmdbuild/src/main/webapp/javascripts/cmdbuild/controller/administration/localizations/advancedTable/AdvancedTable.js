(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTable.AdvancedTable', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Localizations}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onAdvancedTableBuildColumns',
			'onAdvancedTableBuildStore',
			'onAdvancedTableTabCreation',
		],

		/**
		 * @property {CMDBuild.controller.administration.localizations.advancedTable.SectionClasses}
		 */
		sectionControllerClasses: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localizations.advancedTable.SectionDomains}
		 */
		sectionControllerDomains: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.advancedTable.AdvancedTableView}
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

			this.view = Ext.create('CMDBuild.view.administration.localizations.advancedTable.AdvancedTableView', {
				delegate: this
			});

			// Build tabs
			this.sectionControllerClasses = Ext.create('CMDBuild.controller.administration.localizations.advancedTable.SectionClasses', { parentDelegate: this });
			this.sectionControllerDomains = Ext.create('CMDBuild.controller.administration.localizations.advancedTable.SectionDomains', { parentDelegate: this });

			this.view.setActiveTab(0);

			this.view.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
		},

		/**
		 * @param {CMDBuild.model.localizations.Localization} languageObject
		 *
		 * @return {Ext.grid.column.Column} or null
		 */
		buildColumn: function(languageObject) {
			if (!Ext.isEmpty(languageObject)) {
				return Ext.create('Ext.grid.column.Column', {
					dataIndex: languageObject.get(CMDBuild.core.proxy.CMProxyConstants.TAG),
					text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/'
						+ languageObject.get(CMDBuild.core.proxy.CMProxyConstants.TAG) + '.png" /> '
						+ languageObject.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION),
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
			var enabledLanguages = CMDBuild.configuration[CMDBuild.core.proxy.CMProxyConstants.LOCALIZATION].getEnabledLanguages();
			var columnsArray = [
				{
					xtype: 'treecolumn',
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.OBJECT,
					text: '@@ Translation object',
					width: 300,
					// locked: true, // There is a performance issue in ExtJs 4.2.0 without locked columns all is fine
					sortable: false,
					draggable: false
				},
				{
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.DEFAULT,
					text: '@@ defaultTranslation',
					width: 300,
					sortable: false,
					draggable: false
				}
			];
_debug('enabledLanguages', enabledLanguages);
			Ext.Object.each(enabledLanguages, function(key, value, myself) {
				columnsArray.push(this.buildColumn(value));
			}, this);

			return columnsArray;
		},

		/**
		 * @return {Ext.data.TreeStore}
		 */
		onAdvancedTableBuildStore: function() {
			return Ext.create('Ext.data.TreeStore', {
				model: 'CMDBuild.model.localizations.advancedTable.TreeStore',
				root: {
					text: 'ROOT',
					expanded: true,
					children: []
				}
			});
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