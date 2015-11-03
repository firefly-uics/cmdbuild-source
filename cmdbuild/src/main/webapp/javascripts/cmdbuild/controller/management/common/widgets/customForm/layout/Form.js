(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.layout.Form', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'importData',
			'onCustomFormLayoutFormImportButtonClick',
			'onCustomFormLayoutFormResetButtonClick',
			'onCustomFormLayoutFormShow = onCustomFormShow'
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

			// Barrier to load data after reference field store's load end
			CMDBuild.core.RequestBarrier.init('referenceStoreLoadBarrier', function() {
				if (!this.cmfg('widgetCustomFormInstancesDataStorageIsEmpty'))
					this.setData(this.cmfg('widgetCustomFormInstancesDataStorageGet'));

				this.cmfg('widgetCustomFormViewSetLoading', false);
			}, this);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.customForm.layout.FormPanel', { delegate: this });

			this.view.add(this.buildFields());

			this.cmfg('widgetCustomFormViewSetLoading', true);
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
						var item = undefined;

						if (attribute.type == 'REFERENCE') { // TODO: hack to force a templateResolver build for editor that haven't a form associated like other fields types
							var xaVars = CMDBuild.Utils.Metadata.extractMetaByNS(attribute.meta, 'system.template.');
							xaVars['_SystemFieldFilter'] = attribute.filter;

							var templateResolver = new CMDBuild.Management.TemplateResolver({ // TODO: implementation of serverside template resolver
								clientForm: this.cmfg('widgetControllerPropertyGet', 'getClientForm'),
								xaVars: xaVars,
								serverVars: this.cmfg('getTemplateResolverServerVars')
							});

							// Required label fix
							if (attribute[CMDBuild.core.proxy.CMProxyConstants.MANDATORY] || attribute['isnotnull']) {
								attribute[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = (!Ext.isEmpty(attribute['isnotnull']) && attribute['isnotnull'] ? '* ' : '')
								+ attribute.description || attribute.name;
							}

							item = CMDBuild.Management.ReferenceField.buildEditor(attribute, templateResolver);

							// Force execution of template resolver
							if (!Ext.isEmpty(item) && Ext.isFunction(item.resolveTemplate))
								item.resolveTemplate();

							// Apply event for store load barrier
							if (!Ext.isEmpty(item) && Ext.isFunction(item.getStore) && item.getStore().count() == 0)
								item.getStore().on('load', CMDBuild.core.RequestBarrier.getCallback('referenceStoreLoadBarrier'), this);
						} else {
							item = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);
						}

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
			this.cmfg('widgetConfigurationSet', {
				configurationObject: this.cmfg('widgetControllerPropertyGet', 'widgetConfiguration')[CMDBuild.core.proxy.CMProxyConstants.DATA],
				propertyName: CMDBuild.core.proxy.CMProxyConstants.DATA
			});

			this.setData(this.cmfg('widgetConfigurationGet', CMDBuild.core.proxy.CMProxyConstants.DATA));
		},

		/**
		 * Setup form items disabled state, disable topToolBar only if is readOnly
		 * Load grid data
		 */
		onCustomFormLayoutFormShow: function() {
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

			this.setData(this.cmfg('widgetCustomFormInstancesDataStorageGet'));
		},

		/**
		 * @param {Array} data
		 */
		setData: function(data) {
			data = (Ext.isArray(data) && !Ext.isEmpty(data[0])) ? data[0] : data;

			this.view.reset(); // In form layout is managed only one row at time, so all actions are considered with replace mode

			if (Ext.isObject(data)) // Model or simple object manage
				this.view.getForm().setValues(
					Ext.isFunction(data.getData) ? data.getData() : data
				);
		}
	});

})();