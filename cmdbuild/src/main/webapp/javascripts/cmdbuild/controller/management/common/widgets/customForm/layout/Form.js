(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.layout.Form', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWidgetCustomFormLayoutFormImportButtonClick',
			'onWidgetCustomFormLayoutFormResetButtonClick',
			'onWidgetCustomFormLayoutFormShow = onWidgetCustomFormShow',
			'widgetCustomFormLayoutFormImportData = widgetCustomFormImportData'
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

			CMDBuild.core.RequestBarrier.finalize('referenceStoreLoadBarrier');
		},

		/**
		 * @return {Array} itemsArray
		 */
		buildFields: function() {
			var itemsArray = [];

			if (!this.cmfg('widgetCustomFormConfigurationIsAttributeEmpty',  CMDBuild.core.proxy.CMProxyConstants.MODEL)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.forEach(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.proxy.CMProxyConstants.MODEL), function(attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute.get(CMDBuild.core.proxy.CMProxyConstants.TYPE))) {
						fieldManager.attributeModelSet(Ext.create('CMDBuild.model.common.attributes.Attribute', attribute.getData()));
						fieldManager.push(itemsArray, fieldManager.buildField());
					} else { // @deprecated - Old field manager
						var attribute = attribute.getAdaptedData();
						var item = undefined;

						if (attribute.type == 'REFERENCE') { // TODO: hack to force a templateResolver build for editor that haven't a form associated like other fields types
							var xaVars = CMDBuild.Utils.Metadata.extractMetaByNS(attribute.meta, 'system.template.');
							xaVars['_SystemFieldFilter'] = attribute.filter;

							var templateResolver = new CMDBuild.Management.TemplateResolver({ // TODO: implementation of serverside template resolver
								clientForm: this.cmfg('widgetCustomFormControllerPropertyGet', 'getClientForm'),
								xaVars: xaVars,
								serverVars: this.cmfg('widgetCustomFormGetTemplateResolverServerVars')
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
			return [this.view.getData(true)];
		},

		/**
		 * Validate form
		 *
		 * @param {Boolean} showPopup
		 *
		 * @returns {Boolean}
		 */
		isValid: function(showPopup) {
			return this.validate(this.view, showPopup);
		},

		/**
		 * Opens import configuration pop-up window
		 */
		onWidgetCustomFormLayoutFormImportButtonClick: function() {
			Ext.create('CMDBuild.controller.management.common.widgets.customForm.Import', {
				parentDelegate: this,
				modeDisabled: true
			});
		},

		onWidgetCustomFormLayoutFormResetButtonClick: function() {
			this.cmfg('widgetCustomFormConfigurationSet', {
				configurationObject: this.cmfg('widgetCustomFormControllerPropertyGet', 'widgetConfiguration')[CMDBuild.core.proxy.CMProxyConstants.DATA],
				propertyName: CMDBuild.core.proxy.CMProxyConstants.DATA
			});

			this.setData(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.proxy.CMProxyConstants.DATA));
		},

		/**
		 * Setup form items disabled state, disable topToolBar only if is readOnly
		 * Load grid data
		 */
		onWidgetCustomFormLayoutFormShow: function() {
			var isWidgetReadOnly = this.cmfg('widgetCustomFormConfigurationGet', [
				CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
				CMDBuild.core.proxy.CMProxyConstants.READ_ONLY
			]);

			if (
				isWidgetReadOnly
 				|| this.cmfg('widgetCustomFormConfigurationGet', [
					CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
					CMDBuild.core.proxy.CMProxyConstants.MODIFY_DISABLED
				])
			) {
				this.view.setDisabledModify(true, true, isWidgetReadOnly);
			}

			if (!this.cmfg('widgetCustomFormInstancesDataStorageIsEmpty'))
				this.setData(this.cmfg('widgetCustomFormInstancesDataStorageGet'));
		},

		/**
		 * @param {Array} data
		 */
		setData: function(data) {
			data = (Ext.isArray(data) && !Ext.isEmpty(data[0])) ? data[0] : data;

			this.view.reset();

			if (Ext.isObject(data)) {
				// Clean data object to avoid set of empty values
				Ext.Object.each(data, function(key, value, myself) {
					if (Ext.isEmpty(value))
						delete data[key];
				}, this);

				if (!Ext.Object.isEmpty(data))
					this.view.getForm().setValues(data);
			}

			this.isValid(false);
		},

		/**
		 * @param {Object} parameters
		 * @param {String} parameters.append
		 * @param {Array} parameters.rowsObjects
		 */
		widgetCustomFormLayoutFormImportData: function(parameters) {
			var rowsObjects = Ext.isArray(parameters.rowsObjects) ? parameters.rowsObjects : [];

			this.setData(rowsObjects);
		}
	});

})();