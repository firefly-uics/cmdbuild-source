(function () {

	Ext.define('CMDBuild.controller.common.field.filter.advanced.configurator.Configurator', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.service.LoadMask'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFilterAdvancedConfiguratorConfigurationGet',
			'fieldFilterAdvancedConfiguratorConfigurationIsEmpty',
			'fieldFilterAdvancedConfiguratorEntryTypeSelect',
			'fieldFilterAdvancedConfiguratorReset -> controllerTabAttributes, controllerTabFunctons, controllerTabRelations',
			'fieldFilterAdvancedConfiguratorValueGet',
			'fieldFilterAdvancedConfiguratorValueSet'
		],

		/**
		 * @property {CMDBuild.model.common.field.filter.advanced.configurator.Configuration}
		 *
		 * @private
		 */
		configuration: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.Attributes}
		 */
		controllerTabAttributes: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.Functions}
		 */
		controllerTabFunctons: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.Relations}
		 */
		controllerTabRelations: undefined,

		/**
		 * @cfg {CMDBuild.view.common.field.filter.advanced.configurator.ConfiguratorView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.view.common.field.filter.advanced.configurator.ConfiguratorView} configurationObject.view
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			// Build sub-controllers
			this.controllerTabAttributes = Ext.create('CMDBuild.controller.common.field.filter.advanced.configurator.tabs.Attributes', { parentDelegate: this });
			this.controllerTabFunctons = Ext.create('CMDBuild.controller.common.field.filter.advanced.configurator.tabs.Functions', { parentDelegate: this });
			this.controllerTabRelations = Ext.create('CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.Relations', { parentDelegate: this });

			// Build view
			this.view.tabPanel.removeAll();
			this.view.tabPanel.add([
				this.controllerTabAttributes.getView(),
				this.controllerTabFunctons.getView(),
				this.controllerTabRelations.getView()
			]);
		},

		// Configuration property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed}
			 */
			fieldFilterAdvancedConfiguratorConfigurationGet: function (attributePath) {
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
			fieldFilterAdvancedConfiguratorConfigurationIsEmpty: function (attributePath) {
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
			fieldFilterAdvancedConfiguratorConfigurationSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.field.filter.advanced.configurator.Configuration';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 * @param {String} configuration.className
		 * @param {Array} configuration.disabledPanels ['attributes', 'functions', 'relations']
		 * @param {Object} configuration.filter
		 * @param {Object} configuration.scope
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		fieldFilterAdvancedConfiguratorEntryTypeSelect: function (configuration) {
			configuration = Ext.isObject(configuration) ? configuration : {};
			configuration.scope = Ext.isObject(configuration.scope) ? configuration.scope : this;

			// Error handling
				if (!Ext.isString(configuration.className) || Ext.isEmpty(configuration.className))
					return _error('fieldFilterAdvancedConfiguratorEntryTypeSelect(): unmanaged className parameter', this, configuration.className);
			// END: Error handling

			CMDBuild.core.interfaces.service.LoadMask.manage(true, true); // Manually manage LoadMask (show)

			this.fieldFilterAdvancedConfiguratorConfigurationSet({ value: configuration });

			var configurationDisabledPanels = this.cmfg('fieldFilterAdvancedConfiguratorConfigurationGet', CMDBuild.core.constants.Proxy.DISABLED_PANELS),
				requestBarrier = Ext.create('CMDBuild.core.RequestBarrier', {
					id: 'fieldFilterAdvancedConfiguratorEntryTypeSelectBarrier',
					scope: this,
					callback: function () {
						this.manageActiveTabSet();

						CMDBuild.core.interfaces.service.LoadMask.manage(true, false); // Manually manage LoadMask (hide)

						if (Ext.isFunction(configuration.callback))
							Ext.callback(configuration.callback, configuration.scope);
					}
				}),
				tabs = [];

			this.controllerTabAttributes.cmfg('fieldFilterAdvancedConfiguratorConfigurationAttributesEntryTypeSelect', {
				callback: requestBarrier.getCallback('fieldFilterAdvancedConfiguratorEntryTypeSelectBarrier'),
				visible: !Ext.Array.contains(configurationDisabledPanels, 'attributes')
			});

			this.controllerTabFunctons.cmfg('fieldFilterAdvancedConfiguratorConfigurationFunctionsEntryTypeSelect', {
				callback: requestBarrier.getCallback('fieldFilterAdvancedConfiguratorEntryTypeSelectBarrier'),
				visible: !Ext.Array.contains(configurationDisabledPanels, 'functions')
			});

			this.controllerTabRelations.cmfg('fieldFilterAdvancedConfiguratorConfigurationRelationsEntryTypeSelect', {
				callback: requestBarrier.getCallback('fieldFilterAdvancedConfiguratorEntryTypeSelectBarrier'),
				visible: !Ext.Array.contains(configurationDisabledPanels, 'relations')
			});

			requestBarrier.finalize('fieldFilterAdvancedConfiguratorEntryTypeSelectBarrier', true);
		},

		/**
		 * @returns {Object} value
		 */
		fieldFilterAdvancedConfiguratorValueGet: function () {
			var configurationDisabledPanels = this.cmfg('fieldFilterAdvancedConfiguratorConfigurationGet', CMDBuild.core.constants.Proxy.DISABLED_PANELS) || [],
				value = {};

			if (!Ext.Array.contains(configurationDisabledPanels, 'attributes')) {
				var tabAttributesValue = this.controllerTabAttributes.cmfg('fieldFilterAdvancedConfiguratorConfigurationAttributesValueGet');

				if (Ext.isObject(tabAttributesValue) && !Ext.Object.isEmpty(tabAttributesValue))
					value[CMDBuild.core.constants.Proxy.ATTRIBUTE] = tabAttributesValue;
			}

			if (!Ext.Array.contains(configurationDisabledPanels, 'functions')) {
				var tabFunctionsValue = this.controllerTabFunctons.cmfg('fieldFilterAdvancedConfiguratorConfigurationFunctionsValueGet');

				if (Ext.isArray(tabFunctionsValue) && !Ext.isEmpty(tabFunctionsValue))
					value[CMDBuild.core.constants.Proxy.FUNCTIONS] = tabFunctionsValue;
			}

			if (!Ext.Array.contains(configurationDisabledPanels, 'relations')) {
				var tabRelationsValue = this.controllerTabRelations.cmfg('fieldFilterAdvancedConfiguratorConfigurationRelationsValueGet');

				if (Ext.isArray(tabRelationsValue) && !Ext.isEmpty(tabRelationsValue))
					value[CMDBuild.core.constants.Proxy.RELATION] = tabRelationsValue;
			}

			return value;
		},

		/**
		 * @param {Object} value
		 *
		 * @returns {Void}
		 */
		fieldFilterAdvancedConfiguratorValueSet: function (value) {
			this.controllerTabAttributes.cmfg('fieldFilterAdvancedConfiguratorConfigurationAttributesValueSet', value);
			this.controllerTabFunctons.cmfg('fieldFilterAdvancedConfiguratorConfigurationFunctionsValueSet', value);
			this.controllerTabRelations.cmfg('fieldFilterAdvancedConfiguratorConfigurationRelationsValueSet', value);
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		manageActiveTabSet: function () {
			if (Ext.isEmpty(this.view.tabPanel.getActiveTab()))
				this.view.tabPanel.setActiveTab(0);

			this.view.tabPanel.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
		}
	});

})();
