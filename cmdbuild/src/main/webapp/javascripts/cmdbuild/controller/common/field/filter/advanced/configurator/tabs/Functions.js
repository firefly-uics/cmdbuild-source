(function () {

	Ext.define('CMDBuild.controller.common.field.filter.advanced.configurator.tabs.Functions', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.configurator.Configurator}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFilterAdvancedConfiguratorConfigurationFunctionsEntryTypeSelect',
			'fieldFilterAdvancedConfiguratorConfigurationFunctionsReset = fieldFilterAdvancedConfiguratorReset',
			'fieldFilterAdvancedConfiguratorConfigurationFunctionsValueGet',
			'fieldFilterAdvancedConfiguratorConfigurationFunctionsValueSet'
		],

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.configurator.tabs.functions.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.configurator.tabs.functions.FunctionsView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.filter.advanced.configurator.Configurator} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.tabs.functions.FunctionsView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Object} parameters.scope
		 * @param {Boolean} parameters.visible
		 *
		 * @returns {Void}
		 */
		fieldFilterAdvancedConfiguratorConfigurationFunctionsEntryTypeSelect: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.scope = Ext.isObject(parameters.scope) ? parameters.scope : this;
			parameters.visible = Ext.isBoolean(parameters.visible) ? parameters.visible : false;

			this.view.tab.setVisible(parameters.visible); // Show/Hide tab

			if (Ext.isFunction(parameters.callback))
				Ext.callback(parameters.callback, parameters.scope);
		},

		/**
		 * @returns {Void}
		 */
		fieldFilterAdvancedConfiguratorConfigurationFunctionsReset: function () {
			this.form.fieldFunction.reset();
		},

		/**
		 * @returns {Array} out
		 */
		fieldFilterAdvancedConfiguratorConfigurationFunctionsValueGet: function () {
			var out = [];

			if (!Ext.isEmpty(this.form.fieldFunction) && !Ext.isEmpty(this.form.fieldFunction.getValue())) {
				var filterObject = {};
				filterObject[CMDBuild.core.constants.Proxy.NAME] = this.form.fieldFunction.getValue();

				out.push(filterObject);
			}

			return out;
		},

		/**
		 * @param {Object} filter
		 *
		 * @returns {Void}
		 */
		fieldFilterAdvancedConfiguratorConfigurationFunctionsValueSet: function (filter) {
			if (
				Ext.isObject(filter) && !Ext.Object.isEmpty(filter)
				&& Ext.isArray(filter[CMDBuild.core.constants.Proxy.FUNCTIONS]) && !Ext.isEmpty(filter[CMDBuild.core.constants.Proxy.FUNCTIONS])
			) {
				this.form.fieldFunction.setValue(filter[CMDBuild.core.constants.Proxy.FUNCTIONS][0][CMDBuild.core.constants.Proxy.NAME]);
			}
		}
	});

})();
