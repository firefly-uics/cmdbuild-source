(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.CustomForm', {
		extend: 'CMDBuild.controller.common.AbstractBaseWidgetController',

		requires: [
			'CMDBuild.core.constants.Proxy',
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
			'controllerPropertyGet',
			'getTemplateResolverServerVars',
			'widgetConfigurationGet',
			'widgetConfigurationIsEmpty',
			'widgetConfigurationSet'
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
		 * @param {CMDBuild.view.management.common.widgets.CMWidgetManager} configurationObject.view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} configurationObject.parentDelegate
		 * @param {Object} configurationObject.widgetConfiguration
		 * @param {Ext.form.Basic} configurationObject.clientForm
		 * @param {CMDBuild.model.CMActivityInstance} configurationObject.card
		 *
		 * @override
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
				propertyName: CMDBuild.core.constants.Proxy.MODEL,
				value: this.applyTemplateResolver(this.widgetConfiguration[CMDBuild.core.constants.Proxy.MODEL])
			});

			if (!this.widgetConfigurationIsEmpty(CMDBuild.core.constants.Proxy.MODEL)) {
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
			switch (this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.LAYOUT)) {
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
			output[CMDBuild.core.constants.Proxy.OUTPUT] = [];

			if (!this.widgetConfigurationGet([CMDBuild.core.constants.Proxy.CAPABILITIES, CMDBuild.core.constants.Proxy.READ_ONLY])) {
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
		}
	});

})();