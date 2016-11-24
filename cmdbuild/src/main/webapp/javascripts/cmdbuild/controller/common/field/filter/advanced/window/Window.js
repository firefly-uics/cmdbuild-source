(function () {

	/**
	 * To get the result of filter window you need to implement "onFieldFilterAdvancedWindowgetEndpoint" in cmfg structure
	 */
	Ext.define('CMDBuild.controller.common.field.filter.advanced.window.Window', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.common.field.filter.advanced.window.Window'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFilterAdvancedWindowConfigurationGet',
			'fieldFilterAdvancedWindowConfigurationIsEmpty',
			'fieldFilterAdvancedWindowConfigureAndShow',
			'fieldFilterAdvancedWindowSelectedRecordGet',
			'fieldFilterAdvancedWindowSelectedRecordIsEmpty',
			'onFieldFilterAdvancedWindowAbortButtonClick',
			'onFieldFilterAdvancedWindowBeforeShow',
			'onFieldFilterAdvancedWindowConfirmButtonClick',
			'onFieldFilterAdvancedWindowPresetGridSelect',
			'onFieldFilterAdvancedWindowPresetGridStoreLoad',
			'onFieldFilterAdvancedWindowSetData -> controllerTabAttributes, controllerTabFunctions, controllerTabRelations'
		],

		/**
		 * @property {CMDBuild.model.common.field.filter.advanced.window.Configuration}
		 *
		 * @private
		 */
		configuration: {},

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.panels.ColumnPrivileges}
		 */
		controllerTabColumnPrivileges: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.Window}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.filter.advanced.Advanced} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.field.filter.advanced.window.Window', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;

			// Build sub controllers
			this.controllerTabColumnPrivileges = Ext.create('CMDBuild.controller.common.field.filter.advanced.window.panels.ColumnPrivileges', {
				parentDelegate: this,
				view: this.view.columnPrivileges
			});
		},

		/**
		 * @param {Object} configuration
		 * @param {Array} configuration.attributesPrivileges
		 * @param {String} configuration.className
		 * @param {String} configuration.classDescription
		 * @param {Array} configuration.disabledFeatures
		 * @param {Object} configuration.filter
		 * @param {String} configuration.mode
		 * @param {CMDBuild.model.userAndGroup.group.privileges.GridRecord} configuration.record
		 *
		 * @returns {Void}
		 */
		fieldFilterAdvancedWindowConfigureAndShow: function (configuration) {
			configuration = Ext.isObject(configuration) ? configuration : {};
			configuration.filter = Ext.isObject(configuration.filter) ? configuration.filter : {};

			// Error handling
				if (!Ext.isString(configuration.className) || Ext.isEmpty(configuration.className))
					return _error('fieldFilterAdvancedWindowConfigureAndShow(): unmanaged className parameter', this, configuration.className);
			// END: Error handling

			this.fieldFilterAdvancedWindowConfigurationSet({ value: configuration });

			this.view.fieldFilter.configure({
				className: this.cmfg('fieldFilterAdvancedWindowConfigurationGet', CMDBuild.core.constants.Proxy.CLASS_NAME),
				disabledFeatures: this.cmfg('fieldFilterAdvancedWindowConfigurationGet', CMDBuild.core.constants.Proxy.DISABLED_FEATURES),
				disabledPanels: this.cmfg('fieldFilterAdvancedWindowConfigurationGet', CMDBuild.core.constants.Proxy.DISABLED_PANELS),
				scope: this,
				callback: function () {
					this.view.fieldFilter.setValue(configuration.filter);
				}
			});

			this.view.show();
		},

		// Configuration property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed}
			 */
			fieldFilterAdvancedWindowConfigurationGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			fieldFilterAdvancedWindowConfigurationIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			fieldFilterAdvancedWindowConfigurationSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.field.filter.advanced.window.Configuration';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @returns {Void}
		 */
		onFieldFilterAdvancedWindowAbortButtonClick: function () {
			this.view.close();
		},

		/**
		 * @returns {Boolean}
		 */
		onFieldFilterAdvancedWindowBeforeShow: function () {
			if (!this.cmfg('fieldFilterAdvancedWindowConfigurationIsEmpty')) {
				this.setViewTitle(this.cmfg('fieldFilterAdvancedWindowConfigurationGet', CMDBuild.core.constants.Proxy.CLASS_DESCRIPTION));

				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('fieldFilterAdvancedWindowConfigurationGet', CMDBuild.core.constants.Proxy.CLASS_NAME);

				this.grid.getStore().load({ params: params });

				this.controllerTabColumnPrivileges.cmfg('onFieldFilterAdvancedWindowColumnPrivilegesTabBuild');

				return true;
			}

			return false;
		},

		/**
		 * Fill filter model with tab's data
		 *
		 * @returns {Void}
		 */
		onFieldFilterAdvancedWindowConfirmButtonClick: function () {
			if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'columnPrivileges')) {
				var activeTab = this.view.windowTabPanel.getActiveTab(),
					columnPrivilegesArray = [];

				if (Ext.getClassName(activeTab) == 'CMDBuild.view.common.field.filter.advanced.window.panels.columnPrivileges.ColumnPrivilegesView') {
					if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'columnPrivileges'))
						columnPrivilegesArray = this.controllerTabColumnPrivileges.cmfg('onFieldFilterAdvancedWindowColumnPrivilegesGetData');

					this.cmfg('onFieldFilterAdvancedWindowgetEndpoint', { columnPrivileges: columnPrivilegesArray });
				} else if (Ext.getClassName(activeTab) == 'Ext.panel.Panel') {
					this.cmfg('onFieldFilterAdvancedWindowgetEndpoint', { filter: this.view.fieldFilter.getValue() });
				}
			} else {
				this.cmfg('onFieldFilterAdvancedWindowgetEndpoint', { filter: this.view.fieldFilter.getValue() });
			}

			this.cmfg('onFieldFilterAdvancedWindowAbortButtonClick');
		},

		/**
		 * @param {CMDBuild.model.common.field.filter.advanced.Filter} filter
		 *
		 * @returns {Void}
		 */
		onFieldFilterAdvancedWindowPresetGridSelect: function (filter) {
			if (!Ext.isEmpty(filter)) {
				this.grid.getSelectionModel().deselectAll();

				this.cmfg('onFieldFilterAdvancedWindowSetData', filter);
			}
		},

		/**
		 * Include in store also Users filters to be consistent with checkbox state
		 *
		 * @returns {Void}
		 */
		onFieldFilterAdvancedWindowPresetGridStoreLoad: function () {
			if (this.grid.includeUsersFiltersCheckbox.getValue()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('fieldFilterAdvancedWindowConfigurationGet', CMDBuild.core.constants.Proxy.CLASS_NAME);

				CMDBuild.proxy.common.field.filter.advanced.window.Window.readFilterUser({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.FILTERS];

						this.grid.getStore().loadData(decodedResponse, true);
					}
				});
			}
		}
	});

})();
