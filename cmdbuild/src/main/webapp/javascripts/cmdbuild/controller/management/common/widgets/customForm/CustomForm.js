(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.CustomForm', {
		extend: 'CMDBuild.controller.common.AbstractBaseWidgetController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.widgets.CustomForm',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.model.CMActivityInstance or Ext.data.Model}
		 */
		card: undefined,

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
			'getTemplateResolverServerVars = widgetCustomFormGetTemplateResolverServerVars',
			'instancesDataStorageGet = widgetCustomFormInstancesDataStorageGet',
			'instancesDataStorageIsEmpty = widgetCustomFormInstancesDataStorageIsEmpty',
			'widgetConfigurationGet = widgetCustomFormConfigurationGet',
			'widgetConfigurationIsEmpty = widgetCustomFormConfigurationIsAttributeEmpty',
			'widgetConfigurationSet = widgetCustomFormConfigurationSet',
			'widgetControllerPropertyGet = widgetCustomFormControllerPropertyGet',
			'widgetCustomFormViewSetLoading'
		],

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.CustomFormView}
		 */
		view: undefined,

		/**
		 * @cfg {String}
		 */
		widgetConfigurationModelClassName: 'CMDBuild.model.widget.customForm.Configuration',

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
					serverVars: this.cmfg('widgetCustomFormGetTemplateResolverServerVars')
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
					serverVars: this.cmfg('widgetCustomFormGetTemplateResolverServerVars')
				});

				templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(target),
					scope: this,
					callback: function(out, ctx) {
						decodedOutput = out;

						// Apply change event to reset data property in widgetConfiguration to avoid sql function server call
						templateResolver.bindLocalDepsChange(function() {
							this.widgetConfiguration[CMDBuild.core.constants.Proxy.DATA] = null;
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
			if (!Ext.isEmpty(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.MODEL)))
				this.cmfg('widgetCustomFormConfigurationSet', {
					configurationObject: this.applyTemplateResolverToArray(this.widgetConfiguration[CMDBuild.core.constants.Proxy.MODEL]),
					propertyName: CMDBuild.core.constants.Proxy.MODEL
				});

			// Execute template resolver on variables property
			if (
				Ext.isEmpty(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.DATA))
				&& this.cmfg('widgetCustomFormInstancesDataStorageIsEmpty')
				&& !Ext.isEmpty(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.FUNCTION_DATA))
			) {
				this.cmfg('widgetCustomFormConfigurationSet', {
					configurationObject: this.applyTemplateResolverToObject(this.widgetConfiguration[CMDBuild.core.constants.Proxy.VARIABLES]),
					propertyName: CMDBuild.core.constants.Proxy.VARIABLES
				});

				// Build data configurations from function definition
				this.buildDataConfigurationFromFunction();
			} else {
				this.buildLayout();
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
			params[CMDBuild.core.constants.Proxy.FUNCTION] = this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.FUNCTION_DATA);
			params[CMDBuild.core.constants.Proxy.PARAMS] = Ext.encode(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.VARIABLES));

			CMDBuild.core.proxy.widgets.CustomForm.readFromFunctions({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CARDS];

					// Save function response to configuration's data property
					this.widgetConfiguration[CMDBuild.core.constants.Proxy.DATA] = decodedResponse;

					// Save function response to instance data storage
					this.instancesDataStorageSet(decodedResponse);
				},
				callback: function(response, options, decodedResponse) {
					this.buildLayout();
				}
			});
		},

		/**
		 * Builds layout controller and inject view
		 */
		buildLayout: function() {
			if (!this.cmfg('widgetCustomFormConfigurationIsAttributeEmpty', CMDBuild.core.constants.Proxy.MODEL)) {
				switch (this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.LAYOUT)) {
					case 'form': {
						this.controllerLayout = Ext.create('CMDBuild.controller.management.common.widgets.customForm.layout.Form', { parentDelegate: this });
					} break;

					case 'grid':
					default: {
						this.controllerLayout = Ext.create('CMDBuild.controller.management.common.widgets.customForm.layout.Grid', { parentDelegate: this });
					}
				}

				// Add related layout panel
				if (!Ext.isEmpty(this.view)) {
					this.view.removeAll();
					this.view.add(this.controllerLayout.getView());
				}

				this.controllerLayout.cmfg('onWidgetCustomFormShow');
			}
		},

		/**
		 * @return {Object} output
		 *
		 * @override
		 */
		getData: function() {
			var layoutData = this.controllerLayout.getData();
			var output = {};
			output[CMDBuild.core.constants.Proxy.OUTPUT] = [];

			if (
				!this.cmfg('widgetCustomFormConfigurationGet', [
					CMDBuild.core.constants.Proxy.CAPABILITIES,
					CMDBuild.core.constants.Proxy.READ_ONLY
				])
				&& Ext.isArray(layoutData)
			) {
				// Uses direct data property access to avoid a get problem because of generic model
				Ext.Array.forEach(layoutData, function(rowObject, i, allRowObjects) {
					var dataObject = Ext.isEmpty(rowObject.data) ? rowObject : rowObject.data; // Model/Objects management

					new CMDBuild.Management.TemplateResolver({
						clientForm: this.clientForm,
						xaVars: dataObject,
						serverVars: this.cmfg('widgetCustomFormGetTemplateResolverServerVars')
					}).resolveTemplates({
						attributes: Ext.Object.getKeys(dataObject),
						scope: this,
						callback: function(out, ctx) {
							output[CMDBuild.core.constants.Proxy.OUTPUT].push(Ext.encode(out));
						}
					});
				}, this);
			}

			return output;
		},

		/**
		 * Check required field value of grid store records
		 *
		 * @returns {Boolean}
		 *
		 * @override
		 */
		isValid: function() {
			if (!Ext.isEmpty(this.controllerLayout) && Ext.isFunction(this.controllerLayout.isValid))
				return this.controllerLayout.isValid();

			return this.callParent(arguments);
		},

		/**
		 * Preset data in instanceDataStorage variable
		 *
		 * @override
		 */
		onEditMode: function() {
			this.instancesDataStorageSet(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.DATA));

			this.beforeActiveView();
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

		/**
		 * @param {Boolean} state
		 */
		widgetCustomFormViewSetLoading: function(state) {
			state = Ext.isBoolean(state) ? state : false;

			this.view.setLoading(state);
		}
	});

})();