(function() {

	Ext.define('CMDBuild.controller.administration.localizations.advancedTranslationsTable.Main', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.Attributes',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel}
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

			this.view = Ext.create('CMDBuild.view.administration.localizations.advancedTranslationsTable.MainPanel', {
				delegate: this
			});

			// Build tabs
			this.sectionClassesController = Ext.create('CMDBuild.controller.administration.localizations.advancedTranslationsTable.SectionClasses', { parentDelegate: this });
			this.view.add(this.sectionClassesController.getView());

			this.view.setActiveTab(0);
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
				case 'buildColumns':
					return this.buildColumns();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Build TreePanel columns only with languages with translations
		 */
		buildColumns: function() {
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
				columnsArray.push(this.view.buildColumn(language));
			}, this);

			return columnsArray;
		},

		/**
		 * @return {CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel}
		 */
		getView: function() {
			return this.view;
		}
	});

})();