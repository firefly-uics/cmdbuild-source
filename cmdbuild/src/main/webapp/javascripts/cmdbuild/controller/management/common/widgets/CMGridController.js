(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.CMGridController', {
		extend: 'CMDBuild.controller.management.common.widgets.CMWidgetController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.Grid'
		],

		mixins: {
			observable: 'Ext.util.Observable'
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.grid.CMGrid.WIDGET_NAME
		},

		/**
		 * @property {Array}
		 */
		cardAttributes: undefined,

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		classType: undefined,

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * Grid column configuration variable
		 *
		 * @proeprty {Array}
		 */
		columns: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.grid.CMGridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.grid.CMGrid}
		 */
		view: undefined,

		/**
		 * @property {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {CMDBuild.view.management.common.widgets.grid.CMGrid} view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} ownerController
		 * @param {Object} widgetConf
		 * @param {Ext.form.Basic} clientForm
		 * @param {CMDBuild.model.CMActivityInstance} card
		 *
		 * @override
		 */
		constructor: function(view, ownerController, widgetConf, clientForm, card) {
			var me = this;

			this.mixins.observable.constructor.call(this);

			this.callParent(arguments);

			this.classType = _CMCache.getEntryTypeByName(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME]);
			this.grid = this.view.grid;
			this.view.delegate = this;
			this.view.grid.delegate = this;

			if (!Ext.isEmpty(this.classType)) {
				CMDBuild.Management.FieldManager.loadAttributes(
					this.classType.get(CMDBuild.core.proxy.CMProxyConstants.ID),
					function(attributes) {
						me.cardAttributes = attributes;
						me.setColumnsForClass();
						me.loadPresets();
					}
				);
			} else {
				CMDBuild.Msg.error(
					CMDBuild.Translation.error,
					'CMGridController error: classType error with className ' + this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME],
					true
				);
			}
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAddRowButtonClick' :
					return this.onAddRowButtonClick();

				case 'onCSVImportButtonClick':
					return this.onCSVImportButtonClick();

				case 'onCSVUploadButtonClick':
					return this.onCSVUploadButtonClick();

				case 'onDeleteRowButtonClick' :
					return this.onDeleteRowButtonClick(param.rowIndex);

				case 'onEditRowButtonClick' :
					return this.onEditRowButtonClick(param.record);

				case 'onEditWindowAbortButtonClick':
					return this.onEditWindowAbortButtonClick();

				case 'onEditWindowSaveButtonClick' :
					return this.onEditWindowSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		addActionColumns: function() {
			var me = this;
			var readOnly = this.widgetConf.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.READ_ONLY)
				&& this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY];
			var disableDeleteRow = this.widgetConf.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.DISABLE_DELETE_ROW)
				&& this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.DISABLE_DELETE_ROW];

			this.columns.headers.push(
				{
					xtype: 'actioncolumn',
					width: 30,
					align: 'center',
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					items: [
						{
							iconCls: 'modify',
							tooltip: CMDBuild.Translation.row_edit,

							handler: function(grid, rowIndex, colIndex) {
								var record = grid.getStore().getAt(rowIndex);

								me.cmOn('onEditRowButtonClick', {
									record: record
								});
							},

							isDisabled: function(grid, rowIndex, colIndex, item, record) {
								return readOnly;
							}
						}
					]
				},
				{
					xtype: 'actioncolumn',
					width: 30,
					align: 'center',
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					items: [
						{
							iconCls: 'delete',
							tooltip: CMDBuild.Translation.row_delete,

							handler: function(grid, rowIndex, colIndex) {
								me.cmOn('onDeleteRowButtonClick', {
									rowIndex: rowIndex
								});
							},

							isDisabled: function(grid, rowIndex, colIndex, item, record) {
								return readOnly || disableDeleteRow;
							}
						}
					]
				}
			);
		},

		/**
		 * @param {Object} header
		 * @param {Boolean} required
		 *
		 * @return {String} value
		 */
		addRendererToHeader: function(header, required) {
			var me = this;

			if (Ext.isEmpty(header.renderer))
				header.renderer = function(value, metadata, record, rowIndex, colIndex, store, view) {
					value = value || record.get(header.dataIndex);

					if (!Ext.isEmpty(value)) {
						if (header.field.store) {
							var comboRecord = header.field.store.findRecord('Id', value);

							value = (comboRecord) ?	comboRecord.get('Description') : '';
						} else if (value && typeof value == 'object') {
							if (value instanceof Date)
								value = me.formatDate(value);
						}

						if (Ext.isEmpty(Ext.String.trim(value)) && required)
							value = '<div style="width: 100%; height: 100%; border: 1px dotted red;">';

						return value;
					}

					return null;
				}
		},

		/**
		 * @override
		 */
		beforeActiveView: function() {
			// Disable add button
			if (
				this.widgetConf.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.DISABLE_ADD_ROW)
				&& this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.DISABLE_ADD_ROW]
			) {
				this.view.addButton.setDisabled(true);
			}

			// Disable import from CSV button
			if (
				this.widgetConf.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.DISABLE_IMPORT_FROM_CSV)
				&& this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.DISABLE_IMPORT_FROM_CSV]
			) {
				this.view.importFromCSVButton.setDisabled(true);
			}

			// Disable buttons for readOnly mode
			if (
				this.widgetConf.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.READ_ONLY)
				&& this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY]
			) {
				this.view.addButton.setDisabled(true);
				this.view.importFromCSVButton.setDisabled(true);

				this.grid.on('beforeedit', function(plugin, edit) {
					return false;
				});
			}
		},

		/**
		 * Builds columns for grid with cell editors.
		 * Builds attributesToTranslate array where stores all attributes with needs translations from Id to Description.
		 *
		 * @return {Object}
		 */
		buildColumnsForAttributes: function() {
			var me = this;
			var headers = [];
			var fields = [];
			var classId = this.classType.get(CMDBuild.core.proxy.CMProxyConstants.ID);

			if (_CMUtils.isSuperclass(classId))
				headers.push(this.buildClassColumn());

			for (var i = 0; i < this.getCardAttributes().length; i++) {
				var attribute = this.getCardAttributes()[i];
				var attributesMap = CMDBuild.Management.FieldManager.getAttributesMap();
				var editor = {};
				var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);

				if (attribute.type == 'REFERENCE') { // TODO: hack to force a templateResolver buid for editor that haven't a form associated like other fields types
					var xaVars = CMDBuild.Utils.Metadata.extractMetaByNS(attribute.meta, "system.template.");
					xaVars["_SystemFieldFilter"] = attribute.filter;

					var templateResolver = new CMDBuild.Management.TemplateResolver({
						clientForm: me.clientForm,
						xaVars: xaVars,
						serverVars: this.getTemplateResolverServerVars()
					});

					editor = CMDBuild.Management.ReferenceField.buildEditor(attribute, templateResolver);

					if (!Ext.Object.isEmpty(editor) && typeof editor.resolveTemplate == 'function')
						editor.resolveTemplate();
				} else {
					// TODO: hack to bypass CMDBuild.Management.FieldManager.getFieldForAttr() control to check if return DisplayField
					// (correct way "var editor = CMDBuild.Management.FieldManager.getCellEditorForAttribute(attribute);")
					editor = attributesMap[attribute.type].buildField(attribute, false, false);
				}
				editor.hideLabel = true;

				if (header) {
					if (attribute[CMDBuild.core.proxy.CMProxyConstants.FIELD_MODE] == CMDBuild.core.proxy.CMProxyConstants.READ)
						editor.disabled = true;

					if (attribute[CMDBuild.core.proxy.CMProxyConstants.NOT_NULL]) {
						header.header = '* ' + header.header;

						header[CMDBuild.core.proxy.CMProxyConstants.REQUIRED] = true;
						editor[CMDBuild.core.proxy.CMProxyConstants.REQUIRED] = true;
					} else {
						header[CMDBuild.core.proxy.CMProxyConstants.REQUIRED] = false;
						editor[CMDBuild.core.proxy.CMProxyConstants.REQUIRED] = false;
					}

					// Do not override renderer, add editor on checkbox columns and make it editable
					if (attribute[CMDBuild.core.proxy.CMProxyConstants.TYPE] != 'BOOLEAN') {
						header.field = editor;
						this.addRendererToHeader(header, attribute[CMDBuild.core.proxy.CMProxyConstants.NOT_NULL]);
					} else {
						header.cmReadOnly = false;
					}

					// Read only attributes header setup
					header.disabled = (attribute[CMDBuild.core.proxy.CMProxyConstants.FIELD_MODE] == CMDBuild.core.proxy.CMProxyConstants.READ) ? true : false,

					headers.push(header);

					fields.push(header.dataIndex);
				} else if (attribute[CMDBuild.core.proxy.CMProxyConstants.NAME] == 'Description') {
					// FIXME Always add Description, even if hidden, for the reference popup
					fields.push('Description');
				}
			}

			return {
				headers: headers,
				fields: fields
			};
		},

		/**
		 * To decode "function" presetsType and fill grid store
		 *
		 * @param {String} presetsString
		 */
		decodeFunctionPresets: function(presetsString) {
			// Validate presetsString
			CMDBuild.core.proxy.widgets.Grid.getFunctions({
				scope: this,
				success: function(result, options, decodedResult) {
					var me = this;
					var isPresetsStringValid = false;

					Ext.Array.each(decodedResult.response, function(record) {
						if (record[CMDBuild.core.proxy.CMProxyConstants.NAME] == presetsString)
							isPresetsStringValid = true;
					});

					if (isPresetsStringValid) {
						var functionParamsNames = [];
						var params = {};
						var widgetUnmanagedVariables = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.VARIABLES];

						// Resolve templates for widget configuration "function" type
						var templateResolver = new CMDBuild.Management.TemplateResolver({
							clientForm: me.clientForm,
							xaVars: widgetUnmanagedVariables,
							serverVars: this.getTemplateResolverServerVars()
						});

						templateResolver.resolveTemplates({
							attributes: Ext.Object.getKeys(widgetUnmanagedVariables),
							callback: function(out, ctx) {
								widgetUnmanagedVariables = out;
							}
						});

						// Builds functionParams with all param names
						for (var index in _CMCache.getDataSourceInput(presetsString)) {
							var functionParamDefinitionObject = _CMCache.getDataSourceInput(presetsString)[index];

							functionParamsNames.push(functionParamDefinitionObject[CMDBuild.core.proxy.CMProxyConstants.NAME]);
						}

						var functionParams = Ext.Array.intersect(functionParamsNames, Object.keys(widgetUnmanagedVariables));

						for (var index in functionParams)
							params[functionParams[index]] = widgetUnmanagedVariables[functionParams[index]];

						this.grid.reconfigure(
							CMDBuild.core.proxy.widgets.Grid.getStoreFromFunction({
								fields: _CMCache.getDataSourceOutput(presetsString),
								extraParams: {
									'function': presetsString,
									params: Ext.encode(params)
								}
							})
						);
					} else {
						CMDBuild.Msg.error(
							CMDBuild.Translation.error,
							'CMGridController decodeFunctionPresets: SQL function not found',
							true
						);
					}
				}
			});
		},

		/**
		 * To decode "text" presetsType strings, uses widget separators
		 *
		 * @param {String} presetsString
		 *
		 * @return {Array} decodedArray
		 */
		decodeTextPresets: function(presetsString) {
			var cardsArray = presetsString.split(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.CARD_SEPARATOR]);
			var decodedArray = [];

			for (var item in cardsArray) { // Decode cards
				var card = cardsArray[item];

				if (!Ext.isEmpty(card)) {
					var buffer = {};
					var cardAttributes = card.split(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_SEPARATOR]);

					for (var index in cardAttributes) { // Decode card's attributes
						var attribute = cardAttributes[index];
						var keyValueArray = attribute.split(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.KEY_VALUE_SEPARATOR]);

						buffer[keyValueArray[0]] = keyValueArray[1];
					}

					decodedArray.push(buffer);
				}
			}

			return decodedArray;
		},

		/**
		 * @param {Object} date
		 *
		 * @return {String}
		 */
		formatDate: function(date) {
			var day = date.getDate();
			var month = date.getMonth() + 1; // getMonth return 0-11

			if (day < 10)
				day = '0' + day;

			if (month < 10)
				month = '0' + month;

			return day + '/' + month + '/' + date.getFullYear();
		},

		// GETters functions
			/**
			 * @return {Array} cardAttributes
			 */
			getCardAttributes: function() {
				return this.cardAttributes;
			},

			/**
			 * @return {Object} out
			 *
			 * @override
			 */
			getData: function() {
				var me = this;
				var out = {};
				var data = [];
				var store = this.grid.getStore();

				for (var i = 0; i < store.getCount(); i++) {
					var item = store.getAt(i);
					var xaVars = item.getData();

					// Resolve templates for widget configuration "text" type
					var templateResolver = new CMDBuild.Management.TemplateResolver({
						clientForm: me.clientForm,
						xaVars: xaVars,
						serverVars: this.getTemplateResolverServerVars()
					});

					templateResolver.resolveTemplates({
						attributes: Ext.Object.getKeys(xaVars),
						callback: function(out, ctx) {
							data.push(
								Ext.encode(
									Ext.Object.merge(item.getData(), out)
								)
							);
						}
					});
				}

				if (!this.readOnly)
					out[CMDBuild.core.proxy.CMProxyConstants.OUTPUT] = data;

				return out;
			},

			/**
			 * @param {Array} fields
			 *
			 * @return {Ext.data.Store}
			 */
			getStoreForFields: function(fields) {
				fields.push({ name: 'Id', type: 'int' });
				fields.push({ name: 'IdClass', type: 'int' });

				return Ext.create('Ext.data.Store', {
					fields: fields,
					data: []
				});
			},

		/**
		 * Check required field value of grid store records
		 *
		 * @return {Boolean}
		 *
		 * @override
		 */
		isValid: function() {
			var returnValue = true;
			var requiredAttributes = [];

			// If widget is flagged as required must return at least 1 row
			if (this.widgetConf.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.REQUIRED) && this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.REQUIRED] && this.grid.getStore().getCount() == 0)
				returnValue = false;

			// Build columns required array
			for (var i in this.columns.headers) {
				var header = this.columns.headers[i];

				if (header[CMDBuild.core.proxy.CMProxyConstants.REQUIRED])
					requiredAttributes.push(header[CMDBuild.core.proxy.CMProxyConstants.DATA_INDEX]);
			}

			// Check grid store records empty required fields
			this.grid.getStore().each(function(record) {
				for (var y in requiredAttributes)
					if (Ext.isEmpty(record.get(requiredAttributes[y]))) {
						returnValue = false;

						return false;
					}

				return true;
			}, this);

			return returnValue;
		},

		/**
		 * Read presets and loads data to grid store
		 */
		loadPresets: function() {
			if (!Ext.isEmpty(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.PRESETS])) {
				switch (this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.PRESETS_TYPE]) {
					case 'text':
						return this.setGridDataFromTextPresets(
							this.decodeTextPresets(
								this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.PRESETS]
							)
						);

					case 'function':
						return this.decodeFunctionPresets(
							this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.PRESETS]
						);

					default:
						CMDBuild.Msg.error(
							CMDBuild.Translation.error,
							'CMGridController: wrong serializationType (' + this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.SERIALIZATION_TYPE] + ') format or value',
							true
						);
				}
			}
		},

		onAddRowButtonClick: function() {
			this.grid.getStore().add(this.getStoreForFields(this.columns.fields));
		},

		/**
		 * Opens importCSV configuration popup
		 */
		onCSVImportButtonClick: function() {
			this.importCSVWindow = Ext.create('CMDBuild.view.management.common.widgets.grid.CMImportCSVWindow', {
				title: CMDBuild.Translation.importFromCSV,
				classId: this.classType.get(CMDBuild.core.proxy.CMProxyConstants.ID),
				delegate: this
			}).show();
		},

		/**
		 * Uses importCSV calls to store and get CSV data from server and check if CSV has right fields
		 */
		onCSVUploadButtonClick: function() {
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.widgets.Grid.uploadCsv({
				form: this.importCSVWindow.csvUploadForm.getForm(),
				scope: this,
				success: function(response, options) {
					CMDBuild.core.proxy.widgets.Grid.getCsvRecords({
						scope: this,
						success: function(result, options, decodedResult) {
							this.setGridDataFromCsv(decodedResult.rows);
							this.importCSVWindow.destroy();
						}
					});
				},
				failure: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		},

		/**
		 * @param {Int} rowIndex
		 */
		onDeleteRowButtonClick: function(rowIndex) {
			this.grid.getStore().removeAt(rowIndex);
		},

		/**
		 * Edit row data in new popup window
		 *
		 * @param {Ext.data.Store.ImplicitModel} record
		 */
		onEditRowButtonClick: function(record) {
			this.editWindow = Ext.create('CMDBuild.view.management.common.widgets.grid.CMGridEditWindow', {
				title: CMDBuild.Translation.row_edit,
				record: record,
				delegate: this
			}).show();
		},

		onEditWindowAbortButtonClick: function() {
			this.editWindow.destroy();
		},

		/**
		 * Saves data to widget's grid
		 */
		onEditWindowSaveButtonClick: function() {
			var values = this.editWindow.form.getValues();

			for (var property in values)
				this.editWindow.record.set(property, values[property]);

			this.onEditWindowAbortButtonClick();
		},

		// SETters functions
			/**
			 * Build columns for class in view's grid
			 */
			setColumnsForClass: function() {
				this.columns = this.buildColumnsForAttributes();

				this.addActionColumns();

				this.grid.reconfigure(
					this.getStoreForFields(this.columns.fields),
					this.columns.headers
				);
			},

			/**
			 * Adapter for grid's loarRecords function
			 *
			 * @param {Array} rawData - Ex. [{ card: {...}, not_valid_fields: {...} }, {...}]
			 */
			setGridDataFromCsv: function(rawData) {
				// To clear all grid data if mode = 'replace'
				if (!Ext.isEmpty(this.importCSVWindow) && this.importCSVWindow.csvImportModeCombo.getValue() == 'replace')
					this.grid.getStore().removeAll();

				for (var i = 0; i < rawData.length; ++i) {
					var cardData = rawData[i][CMDBuild.core.proxy.CMProxyConstants.CARD];

					// Resolve objects returned for reference fields, just rewrite with object's id
					for (var item in cardData)
						if (typeof cardData[item] == 'object' && !Ext.isEmpty(cardData[item][CMDBuild.core.proxy.CMProxyConstants.ID]))
							cardData[item] = cardData[item][CMDBuild.core.proxy.CMProxyConstants.ID];

					this.grid.getStore().add(Ext.create('CMDBuild.DummyModel', cardData));
				}
			},

			/**
			 * @param {Array} data
			 */
			setGridDataFromTextPresets: function(data) {
				this.grid.getStore().loadData(data);
			}
	});

})();
