(function () {

	/**
	 * Class to be extended in widget controllers to adapt AbstractController functionalities
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.common.AbstractBaseWidgetController', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

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
		 * @param {Mixed} configurationObject.view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} configurationObject.parentDelegate
		 * @param {Object} configurationObject.widgetConfiguration
		 * @param {Ext.form.Basic} configurationObject.clientForm
		 * @param {CMDBuild.model.CMActivityInstance} configurationObject.card
		 */
		constructor: function(configurationObject) {
			if (!Ext.isEmpty(configurationObject) && !Ext.Object.isEmpty(configurationObject.widgetConfiguration)) {
				this.callParent(arguments);

				this.widgetConfigurationSet({ configurationObject: this.widgetConfiguration }); // Setup widget configuration model

				this.view.delegate = this; // Apply delegate to view
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
				this.widgetConfigurationSet({ configurationObject: this.widgetConfiguration });
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

			_warning('No configured templateResolver instance', this);

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
			return this.widgetConfigurationGet(CMDBuild.core.proxy.CMProxyConstants.ID);
		},

		/**
		 * @param {String}
		 */
		getWidgetLabel: function() {
			return this.widgetConfigurationGet(CMDBuild.core.proxy.CMProxyConstants.LABEL);
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
			 * Attribute could be a single string (attribute name) or an array of strings that declares path to required attribute through model object's properties
			 *
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed}
			 */
			widgetConfigurationGet: function(attributePath) {
				attributePath = Ext.isArray(attributePath) ? attributePath : [attributePath];

				var requiredAttribute = this.widgetConfigurationModel;

				if (!Ext.isEmpty(attributePath))
					Ext.Array.forEach(attributePath, function(attributeName, i, allAttributeNames) {
						if (!Ext.isEmpty(attributeName) && Ext.isString(attributeName))
							if (
								!Ext.isEmpty(requiredAttribute)
								&& Ext.isObject(requiredAttribute)
								&& Ext.isFunction(requiredAttribute.get)
							) { // Model management
								requiredAttribute = requiredAttribute.get(attributeName);
							} else if (
								!Ext.isEmpty(requiredAttribute)
								&& Ext.isObject(requiredAttribute)
							) { // Simple object management
								requiredAttribute = requiredAttribute[attributeName];
							}
					}, this);

				return requiredAttribute;
			},

			/**
			 * @param {String} attributeName
			 *
			 * @returns {Mixed}
			 */
			widgetConfigurationIsAttributeEmpty: function(attributeName) {
				if (!Ext.isEmpty(attributeName) && Ext.isString(attributeName))
					if (
						!Ext.isEmpty(this.widgetConfigurationModel)
						&& Ext.isObject(this.widgetConfigurationModel)
						&& Ext.isFunction(this.widgetConfigurationModel.get)
					) { // Model management
						return Ext.isEmpty(this.widgetConfigurationModel.get(attributeName));
					} else if (
						!Ext.isEmpty(this.widgetConfigurationModel)
						&& Ext.isObject(this.widgetConfigurationModel)
					) { // Simple object management
						return Ext.isEmpty(this.widgetConfigurationModel[attributeName]);
					}

				return true;
			},

			/**
			 * Setup all widgetConfigurationModel or only one model property. Needs to be extended from widget controller to set value of widgetConfigurationModel.
			 *
			 * @param {Object} parameters
			 * @param {Object} parameters.configurationObject
			 * @param {String} parameters.propertyName
			 *
			 * @returns {Mixed}
			 *
			 * @abstract
			 */
			widgetConfigurationSet: function(parameters) {
				if (!Ext.isEmpty(parameters)) {
					var configurationObject = parameters.configurationObject;
					var propertyName = parameters.propertyName;

					// Single property management
					if (!Ext.isEmpty(propertyName) && Ext.isString(propertyName))
						if (
							!Ext.isEmpty(this.widgetConfigurationModel)
							&& Ext.isObject(this.widgetConfigurationModel)
							&& Ext.isFunction(this.widgetConfigurationModel.set)
						) { // Model management
							return this.widgetConfigurationModel.set(propertyName, configurationObject);
						} else if (
							!Ext.isEmpty(this.widgetConfigurationModel)
							&& Ext.isObject(this.widgetConfigurationModel)
						) { // Simple object management
							return this.widgetConfigurationModel[propertyName] = configurationObject;
						}
				}

				// Extends to implement full model setup
			},

		// WidgetCntroller methods
			/**
			 * @param {String} propertyName
			 *
			 * @returns {Mixed}
			 */
			widgetControllerPropertyGet: function(propertyName) {
				if (!Ext.isEmpty(this[propertyName]))
					return this[propertyName];

				return null;
			}
	});

})();