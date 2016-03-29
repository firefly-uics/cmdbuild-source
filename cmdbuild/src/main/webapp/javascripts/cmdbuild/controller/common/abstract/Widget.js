(function () {

	// External implementation to avoid overrides
	Ext.require([
		'CMDBuild.core.constants.Global',
		'CMDBuild.core.constants.Proxy'
	]);

	/**
	 * Class to be extended in widget controllers to adapt CMDBuild.controller.common.abstract.Base functionalities
	 *
	 * Required managed methods:
	 * 	- beforeActiveView
	 * 	- beforeHideView
	 * 	- isBusy
	 * 	- isValid
	 * 	- onEditMode
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.common.abstract.Widget', {
		extend: 'CMDBuild.controller.common.abstract.Base',

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
		enableDelegateApply: true,

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
			 * @returns {Object} out
			 */
			getTemplateResolverServerVars: function (model) {
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
		constructor: function (configurationObject) {
			if (
				!Ext.Object.isEmpty(configurationObject)
				&& !Ext.isEmpty(configurationObject.view)
				&& !Ext.Object.isEmpty(configurationObject.widgetConfiguration)
			) {
				// Add default managed functions
				this.cmfgCatchedFunctions.push('getLabel');

				// Generate a unique ID for widget
				this.generateWidgetId(configurationObject.widgetConfiguration, configurationObject.card.data);

				this.callParent(arguments);

				// Setup widget configuration
				if (this.enableWidgetConfigurationSetup)
					this.widgetConfigurationSet({ value: this.widgetConfiguration }); // Setup widget configuration model

				// Inject delegate to view
				if (this.enableDelegateApply)
					this.view.delegate = this;
			} else {
				_error('wrong configuration object or empty widget view', this);
			}
		},

		/**
		 * @abstract
		 */
		beforeActiveView: function () {
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
		 * @returns {String}
		 *
		 * @private
		 */
		generateWidgetId: function (widgetConfiguration, cardData) {
			widgetConfiguration[CMDBuild.core.constants.Proxy.ID] = cardData[CMDBuild.core.constants.Proxy.ID] + '-' + widgetConfiguration[CMDBuild.core.constants.Proxy.ID];
		},

		/**
		 * @returns {Object or null}
		 */
		getData: function () {
			return null;
		},

		/**
		 * @returns {Number}
		 *
		 * @private
		 */
		getId: function () {
			return this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.ID);
		},

		/**
		 * @returns {String}
		 */
		getLabel: function () {
			return this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.LABEL);
		},

		/**
		 * @returns {Object}
		 */
		getTemplateResolverServerVars: function () {
			if (!Ext.isEmpty(this.card))
				return this.statics().getTemplateResolverServerVars(this.card);

			return {};
		},

		// InstancesDataStorage methods (multiple widget instances support)
			/**
			 * @returns {Mixed} or null
			 */
			instancesDataStorageGet: function () {
				if (!Ext.isEmpty(this.getId()) && !Ext.isEmpty(this.instancesDataStorage[this.getId()]))
					return this.instancesDataStorage[this.getId()];

				return null;
			},

			/**
			 * @returns {Boolean}
			 */
			instancesDataStorageIsEmpty: function () {
				if (!Ext.isEmpty(this.getId()))
					return Ext.isEmpty(this.instancesDataStorage[this.getId()]);

				return true;
			},

			instancesDataStorageReset: function () {
				this.instancesDataStorage = {};
			},

			/**
			 * @param {Mixed} instanceData
			 */
			instancesDataStorageSet: function (instanceData) {
				if (!Ext.isEmpty(this.getId()) && !Ext.isEmpty(instanceData))
					this.instancesDataStorage[this.getId()] = instanceData;
			},

		/**
		 * @returns {Boolean}
		 *
		 * @abstract
		 */
		isBusy: function () {
			return false;
		},

		/**
		 * @returns {Boolean}
		 *
		 * @abstract
		 */
		isValid: function () {
			return true;
		},

		/**
		 * Cannot be manage width cmfg() because requires to be executed on second step
		 *
		 * @param {Array} callbackChainArray
		 *
		 * @public
		 */
		onBeforeSave: function (callbackChainArray, i) {
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
			widgetConfigurationGet: function (attributePath) {
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
			widgetConfigurationIsEmpty: function (attributePath) {
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
			widgetConfigurationSet: function (parameters) {
				if (Ext.isEmpty(this.widgetConfigurationModelClassName) || !Ext.isString(this.widgetConfigurationModelClassName))
					return _error('widgetConfigurationModelClassName parameter not configured', this);

				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'widgetConfigurationModel';
				parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = this.widgetConfigurationModelClassName;
				parameters[CMDBuild.core.constants.Proxy.VALUE] = Ext.clone(parameters[CMDBuild.core.constants.Proxy.VALUE]);

				return this.propertyManageSet(parameters);
			},

		// WidgetCntroller methods
			/**
			 * @param {String} propertyName
			 *
			 * @returns {Mixed}
			 */
			widgetControllerPropertyGet: function (propertyName) {
				if (!Ext.isEmpty(this[propertyName]))
					return this[propertyName];

				return null;
			}
	});

})();
