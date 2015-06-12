(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTable.AdvancedTable', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
//			'CMDBuild.core.proxy.Attributes',
			'CMDBuild.core.proxy.CMProxyConstants',
//			'CMDBuild.core.proxy.Classes',
//			'CMDBuild.core.proxy.localizations.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Localizations}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'localizationsAdvancedTableBuildColumns'
		],

		sectionClassesController: undefined, // TODO

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
			this.sectionClassesController = Ext.create('CMDBuild.controller.administration.localizations.advancedTable.SectionClasses', { parentDelegate: this });

			if (!Ext.isEmpty(this.sectionClassesController.getView()))
				this.view.add(this.sectionClassesController.getView());

			this.view.setActiveTab(0);
		},

		/**
		 * @param {CMDBuild.model.localizations.Localization} languageObject
		 *
		 * @return {Ext.grid.column.Column} or null
		 */
		buildColumn: function(languageObject) { // TODO static???
			if (!Ext.isEmpty(languageObject))
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

			return null;
		},

		/**
		 * Build TreePanel columns only with languages with translations
		 *
		 * @return {Array} columnsArray
		 */
		localizationsAdvancedTableBuildColumns: function() { // TODO static??
			var columnsArray = [
				{
					xtype: 'treecolumn',
					text: '@@ Translation object',
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.OBJECT,
					width: 300,
					// locked: true, // There is a performance issue in ExtJs 4.2.0 without locked columns all is fine
					sortable: false,
					draggable: false
				},
				{
					text: '@@ defaultTranslation',
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.DEFAULT,
					width: 300,
					sortable: false,
					draggable: false
				}
			];

			Ext.Array.forEach(CMDBuild.Config.localization.get(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES_WITH_LOCALIZATIONS), function(language, i, allLanguages) {
				columnsArray.push(this.buildColumn(language));
			}, this);

			return columnsArray;
		}
	});

})();