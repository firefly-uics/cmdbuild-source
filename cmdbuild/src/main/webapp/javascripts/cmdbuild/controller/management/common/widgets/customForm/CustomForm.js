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
			'widgetConfigurationIsAttributeEmpty'
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
			this.widgetConfigurationSet(
				this.applyTemplateResolver(this.widgetConfiguration[CMDBuild.core.proxy.CMProxyConstants.MODEL])
				, CMDBuild.core.proxy.CMProxyConstants.MODEL
			);

			if (!this.widgetConfigurationIsAttributeEmpty(CMDBuild.core.proxy.CMProxyConstants.MODEL)) {
				this.buildLayout();

				if (!this.instancesDataStorageIsEmpty())
					this.controllerLayout.setData(this.instancesDataStorageGet());
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

			this.controllerLayout.setData(this.widgetConfigurationGet(CMDBuild.core.proxy.CMProxyConstants.DATA));

			// Add related layout panel
			if (!Ext.isEmpty(this.view)) {
				this.view.removeAll();
				this.view.add(this.controllerLayout.getView());
			}
		},

		/**
		 * @return {Object} out
		 *
		 * @override
		 */
		getData: function() { // TODO: finish implementation
			var out = {};

			if (!this.widgetConfigurationGet([CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES, CMDBuild.core.proxy.CMProxyConstants.READ_ONLY])) {
				out[CMDBuild.core.proxy.CMProxyConstants.OUTPUT] = [];

				// Uses direct data property access to avoid a get problem because of generic model
				Ext.Array.forEach(this.controllerLayout.getData(), function(rowModel, i, allRowModels) {
					new CMDBuild.Management.TemplateResolver({
						clientForm: this.clientForm,
						xaVars: rowModel.data,
						serverVars: this.getTemplateResolverServerVars()
					}).resolveTemplates({
						attributes: Ext.Object.getKeys(rowModel.data),
						callback: function(out, ctx) {
							out[CMDBuild.core.proxy.CMProxyConstants.OUTPUT].push(Ext.encode(out));
						}
					});
				}, this);
			}

			return out;
		},

		/**
		 * Check required field value of grid store records
		 *
		 * FIXME: this function should be called with cmfg functionalities but that's requires a refactor of widgets base classes.
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
			 * @param {Object} configurationObject
			 * @param {String} propertyName
			 *
			 * @returns {Mixed}
			 *
			 * @override
			 */
			widgetConfigurationSet: function(configurationObject, propertyName) {
				this.callParent(arguments);

				if (Ext.isEmpty(propertyName))
					this.widgetConfigurationModel = Ext.create('CMDBuild.model.widgets.customForm.Configuration', configurationObject);
			}
	});

})();