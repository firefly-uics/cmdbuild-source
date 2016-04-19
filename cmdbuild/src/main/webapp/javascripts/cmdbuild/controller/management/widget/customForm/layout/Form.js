(function () {

	Ext.define('CMDBuild.controller.management.widget.customForm.layout.Form', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.RequestBarrier'
		],

		/**
		 * @cfg {CMDBuild.controller.management.widget.customForm.CustomForm}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWidgetCustomFormLayoutFormExportButtonClick',
			'onWidgetCustomFormLayoutFormImportButtonClick',
			'onWidgetCustomFormLayoutFormResetButtonClick',
			'onWidgetCustomFormLayoutFormShow = onWidgetCustomFormShow',
			'widgetCustomFormLayoutFormDataGet = widgetCustomFormLayoutControllerDataGet',
			'widgetCustomFormLayoutFormDataSet = widgetCustomFormDataSet',
			'widgetCustomFormLayoutFormIsValid = widgetCustomFormLayoutControllerIsValid'
		],

		/**
		 * @property {CMDBuild.view.management.widget.customForm.layout.FormPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.widget.customForm.CustomForm} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			// Barrier to load data after reference field store's load end
			CMDBuild.core.RequestBarrier.init('referenceStoreLoadBarrier', function () {
				if (!this.cmfg('widgetCustomFormInstancesDataStorageIsEmpty'))
					this.cmfg('widgetCustomFormLayoutFormDataSet', this.cmfg('widgetCustomFormInstancesDataStorageGet'));

				this.cmfg('widgetCustomFormViewSetLoading', false);
			}, this);

			this.view = Ext.create('CMDBuild.view.management.widget.customForm.layout.FormPanel', { delegate: this });

			this.view.add(this.buildFields());

			this.cmfg('widgetCustomFormViewSetLoading', true);

			CMDBuild.core.RequestBarrier.finalize('referenceStoreLoadBarrier');
		},

		/**
		 * @return {Array} itemsArray
		 *
		 * @private
		 */
		buildFields: function () {
			var itemsArray = [];

			if (!this.cmfg('widgetCustomFormConfigurationIsEmpty',  CMDBuild.core.constants.Proxy.MODEL)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.forEach(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.MODEL), function (attribute, i, allAttributes) {
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
		 * Opens export configuration pop-up window
		 */
		onWidgetCustomFormLayoutFormExportButtonClick: function () {
			Ext.create('CMDBuild.controller.management.widget.customForm.Export', { parentDelegate: this });
		},

		/**
		 * Opens import configuration pop-up window
		 */
		onWidgetCustomFormLayoutFormImportButtonClick: function () {
			Ext.create('CMDBuild.controller.management.widget.customForm.Import', { parentDelegate: this });
		},

		/**
		 * Setup form items disabled state, disable topToolBar only if is readOnly
		 * Load grid data
		 */
		onWidgetCustomFormLayoutFormShow: function () {
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
				this.cmfg('widgetCustomFormLayoutFormDataSet', this.cmfg('widgetCustomFormInstancesDataStorageGet'));
		},

		/**
		 * @returns {Array}
		 */
		widgetCustomFormLayoutFormDataGet: function () {
			return [this.view.getData(true)];
		},

		/**
		 * @param {Array} data
		 *
		 * @returns {Void}
		 */
		widgetCustomFormLayoutFormDataSet: function (data) {
			data = (Ext.isArray(data) && !Ext.isEmpty(data[0])) ? data[0] : data; // Get first item only from arrays
			data = Ext.isFunction(data.getData) ? data.getData() : data; // Manage models

			this.view.reset();

			if (Ext.isObject(data) && !Ext.Object.isEmpty(data))
				this.view.getForm().setValues(data);

			this.cmfg('widgetCustomFormLayoutFormIsValid', false);
		},

		/**
		 * Validate form
		 *
		 * @param {Boolean} showPopup
		 *
		 * @returns {Boolean}
		 */
		widgetCustomFormLayoutFormIsValid: function (showPopup) {
			return this.validate(this.view, showPopup);
		}
	});

})();
