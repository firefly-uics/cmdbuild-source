(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.layout.Grid', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'importData',
			'onCustomFormLayoutGridAddRowButtonClick',
			'onCustomFormLayoutGridDeleteRowButtonClick' ,
			'onCustomFormLayoutGridEditRowButtonClick',
			'onCustomFormLayoutGridImportButtonClick',
			'onCustomFormLayoutGridResetButtonClick'
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

			this.view = Ext.create('CMDBuild.view.management.common.widgets.customForm.layout.GridPanel', {
				delegate: this,
				columns: this.buildColumns(),
				store: this.buildDataStore()
			});
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

						if (Ext.isEmpty(Ext.String.trim(value)) && attribute[CMDBuild.core.constants.Proxy.NOT_NULL])
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
					Ext.create('CMDBuild.core.buttons.iconized.Modify', {
						withSpacer: true,
						tooltip: CMDBuild.Translation.editRow,
						scope: this,

						isDisabled: function(grid, rowIndex, colIndex, item, record) {
							return (
								this.cmfg('widgetConfigurationGet', [
									CMDBuild.core.constants.Proxy.CAPABILITIES,
									CMDBuild.core.constants.Proxy.READ_ONLY
								])
								|| this.cmfg('widgetConfigurationGet', [
									CMDBuild.core.constants.Proxy.CAPABILITIES,
									CMDBuild.core.constants.Proxy.MODIFY_DISABLED
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
									CMDBuild.core.constants.Proxy.CAPABILITIES,
									CMDBuild.core.constants.Proxy.READ_ONLY
								])
								|| this.cmfg('widgetConfigurationGet', [
									CMDBuild.core.constants.Proxy.CAPABILITIES,
									CMDBuild.core.constants.Proxy.DELETE_DISABLED
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

			if (!this.cmfg('widgetConfigurationIsAttributeEmpty',  CMDBuild.core.constants.Proxy.MODEL)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.forEach(this.cmfg('widgetConfigurationGet', CMDBuild.core.constants.Proxy.MODEL), function(attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute.get(CMDBuild.core.constants.Proxy.TYPE))) {
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
								clientForm: this.cmfg('controllerPropertyGet', 'getClientForm'),
								xaVars: xaVars,
								serverVars: this.cmfg('getTemplateResolverServerVars')
							});

							editor = CMDBuild.Management.ReferenceField.buildEditor(attribute, templateResolver);

							// Force execution of template resolver
							if (!Ext.isEmpty(editor) && Ext.isFunction(editor.resolveTemplate))
								editor.resolveTemplate();
						}

						if (!Ext.isEmpty(header)) {
							editor.hideLabel = true;

							// Read only attributes header/editor setup
							header[CMDBuild.core.constants.Proxy.DISABLED] = !attribute[CMDBuild.core.constants.Proxy.WRITABLE];
							header[CMDBuild.core.constants.Proxy.REQUIRED] = false;
							editor[CMDBuild.core.constants.Proxy.DISABLED] = !attribute[CMDBuild.core.constants.Proxy.WRITABLE];
							editor[CMDBuild.core.constants.Proxy.REQUIRED] = false;

							if (attribute[CMDBuild.core.constants.Proxy.MANDATORY]) {
								header.header = '* ' + header.header; // TODO: header property is deprecated, should use "text" but FieldManager uses header so ...

								header[CMDBuild.core.constants.Proxy.REQUIRED] = true;
								editor[CMDBuild.core.constants.Proxy.REQUIRED] = true;
							}

							// Do not override renderer, add editor on checkbox columns and make it editable
							if (attribute[CMDBuild.core.constants.Proxy.TYPE] != 'BOOLEAN') {
								header.editor = editor;

								this.addRendererToHeader(header, attribute);
							}

							columns.push(header);
						}

						// Force editor fields store load (must be done because FieldManager field don't works properly)
						if (!Ext.isEmpty(editor) && !Ext.isEmpty(editor.store) && editor.store.count() == 0)
							editor.store.load();
					}
				}, this);
			}

			columns.push(this.buildActionColumns());

			return columns;
		},

		/**
		 * @returns {Ext.data.Store}
		 */
		buildDataStore: function() {
			var storeFields = [];

			if (!this.cmfg('widgetConfigurationIsAttributeEmpty',  CMDBuild.core.constants.Proxy.MODEL)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.forEach(this.cmfg('widgetConfigurationGet', CMDBuild.core.constants.Proxy.MODEL), function(attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute.get(CMDBuild.core.constants.Proxy.TYPE))) {
						fieldManager.attributeModelSet(Ext.create('CMDBuild.model.common.attributes.Attribute', attribute.getData()));

						storeFields.push(fieldManager.buildStoreField());
					} else {
						storeFields.push({ name: attribute.get(CMDBuild.core.constants.Proxy.NAME), type: 'string' });
					}
				}, this);
			}

			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				fields: storeFields,
				data: []
			});
		},

		/**
		 * @returns {Array}
		 */
		getData: function() {
			return this.view.getStore().getRange();
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
		 *
		 * @override
		 */
		isValid: function() {
			var returnValue = true;
			var requiredAttributes = [];

			// If widget is flagged as required must return at least 1 row
			if (
				this.cmfg('widgetConfigurationGet',CMDBuild.core.constants.Proxy.REQUIRED)
				&& this.view.getStore().getCount() == 0
			) {
				returnValue = false;
			}

			// Build columns required array
			Ext.Array.forEach(this.view.columns, function(column, i, allColumns) {
				if (column[CMDBuild.core.constants.Proxy.REQUIRED])
					requiredAttributes.push(column[CMDBuild.core.constants.Proxy.DATA_INDEX]);
			}, this);

			// Check grid store records empty required fields
			this.view.getStore().each(function(record) {
				Ext.Array.forEach(requiredAttributes, function(attribute, i, allAttributes) {
					if (Ext.isEmpty(record.get(attribute))) {
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

		/**
		 * @param {Array} data
		 */
		setData: function(data) {
			return this.view.getStore().loadRecords(data);
		},

		/**
		 * Resets widget configuration model because of a referencing of store records
		 */
		setDefaultContent: function() {
			this.cmfg('widgetConfigurationSet', {
				configurationObject: this.cmfg('controllerPropertyGet', 'widgetConfiguration')[CMDBuild.core.constants.Proxy.DATA],
				propertyName: CMDBuild.core.constants.Proxy.DATA
			});

			this.setData(this.cmfg('widgetConfigurationGet', CMDBuild.core.constants.Proxy.DATA));
		}
	});

})();