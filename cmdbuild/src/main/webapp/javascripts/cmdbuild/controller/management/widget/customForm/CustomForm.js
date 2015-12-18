(function() {

	Ext.define('CMDBuild.controller.management.widget.customForm.CustomForm', {
		extend: 'CMDBuild.controller.common.AbstractWidgetController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.widget.CustomForm',
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
			'widgetConfigurationIsEmpty = widgetCustomFormConfigurationIsEmpty',
			'widgetConfigurationSet = widgetCustomFormConfigurationSet',
			'widgetControllerPropertyGet = widgetCustomFormControllerPropertyGet',
			'widgetCustomFormDataGet',
			'widgetCustomFormModelStoreBuilder',
			'widgetCustomFormViewSetLoading'
		],

		/**
		 * @property {CMDBuild.view.management.widget.customForm.CustomFormView}
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
		 *
		 * @private
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
		 *
		 * @private
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
		 * @public
		 * @override
		 */
		beforeActiveView: function() {
			this.callParent(arguments);

			// Execute template resolver on model property
			if (!Ext.isEmpty(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.MODEL)))
				this.cmfg('widgetCustomFormConfigurationSet', {
					propertyName: CMDBuild.core.constants.Proxy.MODEL,
					value: this.applyTemplateResolverToArray(this.widgetConfiguration[CMDBuild.core.constants.Proxy.MODEL])
				});

			// Execute template resolver on variables property
			if (
				Ext.isEmpty(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.DATA))
				&& this.cmfg('widgetCustomFormInstancesDataStorageIsEmpty')
				&& !Ext.isEmpty(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.FUNCTION_DATA))
			) {
				this.cmfg('widgetCustomFormConfigurationSet', {
					propertyName: CMDBuild.core.constants.Proxy.VARIABLES,
					value: this.applyTemplateResolverToObject(this.widgetConfiguration[CMDBuild.core.constants.Proxy.VARIABLES])
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
		 * @public
		 * @override
		 */
		beforeHideView: function() {
			this.instancesDataStorageSet(this.cmfg('widgetCustomFormDataGet'));
		},

		/**
		 * @param {Function} callback
		 *
		 * @private
		 */
		buildDataConfigurationFromFunction: function(callback) {
			callback = Ext.isFunction(callback) ? callback : Ext.emptyFn;

			if (!this.cmfg('widgetCustomFormConfigurationIsEmpty', CMDBuild.core.constants.Proxy.FUNCTION_DATA)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.FUNCTION] = this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.FUNCTION_DATA);
				params[CMDBuild.core.constants.Proxy.PARAMS] = Ext.encode(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.VARIABLES));

				CMDBuild.core.proxy.widget.CustomForm.readFromFunctions({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CARDS];

						// Save function response to configuration's data property
						this.widgetConfiguration[CMDBuild.core.constants.Proxy.DATA] = decodedResponse;

						// Save function response to instance data storage
						this.instancesDataStorageSet(decodedResponse);
					},
					callback: function(options, success, response) {
						this.buildLayout();
					}
				});
			}
		},

		/**
		 * Builds layout controller and inject view
		 *
		 * @private
		 */
		buildLayout: function() {
			if (!this.cmfg('widgetCustomFormConfigurationIsEmpty', CMDBuild.core.constants.Proxy.MODEL)) {
				switch (this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.LAYOUT)) {
					case 'form': {
						this.controllerLayout = Ext.create('CMDBuild.controller.management.widget.customForm.layout.Form', { parentDelegate: this });
					} break;

					case 'grid':
					default: {
						this.controllerLayout = Ext.create('CMDBuild.controller.management.widget.customForm.layout.Grid', { parentDelegate: this });
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
		 * @public
		 * @override
		 */
		getData: function() {
			var output = {};
			output[CMDBuild.core.constants.Proxy.OUTPUT] = [];

			if (
				!this.cmfg('widgetCustomFormConfigurationGet', [
					CMDBuild.core.constants.Proxy.CAPABILITIES,
					CMDBuild.core.constants.Proxy.READ_ONLY
				])
			) {
				// Uses direct data property access to avoid a get problem because of generic model
				Ext.Array.forEach(this.cmfg('widgetCustomFormDataGet'), function(rowObject, i, allRowObjects) {
					var dataObject = Ext.isEmpty(rowObject.data) ? rowObject : rowObject.data; // Model/Objects management

					new CMDBuild.Management.TemplateResolver({
						clientForm: this.clientForm,
						xaVars: dataObject,
						serverVars: this.cmfg('widgetCustomFormGetTemplateResolverServerVars')
					}).resolveTemplates({
						attributes: Ext.Object.getKeys(dataObject),
						scope: this,
						callback: function(out, ctx) {
							if (Ext.isObject(out))
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
		 * @public
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
		 * @public
		 * @override
		 */
		onEditMode: function() {
			this.instancesDataStorageSet(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.DATA));

			this.beforeActiveView();
		},

		/**
		 * Shorthand to controllerLayout's getData method
		 *
		 * @returns {Array}
		 */
		widgetCustomFormDataGet: function() {
			return this.controllerLayout.getData();
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		widgetCustomFormModelStoreBuilder: function() {
			var columnsData = [];

			Ext.Array.forEach(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.MODEL), function(attributeModel, i, allAttributeModels) {
				if (!Ext.isEmpty(attributeModel))
					columnsData.push([
						attributeModel.get(CMDBuild.core.constants.Proxy.NAME),
						attributeModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION)
					]);
			}, this);

			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.NAME, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: columnsData,

				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
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