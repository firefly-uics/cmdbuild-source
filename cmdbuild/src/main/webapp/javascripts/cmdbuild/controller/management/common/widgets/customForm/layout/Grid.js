(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.layout.Grid', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.RequestBarrier'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'importData',
			'onCustomFormLayoutGridAddRowButtonClick',
			'onCustomFormLayoutGridDeleteRowButtonClick' ,
			'onCustomFormLayoutGridEditRowButtonClick',
			'onCustomFormLayoutGridImportButtonClick',
			'onCustomFormLayoutGridResetButtonClick',
			'onCustomFormLayoutGridShow = onCustomFormShow'
		],

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.layout.GridPanel}
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
					this.setData(this.cmfg('widgetCustomFormInstancesDataStorageGet', CMDBuild.core.proxy.CMProxyConstants.DATA));

				this.cmfg('widgetCustomFormViewSetLoading', false);
			}, this);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.customForm.layout.GridPanel', {
				delegate: this,
				columns: this.buildColumns(),
				store: this.buildDataStore()
			});

			this.cmfg('widgetCustomFormViewSetLoading', true);
		},

		/**
		 * @param {Object} header
		 * @param {Object} attribute
		 *
		 * @returns {String} value
		 *
		 * TODO: delete when old FieldManager will be replaced
		 */
		addRendererToHeader: function(header, attribute) {
			var me = this;

			if (Ext.isEmpty(header.renderer))
				header.renderer = function(value, metadata, record, rowIndex, colIndex, store, view) {
					value = value || record.get(header.dataIndex);

					if (!Ext.isEmpty(value)) {
						if (!Ext.isEmpty(header.editor.store)) {
							var comboRecord = header.editor.store.findRecord('Id', value);

							value = !Ext.isEmpty(comboRecord) ? comboRecord.get('Description') : '';
						} else if (attribute.type == 'DATE') {
							value = me.formatDate(value);
						}

						if (Ext.isEmpty(Ext.String.trim(String(value))) && attribute[CMDBuild.core.proxy.CMProxyConstants.NOT_NULL])
							value = '<div style="width: 100%; height: 100%; border: 1px dotted red;">';

						return value;
					}

					return null;
				}
		},

		/**
		 * @returns {Ext.grid.column.Action}
		 */
		buildActionColumns: function() {
			return Ext.create('Ext.grid.column.Action', {
				align: 'center',
				width: 50,
				sortable: false,
				hideable: false,
				menuDisabled: true,
				fixed: true,

				items: [
					Ext.create('CMDBuild.core.buttons.Modify', {
						withSpacer: true,
						tooltip: CMDBuild.Translation.editRow,
						scope: this,

						isDisabled: function(grid, rowIndex, colIndex, item, record) {
							return (
								this.cmfg('widgetConfigurationGet', [
									CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
									CMDBuild.core.proxy.CMProxyConstants.READ_ONLY
								])
								|| this.cmfg('widgetConfigurationGet', [
									CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
									CMDBuild.core.proxy.CMProxyConstants.MODIFY_DISABLED
								])
							);
						},

						handler: function(grid, rowIndex, colIndex) {
							var record = grid.getStore().getAt(rowIndex);

							this.cmfg('onCustomFormLayoutGridEditRowButtonClick', record);
						}
					}),
					Ext.create('CMDBuild.core.buttons.Delete', {
						withSpacer: true,
						tooltip: CMDBuild.Translation.deleteRow,
						scope: this,

						isDisabled: function(grid, rowIndex, colIndex, item, record) {
							return (
								this.cmfg('widgetConfigurationGet', [
									CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
									CMDBuild.core.proxy.CMProxyConstants.READ_ONLY
								])
								|| this.cmfg('widgetConfigurationGet', [
									CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
									CMDBuild.core.proxy.CMProxyConstants.DELETE_DISABLED
								])
							);
						},

						handler: function(grid, rowIndex, colIndex) {
							this.cmfg('onCustomFormLayoutGridDeleteRowButtonClick', rowIndex);
						}
					})
				]
			});
		},

		/**
		 * @returns {Array} columns definitions
		 *
		 * TODO: this implementation should be refactored with FieldManager class
		 */
		buildColumns: function() {
			var columns = [];

			if (!this.cmfg('widgetConfigurationIsAttributeEmpty',  CMDBuild.core.proxy.CMProxyConstants.MODEL)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.forEach(this.cmfg('widgetConfigurationGet', CMDBuild.core.proxy.CMProxyConstants.MODEL), function(attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute.get(CMDBuild.core.proxy.CMProxyConstants.TYPE))) {
						fieldManager.attributeModelSet(Ext.create('CMDBuild.model.common.attributes.Attribute', attribute.getData()));

						columns.push(fieldManager.buildColumn(true));
					} else { // @deprecated - Old field manager
						var attribute = attribute.getAdaptedData();
						var attributesMap = CMDBuild.Management.FieldManager.getAttributesMap();

						// TODO: hack to bypass CMDBuild.Management.FieldManager.getFieldForAttr() control to check if return DisplayField
						// (correct way "var editor = CMDBuild.Management.FieldManager.getCellEditorForAttribute(attribute);")
						var editor = attributesMap[attribute.type].buildField(attribute, false, false);

						var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);

						header.flex = 1; // Apply flex 1 by default to avoid unused empty space on grids

						if (attribute.type == 'REFERENCE') { // TODO: hack to force a templateResolver build for editor that haven't a form associated like other fields types
							var xaVars = CMDBuild.Utils.Metadata.extractMetaByNS(attribute.meta, 'system.template.');
							xaVars['_SystemFieldFilter'] = attribute.filter;

							var templateResolver = new CMDBuild.Management.TemplateResolver({ // TODO: implementation of serverside template resolver
								clientForm: this.cmfg('widgetControllerPropertyGet', 'getClientForm'),
								xaVars: xaVars,
								serverVars: this.cmfg('getTemplateResolverServerVars')
							});

							editor = CMDBuild.Management.ReferenceField.buildEditor(attribute, templateResolver);

							// Force execution of template resolver
							if (!Ext.isEmpty(editor) && Ext.isFunction(editor.resolveTemplate))
								editor.resolveTemplate();

							// Manage reference selection from window
							editor.on('cmdbuild-reference-selected', function(selectedRecord, field) {
								selectedRecord = Ext.isArray(selectedRecord) ? selectedValue[0] : selectedRecord;

								var record = this.view.getSelectionModel().getSelection()[0];
								record.set(field.getName(), selectedRecord.get('Id'));
							}, this);
						}

						if (!Ext.isEmpty(header)) {
							editor.hideLabel = true;

							// Read only attributes header/editor setup
							header[CMDBuild.core.proxy.CMProxyConstants.DISABLED] = !attribute[CMDBuild.core.proxy.CMProxyConstants.WRITABLE];
							header[CMDBuild.core.proxy.CMProxyConstants.REQUIRED] = false;
							editor[CMDBuild.core.proxy.CMProxyConstants.DISABLED] = !attribute[CMDBuild.core.proxy.CMProxyConstants.WRITABLE];
							editor[CMDBuild.core.proxy.CMProxyConstants.REQUIRED] = false;

							if (attribute[CMDBuild.core.proxy.CMProxyConstants.MANDATORY] || attribute['isnotnull']) {
								header.header = '* ' + header.header; // TODO: header property is deprecated, should use "text" but FieldManager uses header so ...

								header[CMDBuild.core.proxy.CMProxyConstants.REQUIRED] = true;
								editor[CMDBuild.core.proxy.CMProxyConstants.REQUIRED] = true;
							}

							// Do not override renderer, add editor on checkbox columns and make it editable
							if (attribute[CMDBuild.core.proxy.CMProxyConstants.TYPE] != 'BOOLEAN') {
								header.editor = editor;

								this.addRendererToHeader(header, attribute);
							}

							columns.push(header);
						}

						// Force editor fields store load (must be done because FieldManager field don't works properly)
						if (!Ext.isEmpty(editor) && !Ext.isEmpty(editor.store) && editor.store.count() == 0)
							editor.store.load({ callback: CMDBuild.core.RequestBarrier.getCallback('referenceStoreLoadBarrier') });
					}
				}, this);
			}

			columns.push(this.buildActionColumns());

			return columns;
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		buildDataStore: function() {
			var storeFields = [];

			if (!this.cmfg('widgetConfigurationIsAttributeEmpty',  CMDBuild.core.proxy.CMProxyConstants.MODEL)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.forEach(this.cmfg('widgetConfigurationGet', CMDBuild.core.proxy.CMProxyConstants.MODEL), function(attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute.get(CMDBuild.core.proxy.CMProxyConstants.TYPE))) {
						fieldManager.attributeModelSet(Ext.create('CMDBuild.model.common.attributes.Attribute', attribute.getData()));

						storeFields.push(fieldManager.buildStoreField());
					} else {
						storeFields.push({ name: attribute.get(CMDBuild.core.proxy.CMProxyConstants.NAME), type: 'string' });
					}
				}, this);
			}

			return Ext.create('Ext.data.ArrayStore', {
				fields: storeFields,
				data: []
			});
		},

		/**
		 * @returns {Array} storeRecordsData
		 */
		getData: function() {
			var storeRecordsData = [];

			Ext.Array.forEach(this.view.getStore().getRange(), function(record, i, allRecords) {
				if (!Ext.isEmpty(record) && Ext.isFunction(record.getData))
					storeRecordsData.push(record.getData());
			}, this);

			return storeRecordsData;
		},

		/**
		 * @param {Object} parameters
		 * @param {String} parameters.append
		 * @param {Array} parameters.rowsObjects
		 */
		importData: function(parameters) {
			var append = Ext.isBoolean(parameters.append) ? parameters.append : false;
			var rowsObjects = Ext.isArray(parameters.rowsObjects) ? parameters.rowsObjects : [];

			this.view.getStore().loadData(rowsObjects, append);
		},

		/**
		 * Check required field value of grid store records
		 *
		 * @returns {Boolean}
		 */
		isValid: function() {
			var returnValue = true;
			var requiredAttributes = [];

			// If widget is flagged as required must return at least 1 row
			if (
				this.cmfg('widgetConfigurationGet', CMDBuild.core.proxy.CMProxyConstants.REQUIRED)
				&& this.view.getStore().getCount() == 0
			) {
				returnValue = false;
			}

			// Build required attributes names array
			Ext.Array.forEach(this.cmfg('widgetConfigurationGet', CMDBuild.core.proxy.CMProxyConstants.MODEL), function(attributeModel, i, allAttributeModels) {
				if (attributeModel.get(CMDBuild.core.proxy.CMProxyConstants.MANDATORY))
					requiredAttributes.push(attributeModel.get(CMDBuild.core.proxy.CMProxyConstants.NAME));
			}, this);

			// Check grid store records empty required fields
			this.view.getStore().each(function(record) {
				Ext.Array.forEach(requiredAttributes, function(attributeName, i, allAttributeNames) {
					if (Ext.isEmpty(record.get(attributeName))) {
						returnValue = false;

						return false;
					}
				}, this);
			}, this);

			return returnValue;
		},

		/**
		 * Add empty row to grid store
		 */
		onCustomFormLayoutGridAddRowButtonClick: function() {
			this.view.getStore().insert(0, {});
		},

		/**
		 * @param {Number} rowIndex
		 */
		onCustomFormLayoutGridDeleteRowButtonClick: function(rowIndex) {
			this.view.getStore().removeAt(rowIndex);
		},

		/**
		 * @param {Ext.data.Model} record
		 */
		onCustomFormLayoutGridEditRowButtonClick: function(record) {
			Ext.create('CMDBuild.controller.management.common.widgets.customForm.RowEdit', {
				parentDelegate: this,
				record: record
			});
		},

		/**
		 * Opens import configuration pop-up window
		 */
		onCustomFormLayoutGridImportButtonClick: function() {
			Ext.create('CMDBuild.controller.management.common.widgets.customForm.Import', { parentDelegate: this });
		},

		onCustomFormLayoutGridResetButtonClick: function() {
			this.setDefaultContent();
		},

		onCustomFormLayoutGridShow: Ext.emptyFn,

		/**
		 * @param {Array} data
		 */
		setData: function(data) {
			this.view.getStore().removeAll();

			if (!Ext.isEmpty(data))
				return this.view.getStore().loadData(data);
		},

		/**
		 * Resets widget configuration model because of a referencing of store records
		 */
		setDefaultContent: function() {
			this.cmfg('widgetConfigurationSet', {
				configurationObject: this.cmfg('widgetControllerPropertyGet', 'widgetConfiguration')[CMDBuild.core.proxy.CMProxyConstants.DATA],
				propertyName: CMDBuild.core.proxy.CMProxyConstants.DATA
			});

			this.setData(this.cmfg('widgetConfigurationGet', CMDBuild.core.proxy.CMProxyConstants.DATA));
		}
	});

})();