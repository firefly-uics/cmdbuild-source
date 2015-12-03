(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.layout.Form', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.RequestBarrier'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.customForm.CustomForm}
		 */
		parentDelegate: undefined,

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

			if (!this.cmfg('widgetCustomFormConfigurationIsEmpty',  CMDBuild.core.constants.Proxy.MODEL)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.forEach(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.MODEL), function(attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute.get(CMDBuild.core.constants.Proxy.TYPE))) {
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
							if (attribute[CMDBuild.core.constants.Proxy.MANDATORY] || attribute['isnotnull']) {
								attribute[CMDBuild.core.constants.Proxy.DESCRIPTION] = (!Ext.isEmpty(attribute['isnotnull']) && attribute['isnotnull'] ? '* ' : '')
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
				propertyName: CMDBuild.core.constants.Proxy.DATA,
				value: this.cmfg('widgetCustomFormControllerPropertyGet', 'widgetConfiguration')[CMDBuild.core.constants.Proxy.DATA]
			});

			this.setData(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.DATA));
		},

		/**
		 * Setup form items disabled state, disable topToolBar only if is readOnly
		 * Load grid data
		 */
		onWidgetCustomFormLayoutFormShow: function() {
			var isWidgetReadOnly = this.cmfg('widgetCustomFormConfigurationGet', [
				CMDBuild.core.constants.Proxy.CAPABILITIES,
				CMDBuild.core.constants.Proxy.READ_ONLY
			]);

			if (
				isWidgetReadOnly
 				|| this.cmfg('widgetCustomFormConfigurationGet', [
					CMDBuild.core.constants.Proxy.CAPABILITIES,
					CMDBuild.core.constants.Proxy.MODIFY_DISABLED
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
			data = (Ext.isArray(data) && !Ext.isEmpty(data[0])) ? data[0] : data; // Get first item only from arrays

			this.view.reset();

			if (Ext.isObject(data) && !Ext.Object.isEmpty(data))
				this.view.loadRecord(data);

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