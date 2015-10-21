(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.layout.Form', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'importData',
			'onCustomFormLayoutFormImportButtonClick',
			'onCustomFormLayoutFormResetButtonClick'
		],

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.layout.FormPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.customForm.layout.FormPanel', { delegate: this });

			this.view.add(this.buildFields());
		},

		/**
		 * Setup form items disabled state.
		 * Disable topToolBar only if is readOnly.
		 */
		beforeActiveView: function() {
			var isWidgetReadOnly = this.cmfg('widgetConfigurationGet', [
				CMDBuild.core.constants.Proxy.CAPABILITIES,
				CMDBuild.core.constants.Proxy.READ_ONLY
			]);

			if (
				isWidgetReadOnly
 				|| this.cmfg('widgetConfigurationGet', [
					CMDBuild.core.constants.Proxy.CAPABILITIES,
					CMDBuild.core.constants.Proxy.MODIFY_DISABLED
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

			if (!this.cmfg('widgetConfigurationIsEmpty',  CMDBuild.core.constants.Proxy.MODEL)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.forEach(this.cmfg('widgetConfigurationGet', CMDBuild.core.constants.Proxy.MODEL), function(attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute.get(CMDBuild.core.constants.Proxy.TYPE))) {
						fieldManager.attributeModelSet(Ext.create('CMDBuild.model.common.attributes.Attribute', attribute.getData()));

						itemsArray.push(fieldManager.buildField());
					} else { // @deprecated - Old field manager
						var attribute = attribute.getAdaptedData();
						var item = undefined;

						if (attribute.type == 'REFERENCE') { // TODO: hack to force a templateResolver build for editor that haven't a form associated like other fields types
							var xaVars = CMDBuild.Utils.Metadata.extractMetaByNS(attribute.meta, 'system.template.');
							xaVars['_SystemFieldFilter'] = attribute.filter;

							var templateResolver = new CMDBuild.Management.TemplateResolver({ // TODO: implementation of serverside template resolver
								clientForm: this.cmfg('controllerPropertyGet', 'getClientForm'),
								xaVars: xaVars,
								serverVars: this.cmfg('getTemplateResolverServerVars')
							});

							// Required label fix
							if (attribute[CMDBuild.core.constants.Proxy.MANDATORY] || attribute['isnotnull']) {
								attribute[CMDBuild.core.constants.Proxy.DESCRIPTION] = (!Ext.isEmpty(attribute['isnotnull']) && attribute['isnotnull'] ? '* ' : '')
								+ attribute.description || attribute.name;
							}

							item = CMDBuild.Management.ReferenceField.buildEditor(attribute, templateResolver);

							// Force execution of template resolver
							if (!Ext.isEmpty(item) && Ext.isFunction(item.resolveTemplate))
								item.resolveTemplate();
						} else {
							item = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);
						}

						if (attribute[CMDBuild.core.constants.Proxy.FIELD_MODE] == 'read')
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
		 * @param {Object} parameters
		 * @param {String} parameters.append
		 * @param {Array} parameters.rowsObjects
		 */
		importData: function(parameters) {
			var rowsObjects = Ext.isArray(parameters.rowsObjects) ? parameters.rowsObjects : [];

			this.setData(rowsObjects);
		},

		/**
		 * Validate form
		 *
		 * @returns {Boolean}
		 */
		isValid: function() {
			return this.validate(this.view);
		},

		/**
		 * Opens import configuration pop-up window
		 */
		onCustomFormLayoutFormImportButtonClick: function() {
			Ext.create('CMDBuild.controller.management.common.widgets.customForm.Import', {
				parentDelegate: this,
				modeDisabled: true
			});
		},

		onCustomFormLayoutFormResetButtonClick: function() {
			this.setDefaultContent();
		},

		/**
		 * @param {Array} data
		 */
		setData: function(data) {
			data = (Ext.isArray(data) && !Ext.isEmpty(data[0])) ? data[0] : data;

			this.view.reset(); // In form layout is managed only one row at time, so all actions are considered with replace mode

			if (Ext.isObject(data))
				if (Ext.isFunction(data.getData)) {
					return this.view.getForm().setValues(data.getData());
				} else {
					return this.view.getForm().setValues(data);
				}
		},

		/**
		 * Resets widget configuration model because of a referencing of store records
		 */
		setDefaultContent: function() {
			this.cmfg('widgetConfigurationSet', {
				propertyName: CMDBuild.core.constants.Proxy.DATA,
				value: this.cmfg('controllerPropertyGet', 'widgetConfiguration')[CMDBuild.core.constants.Proxy.DATA]
			});

			this.setData(this.cmfg('widgetConfigurationGet', CMDBuild.core.constants.Proxy.DATA));
		}
	});

})();