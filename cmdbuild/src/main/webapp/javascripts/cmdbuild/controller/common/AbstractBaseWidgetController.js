(function () {

	/**
	 * External requires to avoid overrides from classes that extends
	 */
	Ext.require(['CMDBuild.core.constants.Global']);

	/**
	 * Class to be extended in widget controllers to adapt AbstractController functionalities
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.common.AbstractBaseWidgetController', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Ext.data.Model or CMDBuild.model.CMActivityInstance}
		 */
		card: undefined,

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @cfg {Boolean}
		 */
		enableViewDelegateInject: true,

		/**
		 * @cfg {Boolean}
		 */
		enableWidgetConfigurationSetup: true,

		/**
		 * Multiple widget instances data storage buffer
		 *
		 * @property {Object}
		 *
		 * @private
		 */
		instancesDataStorage: {},

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * @property {Object}
		 */
		view: undefined,

		/**
		 * Plain widget configuration object
		 *
		 * @property {Object}
		 */
		widgetConfiguration: undefined,

		/**
		 * Widget configuration model built with WidgetConfiguration methods
		 *
		 * @property {Object}
		 *
		 * @private
		 */
		widgetConfigurationModel: undefined,

		/**
		 * @cfg {String}
		 */
		widgetConfigurationModelClassName: undefined,

		statics: {
			/**
			 * Old implementation to be used in new widgets
			 *
			 * @param {Object} model
			 *
			 * @return {Object} out
			 */
			getTemplateResolverServerVars: function(model) {
				var out = {};
				var pi = null;

				if (!Ext.isEmpty(model)) {
					if (Ext.getClassName(model) == 'CMDBuild.model.CMActivityInstance') {
						// Retrieve the process instance because it stores the data. this.card has only the varibles to show in this step (is the activity instance)
						pi = _CMWFState.getProcessInstance();
					} else if (Ext.getClassName(model) == 'CMDBuild.model.CMProcessInstance') {
						pi = model;
					}

					if (!Ext.isEmpty(pi) && Ext.isFunction(pi.getValues)) { // The processes use a new serialization. Add backward compatibility attributes to the card values
						out = Ext.apply({
							'Id': pi.get('Id'),
							'IdClass': pi.get('IdClass'),
							'IdClass_value': pi.get('IdClass_value')
						}, pi.getValues());
					} else {
						out = model.raw || model.data;
					}
				}

				return out;
			}
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMWidgetManager} configurationObject.view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} configurationObject.parentDelegate
		 * @param {Object} configurationObject.widgetConfiguration
		 * @param {Ext.form.Basic} configurationObject.clientForm
		 * @param {CMDBuild.model.CMActivityInstance} configurationObject.card
		 */
		constructor: function(configurationObject) {
			if (!Ext.isEmpty(configurationObject) && !Ext.Object.isEmpty(configurationObject.widgetConfiguration)) {
				this.callParent(arguments);

				// Setup widget configuration
				if (this.enableWidgetConfigurationSetup)
					this.widgetConfigurationSet({ value: this.widgetConfiguration }); // Setup widget configuration model

				// Inject delegate to view
				if (this.enableViewDelegateInject)
					this.view.delegate = this;
			} else {
				_error('wrong or empty widget view or configuration object', this);
			}
		},

		/**
		 * @abstract
		 */
		beforeActiveView: function() {
			// Setup widgetConfiguration on widget view activation to switch configuration on multiple instances
			if (!Ext.isEmpty(this.widgetConfiguration))
				this.widgetConfigurationSet({ value: this.widgetConfiguration });
		},

		/**
		 * Executed before window hide perform
		 *
		 * @abstract
		 */
		beforeHideView: Ext.emptyFn,

		/**
		 * @return {Object or null}
		 */
		getData: function() {
			return null;
		},

		/**
		 * @param {String} variableName
		 *
		 * @return {Mixed}
		 */
		getVariable: function(variableName) {
			if (!Ext.isEmpty(this.templateResolver) && Ext.isFunction(this.templateResolver.getVariable))
				return this.templateResolver.getVariable(variableName);

			_warning('templateResolver not instantiated', this);

			return undefined;
		},

		/**
		 * @return {Object}
		 */
		getTemplateResolverServerVars: function() {
			if (!Ext.isEmpty(this.card))
				return this.statics().getTemplateResolverServerVars(this.card);

			return {};
		},

		/**
		 * @return {Number}
		 */
		getWidgetId: function() {
			return this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.ID);
		},

		/**
		 * @param {String}
		 */
		getWidgetLabel: function() {
			return this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.LABEL);
		},

		// InstancesDataStorage methods (multiple widget instances support)
			/**
			 * @returns {Mixed} or null
			 */
			instancesDataStorageGet: function() {
				if (!Ext.isEmpty(this.getWidgetId()) && !Ext.isEmpty(this.instancesDataStorage[this.getWidgetId()]))
					return this.instancesDataStorage[this.getWidgetId()];

				return null;
			},

			/**
			 * @returns {Boolean}
			 */
			instancesDataStorageIsEmpty: function() {
				if (!Ext.isEmpty(this.getWidgetId()))
					return Ext.isEmpty(this.instancesDataStorage[this.getWidgetId()]);

				return true;
			},

			instancesDataStorageReset: function() {
				this.instancesDataStorage = {};
			},

			/**
			 * @param {Mixed} instanceData
			 */
			instancesDataStorageSet: function(instanceData) {
				if (!Ext.isEmpty(this.getWidgetId()) && !Ext.isEmpty(instanceData))
					this.instancesDataStorage[this.getWidgetId()] = instanceData;
			},

		/**
		 * @return {Boolean}
		 */
		isBusy: function() {
			return false;
		},

		/**
		 * @return {Boolean}
		 */
		isValid: function() {
			return true;
		},

		/**
		 * @param {Array} callbackChainArray
		 */
		onBeforeSave: function(callbackChainArray, i) {
			if (!Ext.isEmpty(callbackChainArray[i])) {
				var callbackObject = callbackChainArray[i];

				Ext.callback(callbackObject.fn, callbackObject.scope, [callbackChainArray, i + 1]);
			}
		},

		/**
		 * @abstract
		 */
		onEditMode: Ext.emptyFn,

		// WidgetConfiguration methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed}
			 */
			widgetConfigurationGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'widgetConfigurationModel';

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			widgetConfigurationIsEmpty: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'widgetConfigurationModel';

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 * @param {String} parameters.modelName
			 * @param {Object} parameters.value
			 * @param {String} parameters.propertyName
			 *
			 * @returns {Mixed}
			 *
			 * @abstract
			 */
			widgetConfigurationSet: function(parameters) {
				if (Ext.isEmpty(this.widgetConfigurationModelClassName) || !Ext.isString(this.widgetConfigurationModelClassName))
					return _error('widgetConfigurationModelClassName parameter not configured', this);

				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'widgetConfigurationModel';
				parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = this.widgetConfigurationModelClassName;

				return this.propertyManageSet(parameters);
			}
	});

})();