(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.CustomForm', {
		extend: 'CMDBuild.controller.common.AbstractBaseWidgetController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.Message'
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
			'widgetControllerPropertyGet'
		],

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.CustomFormView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMWidgetManager} configurationObject.view
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
		 * @param {Array} target
		 */
		applyTemplateResolver: function(target) {
			var decodedOutput = [];

			target = Ext.isString(target) ? Ext.decode(target) : target;
			target = Ext.isArray(target) ? target : [target];

			Ext.Array.forEach(target, function(object, i, allObjects) {
				new CMDBuild.Management.TemplateResolver({
					clientForm: this.clientForm,
					xaVars: object,
					serverVars: this.getTemplateResolverServerVars()
				}).resolveTemplates({
					attributes: Ext.Object.getKeys(object),
					callback: function(out, ctx) {
						decodedOutput.push(out);
					}
				});
			}, this);

			return decodedOutput;
		},

		/**
		 * @override
		 */
		beforeActiveView: function() {
			this.callParent(arguments);

			// Execute template resolver on model property
			this.widgetConfigurationSet({
				configurationObject: this.applyTemplateResolver(this.widgetConfiguration[CMDBuild.core.proxy.CMProxyConstants.MODEL]),
				propertyName: CMDBuild.core.proxy.CMProxyConstants.MODEL
			});

			if (!this.widgetConfigurationIsAttributeEmpty(CMDBuild.core.proxy.CMProxyConstants.MODEL)) {
				this.buildLayout();

				if (!this.instancesDataStorageIsEmpty())
					this.controllerLayout.setData(this.instancesDataStorageGet());

				// Function forward
				if (Ext.isFunction(this.controllerLayout.beforeActiveView))
					this.controllerLayout.beforeActiveView();
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
			}
	});

})();