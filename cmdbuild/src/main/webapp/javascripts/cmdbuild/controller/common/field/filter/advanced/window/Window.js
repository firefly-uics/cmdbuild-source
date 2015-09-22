(function() {

	Ext.define('CMDBuild.controller.common.field.filter.advanced.window.Window', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.filter.Groups'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.Advanced}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFilterAdvancedWindowAddTab',
			'onFieldFilterAdvancedWindowAbortButtonClick',
			'onFieldFilterAdvancedWindowBeforeShow',
			'onFieldFilterAdvancedWindowConfirmButtonClick',
			'onFieldFilterAdvancedWindowPresetGridSelect',
			'onFieldFilterAdvancedWindowPresetGridStoreLoad',
			'onFieldFilterAdvancedWindowSetData -> controllerTabAttributes, controllerTabFunctions, controllerTabRelations',
			'onFieldFilterAdvancedWindowShow'
		],

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.panels.Attributes}
		 */
		controllerTabAttributes: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.panels.Functions}
		 */
		controllerTabFunctions: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.panels.relations.Relations}
		 */
		controllerTabRelations: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {Ext.tab.Panel}
		 */
		tabPanel: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.Window}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.filter.advanced.Advanced} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.field.filter.advanced.window.Window', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;
			this.tabPanel = this.view.tabPanel;

			// Build sub controllers
			this.controllerTabAttributes = Ext.create('CMDBuild.controller.common.field.filter.advanced.window.panels.Attributes', { parentDelegate: this });
			this.controllerTabFunctions = Ext.create('CMDBuild.controller.common.field.filter.advanced.window.panels.Functions', { parentDelegate: this });
			this.controllerTabRelations = Ext.create('CMDBuild.controller.common.field.filter.advanced.window.panels.relations.Relations', { parentDelegate: this });
		},

		/**
		 * @property {Mixed} panel
		 */
		fieldFilterAdvancedWindowAddTab: function(panel) {
			if (!Ext.isEmpty(panel))
				this.tabPanel.add(panel);
		},

		onFieldFilterAdvancedWindowAbortButtonClick: function() {
			this.view.hide();
		},

		/**
		 * @returns {Boolean}
		 */
		onFieldFilterAdvancedWindowBeforeShow: function() {
			if (this.cmfg('fieldFilterAdvancedFilterIsEmpty'))
				CMDBuild.core.Message.warning(null, CMDBuild.Translation.warnings.toSetAFilterYouMustBeforeSelectAClass, false);

			return !this.cmfg('fieldFilterAdvancedFilterIsEmpty');
		},

		/**
		 * Fill filter model with tab's data
		 */
		onFieldFilterAdvancedWindowConfirmButtonClick: function() {
			var filterConfigurationObject = {};

			if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'attribute'))
				Ext.apply(filterConfigurationObject, this.controllerTabAttributes.cmfg('onFieldFilterAdvancedWindowAttributesGetData'));

			if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'function'))
				Ext.apply(filterConfigurationObject, this.controllerTabFunctions.cmfg('onFieldFilterAdvancedWindowFunctionsGetData'));

			if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'relation'))
				Ext.apply(filterConfigurationObject, this.controllerTabRelations.cmfg('onFieldFilterAdvancedWindowRelationsGetData'));

			this.cmfg('fieldFilterAdvancedFilterSet', {
				filterObject: filterConfigurationObject,
				propertyName: CMDBuild.core.constants.Proxy.CONFIGURATION
			});

			this.onFieldFilterAdvancedWindowAbortButtonClick();
		},

		/**
		 * @param {CMDBuild.model.common.field.filter.advanced.Filter} filter
		 */
		onFieldFilterAdvancedWindowPresetGridSelect: function(filter) {
			if (!Ext.isEmpty(filter)) {
				this.grid.getSelectionModel().deselectAll();

				this.cmfg('onFieldFilterAdvancedWindowSetData', filter);
			}
		},

		/**
		 * Include in store also System filters to be consistent with checkbox state
		 */
		onFieldFilterAdvancedWindowPresetGridStoreLoad: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('fieldFilterAdvancedSelectedClassGet', CMDBuild.core.constants.Proxy.NAME);

			if (this.grid.includeSystemFiltersCheckbox.getValue())
				CMDBuild.core.proxy.filter.Groups.readAll({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.FILTERS];

						this.grid.getStore().loadData(decodedResponse, true);
					}
				});
		},

		/**
		 * Setup tab visibility based on field configuration
		 */
		onFieldFilterAdvancedWindowShow: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('fieldFilterAdvancedSelectedClassGet', CMDBuild.core.constants.Proxy.NAME);

			this.grid.getStore().load({ params: params });

			this.setViewTitle(this.cmfg('fieldFilterAdvancedSelectedClassGet', CMDBuild.core.constants.Proxy.TEXT)); // TODO: waiting for refactor (description)

			// Refresh tab configuration (sorted)
			this.tabPanel.removeAll(true);

			this.controllerTabAttributes.cmfg('onFieldFilterAdvancedWindowAttributesTabBuild');
			this.controllerTabRelations.cmfg('onFieldFilterAdvancedWindowRelationsTabBuild');
			this.controllerTabFunctions.cmfg('onFieldFilterAdvancedWindowFunctionsTabBuild');

			this.tabPanel.setActiveTab(0); // Configuration parameter doesn't work because panels are added
		},

		/**
		 * Forward method
		 */
		show: function() {
			if (!Ext.isEmpty(this.view))
				this.view.show();
		}
	});

})();