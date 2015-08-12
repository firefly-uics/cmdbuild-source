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
			'widgetConfigurationGet',
			'getTemplateResolverServerVars',
			'widgetConfigurationIsAttributeEmpty',
//			'onEditRowButtonClick',
//			'setGridDataFromCsv'
		],

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.CustomFormView}
		 */
		view: undefined,

		/**
		 * @override
		 */
		beforeActiveView: function() {
			this.callParent(arguments);

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
		getData: function() { // TODO: implementation of serverside template resolver
			var me = this;
			var out = {};
			var data = [];

			this.grid.getStore().each(function(record) {
				new CMDBuild.Management.TemplateResolver({
					clientForm: this.clientForm,
					xaVars: record.getData(),
					serverVars: this.getTemplateResolverServerVars()
				}).resolveTemplates({
					attributes: Ext.Object.getKeys(record.getData()),
					callback: function(out, ctx) {
						// Date field format fix: date field gives wrong formatted value used as cell editor.
						// To delete when FieldManager will be refactored
						Ext.Object.each(out, function(key, value, object) {
							out[key] = me.formatDate(value);
						});

						data.push(
							Ext.encode(
								Ext.Object.merge(record.getData(), out)
							)
						);
					}
				});
			}, this);

			if (this.cmfg('widgetConfigurationGet', [CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES, CMDBuild.core.proxy.CMProxyConstants.READ_ONLY]))
				out[CMDBuild.core.proxy.CMProxyConstants.OUTPUT] = data;

			return out;
		},

		/**
		 * Check required field value of grid store records
		 *
		 * TODO: this function should be called with cmfg functionalities but that's requires a refactor of widgets base classes.
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
			 * @param {Object} widgetConfigurationObject
			 *
			 * @override
			 */
			widgetConfigurationSet: function(widgetConfigurationObject) {
				this.widgetConfigurationModel = Ext.create('CMDBuild.model.widgets.customForm.Configuration', widgetConfigurationObject);
			}
	});

})();