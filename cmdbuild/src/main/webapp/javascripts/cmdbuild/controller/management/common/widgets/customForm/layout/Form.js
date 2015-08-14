(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.layout.Form', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: ['importData'],

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.layout.FormPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.customForm.layout.FormPanel', {
				delegate: this
			});

			this.view.add(this.buildFields());
		},

		/**
		 * Setup form items disabled state.
		 * Disable topToolBar only if is readOnly.
		 */
		beforeActiveView: function() {
			var isWidgetReadOnly = this.cmfg('widgetConfigurationGet', [
				CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
				CMDBuild.core.proxy.CMProxyConstants.READ_ONLY
			]);

			if (
				isWidgetReadOnly
 				|| this.cmfg('widgetConfigurationGet', [
					CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
					CMDBuild.core.proxy.CMProxyConstants.MODIFY_DISABLED
				])
			) {
				this.view.setDisabledModify(true, true, isWidgetReadOnly);
			}
		},

		/**
		 * @return {Array} itemsArray
		 */
		buildFields: function() {
			var itemsArray = [];

			if (!this.cmfg('widgetConfigurationIsAttributeEmpty',  CMDBuild.core.proxy.CMProxyConstants.MODEL)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.forEach(this.cmfg('widgetConfigurationGet', CMDBuild.core.proxy.CMProxyConstants.MODEL), function(attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute.get(CMDBuild.core.proxy.CMProxyConstants.TYPE))) {
						fieldManager.attributeModelSet(Ext.create('CMDBuild.model.common.attributes.Attribute', attribute.getData()));

						itemsArray.push(fieldManager.buildField());
					} else { // @deprecated - Old field manager
						var attribute = attribute.getAdaptedData();
						var item = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);

						if (attribute[CMDBuild.core.proxy.CMProxyConstants.FIELD_MODE] == 'read')
							item.setDisabled(true);

						itemsArray.push(item);
					}
				}, this);
			}

			return itemsArray;
		},

		/**
		 * @returns {Array}
		 */
		getData: function() {
			return [this.view.getValues()];
		},

		/**
		 * @param {Array} data
		 */
		setData: function(data) {
			data = (Ext.isArray(data) && !Ext.isEmpty(data[0])) ? data[0] : data;

			if (Ext.isObject(data))
				if (Ext.isFunction(data.getData)) {
					return this.view.getForm().setValues(data.getData());
				} else {
					return this.view.getForm().setValues(data);
				}
		}
	});

})();