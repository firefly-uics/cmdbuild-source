(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.layout.Grid', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.model.common.attributes.DynamicModelFromAttributes',
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'importData',
			'onCustomFormLayoutGridAddRowButtonClick',
			'onCustomFormLayoutGridDeleteRowButtonClick' ,
			'onCustomFormLayoutGridEditRowButtonClick',
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

						if (Ext.isEmpty(Ext.String.trim(value)) && attribute[CMDBuild.core.proxy.CMProxyConstants.NOT_NULL])
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
								clientForm: this.parentDelegate.clientForm,
								xaVars: xaVars,
								serverVars: this.cmfg('getTemplateResolverServerVars')
							});

							editor = CMDBuild.Management.ReferenceField.buildEditor(attribute, templateResolver);

							// Avoids to resolve field templates when form is in editMode (when you click on abort button) // TODO
	//						if (!this.parentDelegate.owner._isInEditMode && !Ext.Object.isEmpty(editor) && Ext.isFunction(editor.resolveTemplate))
	//							editor.resolveTemplate();
						}

						if (!Ext.isEmpty(header)) {
							editor.hideLabel = true;

							// Read only attributes header/editor setup
							header[CMDBuild.core.proxy.CMProxyConstants.DISABLED] = !attribute[CMDBuild.core.proxy.CMProxyConstants.WRITABLE];
							header[CMDBuild.core.proxy.CMProxyConstants.REQUIRED] = false;
							editor[CMDBuild.core.proxy.CMProxyConstants.DISABLED] = !attribute[CMDBuild.core.proxy.CMProxyConstants.WRITABLE];
							editor[CMDBuild.core.proxy.CMProxyConstants.REQUIRED] = false;

							if (attribute[CMDBuild.core.proxy.CMProxyConstants.MANDATORY]) {
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
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.common.attributes.DynamicModelFromAttributes',
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
		 * @param {Array} data
		 */
		importData: function(data) {
_debug('importData grid', data);
_debug('importData view', this.view);
			if (Ext.isArray(data))
				this.view.getStore().loadData(data); // TODO: nsert mode
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
				this.cmfg('widgetConfigurationGet',CMDBuild.core.proxy.CMProxyConstants.REQUIRED)
				&& this.view.getStore().getCount() == 0
			) {
				returnValue = false;
			}

			// Build columns required array
			Ext.Array.forEach(this.view.columns, function(column, i, allColumns) {
				if (column[CMDBuild.core.proxy.CMProxyConstants.REQUIRED])
					requiredAttributes.push(column[CMDBuild.core.proxy.CMProxyConstants.DATA_INDEX]);
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
			this.view.getStore().insert(0, Ext.create('CMDBuild.model.common.attributes.DynamicModelFromAttributes'));
		},

		/**
		 * @param {Number} rowIndex
		 */
		onCustomFormLayoutGridDeleteRowButtonClick: function(rowIndex) {
			this.view.getStore().removeAt(rowIndex);
		},

		/**
		 * @param {CMDBuild.model.common.attributes.DynamicModelFromAttributes} record
		 */
		onCustomFormLayoutGridEditRowButtonClick: function(record) {
			Ext.create('CMDBuild.controller.management.common.widgets.customForm.RowEdit', { // TODO: date fields check
				parentDelegate: this,
				record: record
			});
		},

		/**
		 * @param {Array} data
		 */
		setData: function(data) {
			return this.view.getStore().loadRecords(data);
		}
	});

})();
