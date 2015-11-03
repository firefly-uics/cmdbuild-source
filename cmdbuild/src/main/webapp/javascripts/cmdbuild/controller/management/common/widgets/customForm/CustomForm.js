(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.CustomForm', {
		extend: 'CMDBuild.controller.common.AbstractBaseWidgetController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.CustomForm',
			'CMDBuild.core.Utils'
		],

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @property {Mixed}
		 */
		controllerLayout: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'getTemplateResolverServerVars',
			'widgetConfigurationGet',
			'widgetConfigurationIsAttributeEmpty',
			'widgetConfigurationSet',
			'widgetControllerPropertyGet',
			'instancesDataStorageGet = widgetCustomFormInstancesDataStorageGet',
			'instancesDataStorageIsEmpty = widgetCustomFormInstancesDataStorageIsEmpty',
			'widgetCustomFormViewSetLoading'
		],

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.CustomFormView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.management.common.widgets.customForm.CustomFormView} configurationObject.view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} configurationObject.parentDelegate
		 * @param {Object} configurationObject.widgetConfiguration
		 * @param {Ext.form.Basic} configurationObject.clientForm
		 * @param {CMDBuild.model.CMActivityInstance} configurationObject.card
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.beforeActiveView();
		},

		/**
		 * @param {Array or String} target
		 *
		 * @returns {Array} decodedOutput
		 */
		applyTemplateResolverToArray: function(target) {
			target = CMDBuild.core.Utils.isJsonString(target) ? Ext.decode(target) : target;
			target = Ext.isArray(target) ? target : [target];

			var decodedOutput = [];

			Ext.Array.forEach(target, function(object, i, allObjects) {
				new CMDBuild.Management.TemplateResolver({
					clientForm: this.clientForm,
					xaVars: object,
					serverVars: this.getTemplateResolverServerVars()
				}).resolveTemplates({
					attributes: Ext.Object.getKeys(object),
					scope: this,
					callback: function(out, ctx) {
						decodedOutput.push(out);
					}
				});
			}, this);

			return decodedOutput;
		},

		/**
		 * @param {Object or String} target
		 *
		 * @returns {Object} decodedOutput
		 */
		applyTemplateResolverToObject: function(target) {
			target = CMDBuild.core.Utils.isJsonString(target) ? Ext.decode(target) : target;

			var decodedOutput = {};

			if (Ext.isObject(target))
				var templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: this.clientForm,
					xaVars: target,
					serverVars: this.getTemplateResolverServerVars()
				});

				templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(target),
					scope: this,
					callback: function(out, ctx) {
						decodedOutput = out;

						// Reset data property in widgetConfiguration to avoid server call to get function response
						templateResolver.bindLocalDepsChange(function() {
							this.widgetConfiguration[CMDBuild.core.proxy.CMProxyConstants.DATA] = null;
						}, this);
					}
				});

			return decodedOutput;
		},

		/**
		 * @override
		 */
		beforeActiveView: function() {
			this.callParent(arguments);

			// Execute template resolver on model property
			if (!Ext.isEmpty(this.widgetConfigurationGet(CMDBuild.core.proxy.CMProxyConstants.MODEL)))
				this.widgetConfigurationSet({
					configurationObject: this.applyTemplateResolverToArray(this.widgetConfiguration[CMDBuild.core.proxy.CMProxyConstants.MODEL]),
					propertyName: CMDBuild.core.proxy.CMProxyConstants.MODEL
				});

			// Execute template resolver on variables property
			if (
				Ext.isEmpty(this.widgetConfigurationGet(CMDBuild.core.proxy.CMProxyConstants.DATA))
				&& !Ext.isEmpty(this.widgetConfigurationGet(CMDBuild.core.proxy.CMProxyConstants.FUNCTION_DATA))
			) {
				this.widgetConfigurationSet({
					configurationObject: this.applyTemplateResolverToObject(this.widgetConfiguration[CMDBuild.core.proxy.CMProxyConstants.VARIABLES]),
					propertyName: CMDBuild.core.proxy.CMProxyConstants.VARIABLES
				});

				// Build data configurations from function definition
				this.buildDataConfigurationFromFunction();
			} else {
				this.runBuildLayout();
			}
		},

		/**
		 * Save data in storage attribute
		 *
		 * @override
		 */
		beforeHideView: function() {
			this.instancesDataStorageSet(this.controllerLayout.getData());
		},

		/**
		 * @param {Function} callback
		 */
		buildDataConfigurationFromFunction: function(callback) {
			callback = Ext.isFunction(callback) ? callback : Ext.emptyFn;

			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.FUNCTION] = this.widgetConfigurationGet(CMDBuild.core.proxy.CMProxyConstants.FUNCTION_DATA);
			params[CMDBuild.core.proxy.CMProxyConstants.PARAMS] = Ext.encode(this.widgetConfigurationGet(CMDBuild.core.proxy.CMProxyConstants.VARIABLES));

			CMDBuild.core.proxy.widgets.CustomForm.readFromFunctions({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.proxy.CMProxyConstants.CARDS];

					// Save function response to configuration's data property
					this.widgetConfiguration[CMDBuild.core.proxy.CMProxyConstants.DATA] = decodedResponse;

					// Save function response to instance data storage
					this.instancesDataStorageSet(decodedResponse);
				},
				callback: function(response, options, decodedResponse) {
					this.runBuildLayout();
				}
			});
		},

		/**
		 * Builds layout controller and inject view
		 */
		buildLayout: function() {
			switch (this.widgetConfigurationGet(CMDBuild.core.proxy.CMProxyConstants.LAYOUT)) {
				case 'form': {
					this.controllerLayout = Ext.create('CMDBuild.controller.management.common.widgets.customForm.layout.Form', { parentDelegate: this });
				} break;

				case 'grid':
				default: {
					this.controllerLayout = Ext.create('CMDBuild.controller.management.common.widgets.customForm.layout.Grid', { parentDelegate: this });
				}
			}

			this.controllerLayout.setDefaultContent();

			// Add related layout panel
			if (!Ext.isEmpty(this.view)) {
				this.view.removeAll();
				this.view.add(this.controllerLayout.getView());
			}
		},

		/**
		 * @return {Object} output
		 *
		 * @override
		 */
		getData: function() {
			var output = {};
			output[CMDBuild.core.proxy.CMProxyConstants.OUTPUT] = [];

			if (!this.widgetConfigurationGet([CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES, CMDBuild.core.proxy.CMProxyConstants.READ_ONLY])) {
				// Uses direct data property access to avoid a get problem because of generic model
				Ext.Array.forEach(this.controllerLayout.getData(), function(rowObject, i, allRowObjects) {
					var dataObject = Ext.isEmpty(rowObject.data) ? rowObject : rowObject.data; // Model/Objects management

					new CMDBuild.Management.TemplateResolver({
						clientForm: this.clientForm,
						xaVars: dataObject,
						serverVars: this.getTemplateResolverServerVars()
					}).resolveTemplates({
						attributes: Ext.Object.getKeys(dataObject),
						callback: function(out, ctx) {
							output[CMDBuild.core.proxy.CMProxyConstants.OUTPUT].push(Ext.encode(out));
						}
					});
				}, this);
			}

			return output;
		},

		/**
		 * Check required field value of grid store records
		 *
		 * FIXME: this function should be called with cmfg functionalities but that's requires a refactor of widgets base classes
		 *
		 * @returns {Boolean}
		 *
		 * @override
		 */
		isValid: function() {
			if (!Ext.isEmpty(this.controllerLayout) && Ext.isFunction(this.controllerLayout.isValid))
				return this.controllerLayout.isValid();

			return true;
		},

		/**
		 * Reset instance storage property
		 *
		 * @override
		 */
		onEditMode: function() {
			this.instancesDataStorageReset();
		},

		// WidgetConfiguration methods
			/**
			 * @param {Object} parameters
			 * @param {Object} parameters.configurationObject
			 * @param {String} parameters.propertyName
			 *
			 * @returns {Mixed}
			 *
			 * @override
			 */
			widgetConfigurationSet: function(parameters) {
				var configurationObject = parameters.configurationObject;
				var propertyName = parameters.propertyName;

				this.callParent(arguments);

				// Full model setup management
				if (!Ext.isEmpty(configurationObject) && Ext.isEmpty(propertyName))
					this.widgetConfigurationModel = Ext.create('CMDBuild.model.widget.customForm.Configuration', Ext.clone(configurationObject));
			},


		runBuildLayout: function() {
			if (!this.widgetConfigurationIsAttributeEmpty(CMDBuild.core.proxy.CMProxyConstants.MODEL)) {
				this.buildLayout();

				this.controllerLayout.cmfg('onCustomFormShow');
			}
		},

		/**
		 * @param {Boolean} state
		 */
		widgetCustomFormViewSetLoading: function(state) {
			state = Ext.isBoolean(state) ? state : false;

			this.view.setLoading(state);
		}
	});

})();