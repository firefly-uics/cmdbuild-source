(function() {

	Ext.require('CMDBuild.core.proxy.widgets.CMProxyWidgetGrid');

	Ext.define('CMDBuild.controller.management.common.widgets.CMGridController', {

		mixins: {
			observable: 'Ext.util.Observable',
			widgetcontroller: 'CMDBuild.controller.management.common.widgets.CMWidgetController'
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.grid.CMGrid.WIDGET_NAME
		},

		// Configurations
			cardAttributes: undefined,
			columns: undefined, // Grid column configuration variable
		// END: Configurations

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMOpenReport} view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} ownerController
		 * @param {Object} widgetDef
		 * @param {Ext.form.Basic} clientForm
		 * @param {CMDBuild.model.CMActivityInstance} card
		 *
		 * @override
		 */
		constructor: function(view, ownerController, widgetDef, clientForm, card) {
			var me = this;

			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);
			this.ownerController = ownerController;

			this.classType = _CMCache.getEntryTypeByName(widgetDef.className);
			this.view = view;
			this.grid = view.grid;
			this.view.delegate = this;

			this.view.classIdField.setValue(this.classType.get(CMDBuild.core.proxy.CMProxyConstants.ID));

			CMDBuild.Management.FieldManager.loadAttributes(
				this.classType.get(CMDBuild.core.proxy.CMProxyConstants.ID),
				function(attributes) {
					me.cardAttributes = attributes;
					me.setColumnsForClass();
				}
			);
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

				case 'onCSVUploadButtonClick':
					return this.onCSVUploadButtonClick();

				case 'onDeleteRowButtonClick' :
					return this.onDeleteRowButtonClick(param.rowIndex);

				case 'onEditRowButtonClick' :
					return this.onEditRowButtonClick(param.record);

				case 'onEditWindowClosed' :
					return this.onEditWindowClosed();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		addActionColumns: function() {
			var me = this;

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

			header.renderer = function(value, metadata, record, rowIndex, colIndex, store, view) {
				value = value || record.get(header.dataIndex);

				if (typeof value != "undefined" && value != null) {
					if (header.field.store) {
						var comboRecord = header.field.store.findRecord('Id', value);

						value = (comboRecord) ?	comboRecord.get('Description') : '';
					} else if (value && typeof value == 'object') {
						if (value instanceof Date)
							value = me.formatDate(value);
					}

					if (Ext.String.trim(value) == '' && required)
						value = '<div style="width: 100%; height: 100%; border: 1px dotted red;">';

					return value;
				}

				return null;
			};
		},

		/**
		 * @return {Object}
		 */
		buildColumnsForAttributes: function() {
			var headers = [];
			var fields = [];
			var classId = this.classType.get(CMDBuild.core.proxy.CMProxyConstants.ID);

			if (_CMUtils.isSuperclass(classId))
				headers.push(this.buildClassColumn());

			for (var i = 0; i < this.getCardAttributes().length; i++) {
				var attribute = this.getCardAttributes()[i];
				var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);
				var editor = CMDBuild.Management.FieldManager.getCellEditorForAttribute(attribute);

				editor.hideLabel = true;

				if (header) {
					if (attribute.fieldmode == 'read')
						editor.disabled = true;

					if (attribute.isnotnull) {
						header.header = '*  ' + header.header;
						editor.required = true;
					}

					header.field = editor;
					this.addRendererToHeader(header, attribute.isnotnull);
					headers.push(header);

					fields.push(header.dataIndex);
				} else if (attribute.name == 'Description') {
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
			var out = {};
			var data = [];
			var store = this.grid.getStore();

			for (var i = 0; i < store.getCount(); i++) {
				var item = store.getAt(i);

				item = Ext.encode(item.data);
				data.push(item);
			}

			if (!this.readOnly)
				out['output'] = data;

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
		 * Adapter for grid's loarRecords function
		 *
		 * @param {Array} rawData - Ex. [{ card: {...}, not_valid_fields: {...} }, {...}]
		 */
		gridLoadData: function(rawData) {
			this.grid.getStore().removeAll(); // To clear all grid datas

			for (var i = 0; i < rawData.length; ++i) {
				var cardData = rawData[i][CMDBuild.core.proxy.CMProxyConstants.CARD];

				// Resolve objects returned for reference fields, just rewrite with object's id
				for (var item in cardData)
					if (typeof cardData[item] == 'object' && !Ext.isEmpty(cardData[item][CMDBuild.core.proxy.CMProxyConstants.ID]))
						cardData[item] = cardData[item][CMDBuild.core.proxy.CMProxyConstants.ID];

				this.grid.getStore().add(Ext.create('CMDBuild.DummyModel', cardData));
			}
		},

		onAddRowButtonClick: function() {
			this.grid.getStore().add(this.getStoreForFields(this.columns.fields));
		},

		/**
		 * Uses importCSV calls to store and get CSV data from server and check if CSV has right fields
		 */
		onCSVUploadButtonClick: function() {
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.widgets.CMProxyWidgetGrid.uploadCsv({
				form: this.view.csvUploadForm.getForm(),
				scope: this,
				success: function(response, options) {
					CMDBuild.core.proxy.widgets.CMProxyWidgetGrid.getCsvRecords({
						scope: this,
						success: function(result, options, decodedResult) {
							this.gridLoadData(decodedResult.rows);
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
			});

			this.editWindow.show();
		},

		/**
		 * Saves datas to widget's grid record
		 */
		onEditWindowClosed: function() {
			var values = this.editWindow.form.getValues();

			for (var property in values)
				this.editWindow.record.set(property, values[property]);

			this.editWindow.destroy();
		},

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
		}
	});

})();