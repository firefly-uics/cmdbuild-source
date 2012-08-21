(function() {

	var tr = CMDBuild.Translation.management.modutilities.csv;

	Ext.define("CMDBuild.view.management.utilities.CMModImportCSV", {
		extend: "Ext.panel.Panel",
		cmName: 'importcsv',
		layout: 'border',
		hideMode:  'offsets',
		frame: true,
		border: false,
	
		initComponent: function() {
			this.form = new CMDBuild.view.management.utilities.CMModImportCSV.UploadForm({
				region: "center",
				frame: true
			});
			
			this.grid = new CMDBuild.view.management.utilities.CMModImportCSV.Grid({
				region: "south",
				height: "60%",
				split: true
			});

			Ext.apply(this, {
				title: tr.title,
				items:[this.form, this.grid],
				buttonAlign: "center",
				buttons: [
					this.updateButton = new CMDBuild.buttons.UpdateButton(),
					this.confirmButton = new CMDBuild.buttons.ConfirmButton(),
					this.abortButton = new CMDBuild.buttons.AbortButton()
				]
			});
			
			this.callParent(arguments);
		}
	});
	
	
	Ext.define("CMDBuild.view.management.utilities.CMModImportCSV.UploadForm", {
		extend: "Ext.form.Panel",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},
		constructor: function() {
	
			this.classList = new CMDBuild.field.CMBaseCombo({
				store: _CMCache.getClassesStore(),
				fieldLabel : tr.selectaclass,
				width: 260,
				name : 'idClass',
				valueField : 'id',
				displayField : 'description',
				queryMode: "local",
				allowBlank : false,
				editable: false
			});

			this.uploadButton = new Ext.Button({
				 text: CMDBuild.Translation.common.buttons.upload,
				 scope: this
			 });

			Ext.apply(this, {
				encoding: 'multipart/form-data',
				fileUpload: true,
				monitorValid: true,

				items: [
					this.classList,
				{
					xtype: 'textfield',
					inputType : "file",
					fieldLabel: tr.csvfile,
					allowBlank: false,
					name: 'filecsv'
				},

				new Ext.form.ComboBox({ 
					name: 'separator',
					fieldLabel: tr.separator,
					valueField: 'value',
					displayField: 'value',
					hiddenName: 'separator',
					store: new Ext.data.SimpleStore({
						fields: ['value'],
						data : [[';'],[','],['|']]
					}),
					width: 150,
					value: ";",
					queryMode: 'local',
					editable: false,
					allowBlank: false
				})],
				buttonAlign: "center",
				buttons: [this.uploadButton]
			});

			this.callParent(arguments);
		}
	});

	var OBJECT_VALUES = "__objectValues__",
		ID = "Id",
		CLASS_ID = "IdClass",
		CLASS_DESCRIPTION = "IdClass_value",
		WRONG_FIELDS = "not_valid_values",
		CARD = "card";

	Ext.define("CMDBuild.view.management.utilities.CMModImportCSV.Grid", {
		extend: "CMDBuild.view.management.common.CMCardGrid",

		cmPaginate: false,
		cmAddGraphColumn: false,

		constructor: function() {
			this.cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit : 1
			});

			this.validFlag = new Ext.form.Checkbox({
				hideLabel: true,
				boxLabel: tr.shownonvalid,
				checked: false,
				scope: this,
				handler: function(obj, checked) {
					this.filterStore();
				}
			});

			var me = this;
			this.searchField = new CMDBuild.field.LocaleSearchField({
				grid: me,
				onTrigger1Click: function() {
					me.filterStore();
				}
			});

			this.store = new Ext.data.Store({
				fields:[],
				data: []
			});

			this.columns = [];
			this.bbar = [this.searchField, "-", this.validFlag];
			this.plugins = [this.cellEditing];
			this.callParent(arguments);
		},

		/*
		 * rawData is an array of object
		 * {
		 * 	card: {...}
		 * 	not_valid_fields: {...}
		 * }
		 */
		loadData: function(rawData) {
			var records = [];
			for (var	i=0,
						l=rawData.length,
						r=null,
						card=null; i<l; ++i) {

				r = rawData[i];
				card = r[CARD];
				card[WRONG_FIELDS] = r[WRONG_FIELDS];

				records.push(new CMDBuild.DummyModel(card));
			}

			this.store.loadRecords(records);
		},

		filterStore: function() {
			var me = this;

			this.store.clearFilter(false);
			var nonValid = this.validFlag.getValue();
			var query = this.searchField.getRawValue().toUpperCase();

			if (query == "") {
				if (nonValid) {
					this.store.filterBy(isInvalid, me);
				}
			} else {
				if (nonValid) {
					this.store.filterBy(isInvalidAndFilterQuery, me);
				} else {
					this.store.filterBy(filterQuery, me);
				}
			}
		},

		//override
		setColumnsForClass: function(classAttributes) {
			this.classAttributes = classAttributes;
		},

		//override
		getStoreForFields: function(fields) {
			fields.push(ID);
			fields.push(CLASS_ID);
			fields.push(CLASS_DESCRIPTION);

			return new Ext.data.Store({
				fields: fields,
				data: [],
				autoLoad: false
			});
		},

		configureHeadersAndStore: function(headersToShow) {
			var grid = this;
			var headers = [];
			var fields = [];

			for (var i=0, l=headersToShow.length; i<l; i++) {

				var a = getClassAttributeByName(this, headersToShow[i]);

				if (a != null) {
					var header = CMDBuild.Management.FieldManager.getHeaderForAttr(a);
					var editor = CMDBuild.Management.FieldManager.getCellEditorForAttribute(a);
					editor.hideLabel = true;

					if (a.type == "REFERENCE"
							|| a.type == "LOOKUP") {

						editor.on("select", updateStoreRecord, this);
						editor.on("cmdbuild-reference-selected", function(record, field) {
							updateStoreRecord.call(this, field, record);
						}, this);
					}

					if (header) {
						header.field = editor;
						header.hidden = false;
						header.renderer = Ext.Function.bind(renderer, a,[header.dataIndex, grid],true);
						headers.push(header);
						fields.push(header.dataIndex);
					}
				}
			}

			// Add a field to use to read the real value set by
			// the editors.
			fields.push(OBJECT_VALUES);

			this.reconfigure(this.getStoreForFields(fields), headers);
		},

		getRecordToUpload: function() {
			var data = [];
			var records = this.store.data.items || [];

			for (var i=0, l=records.length, r=null; i<l; ++i) {
				r = records[i];

				if (r.dirty) {
					var currentData = {};
					var objectValues = r.data[OBJECT_VALUES] || {};
					var wrongFields = r.get(WRONG_FIELDS);

					for (var i=0, l=this.classAttributes.length; i<l; i++) {
						var name = this.classAttributes[i].name;
						var value = objectValues[name] || r.data[name] || wrongFields[name];

						if (value) {
							if (typeof value == "object") {
								currentData[name] = value.id;
								currentData[name + "_description"] = value.description;
							} else {
								currentData[name] = value;
							}
						}
					}

					currentData[ID] = r.get(ID);
					currentData[CLASS_ID] = r.get(CLASS_ID);
					currentData[CLASS_DESCRIPTION] = r.get(CLASS_DESCRIPTION);

					data.push(currentData);
				}
			}

			return data;
		},

		removeAll: function() {
			this.store.removeAll();
		}
	});

	function getClassAttributeByName(me, name) {
		for (var i=0, l=me.classAttributes.length; i<l; i++) {
			var classAttr = me.classAttributes[i];
			if (classAttr.name == name) {
				return classAttr;
			}
		}

		return null;
	}

	function isInvalid(record, id) {
		var invalidFields = record.get(WRONG_FIELDS);
		// return true if there are some invalid fields
		for (var i in invalidFields) {
			return true;
		}
		return false;
	}

	function filterQuery(record, id) {
		var query = this.searchField.getRawValue().toUpperCase();
		var data = Ext.apply({}, record.get(WRONG_FIELDS), record.data);
		var objectValues = record.data[OBJECT_VALUES] || {};

		for (var attributeName in data) {
			var value = objectValues[attributeName] || data[attributeName];
			var attributeAsString = "";
			var searchIndex = -1;

			if (typeof value == "object") {
				value = value.description;
			}
			attributeAsString = (value+"").toUpperCase();
			searchIndex = attributeAsString.search(query);
			if (searchIndex != -1) {
				return true;
			}
		}

		return false;
	}

	function isInvalidAndFilterQuery(record, id) {
		if (isInvalid(record, id)) {
			return filterQuery.call(this, record, id);
		} else {
			return false;
		}
	}

	function renderer(value, metadata, record, rowindex, collindex, store, grid, colName) {
		// look before if there is a object value, if not search it as simple value;
		var objectValues = record.get(OBJECT_VALUES) || {};
		var v = objectValues[colName]|| record.get(colName);

		if (v && typeof v == "object") {
			v = v.description;
		}

		if (v) {
			return v;
		} else {
			var wrongs = record.get(WRONG_FIELDS);
			if (wrongs[colName]) {
				return	'<span class="importcsv-invalid-cell">' + wrongs[colName] + '</span>';
			} else {
				return	'<span class="importcsv-empty-cell"></span>';
			}
		}
	}

	function updateStoreRecord(field, selectedValue) {
		if (Ext.isArray(selectedValue)) {
			selectedValue = selectedValue[0];
		}

		var record = this.getSelectionModel().getSelection()[0];
		var objectValues = record.get(OBJECT_VALUES) || {};

		objectValues[field.name] = {
			description: selectedValue.get("Description"),
			id: selectedValue.get("Id")
		};

		record.set(OBJECT_VALUES, objectValues);

		return false; // to block the set value of the editor;
	}

})();