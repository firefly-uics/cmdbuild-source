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

			this.searchField = new CMDBuild.field.LocaleSearchField({
				grid: this
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

		loadData: function(data) {
			this.store.loadData(data, append = false);
		},

		filterStore: function() {
			this.store.clearFilter(false);
			var nonValid = this.validFlag.getValue();
			var query = this.searchField.getRawValue().toUpperCase();
			
			if (query == "") {
				if (nonValid) {
					this.store.filterBy(isInvalid, this);
				}
			} else {
				if (nonValid) {
					this.store.filterBy(isInvalidAndFilterQuery, this);
				} else {
					this.store.filterBy(filterQuery, this);
				}
			}
		},

		//override
		setColumnsForClass: function(classAttributes) {
			this.classAttributes = classAttributes;
		},

		//override
		getStoreForFields: function(fields) {
			fields.push("Id");
			fields.push("IdClass");
			fields.push("IdClass_value");
			
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

				var a = findInClassAttributes.call(this, headersToShow[i]);

				if (a) {
					var header = CMDBuild.Management.FieldManager.getHeaderForAttr(a);
					var editor = CMDBuild.Management.FieldManager.getFieldForAttr(a, readOnly = false, skipSubFields = true);
					editor.hideLabel = true;

					if (a.type == "REFERENCE" || a.type == "LOOKUP") {
						fields.push(header.dataIndex); // to have both name and name_value
						header.dataIndex = removeValuePostfix(header.dataIndex);
						editor.on('select', updateStoreRecord, this);
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

			var s = this.getStoreForFields.call(this, fields);
			this.reconfigure(s, headers);
		},

		getRecordToUpload: function(){
			var data = [];

			this.store.each(function(r) {
				if (!r.dirty) {
					return;
				} else {
					var changes = r.getChanges();
					var toPush = r.data;
					for (var invalid in toPush.invalid_fields) {
						if (changes[invalid]) {
							continue;
						}
						toPush[invalid] = invalid_fields[invalid];
					}
					data.push(toPush);
				}
			});

			return data;
		},
		
		removeAll: function() {
			this.store.removeAll();
		}
	});

	function findInClassAttributes(a) {
		for (var i=0, l=this.classAttributes.length; i<l; i++) {
			var classAttr = this.classAttributes[i];
			if (classAttr.name == a) {
				return classAttr;
			}
		}
		return null
	}
	
	function isInvalidAndFilterQuery(record, id) {
		var query = this.searchField.getRawValue().toUpperCase();
		if (this.isInvalid(record, id)) {
			return this.filterQuery(record, id);
		} else {
			return false;
		}
	}

	function isInvalid(record, id) {
		var invalidFields = record.get("invalid_fields");
		for (var i in invalidFields) {
			return true;
		}
		return false;
	}

	function filterQuery(record, id) {
		var query = this.searchField.getRawValue().toUpperCase();
		for (var attr in record.data) {
			var attribute = record.data[attr];
			if (attr == 'invalid_fields') {
				for (var invalid in attribute) {
					var attributeInString = (attribute[invalid]+"").toUpperCase();
					var searchIndex = attributeInString.search(query);
					if (searchIndex != -1) {
						return true;
					}
				}
			} else {
				var attributeInString = (attribute+"").toUpperCase();
				var searchIndex = attributeInString.search(query);
				if (searchIndex != -1) {
					return true;
				}
			}
		}
		return false;
	}
	
	function isInvalidAndFilterQuery(record, id) {
		var query = this.searchField.getRawValue().toUpperCase();
		if (isInvalid.call(this, record, id)) {
			return filterQuery.call(this, record, id);
		} else {
			return false;
		}
	}
	
	function renderer(value, metadata, record, rowindex, collindex, store, grid, nameColumn) {
		var cardId = record.get("Id");
		var storeRecordIndex = grid.store.find("Id", cardId)
		var storeRecord = grid.store.getAt(storeRecordIndex)
		
		if ( !value ) {
			var invalid_attr_list = record.get("invalid_fields");
			if (invalid_attr_list[nameColumn]) {
				return	'<span class="importcsv-invalid-cell">' + invalid_attr_list[nameColumn] + '</span>';
			} else {
				return	'<span class="importcsv-empty-cell"></span>';
			}
		} else {	
			if (this.type == "REFERENCE" || this.type == "LOOKUP") 
				return storeRecord.get(nameColumn+"_value");
			else 
				return storeRecord.get(nameColumn);
		}
	}
	
	function updateStoreRecord(field, record, index) {
		record = record[0];
		var sm = this.getSelectionModel();
		var gridRecord = sm.getSelection()[0];
		var storeRecordIndex = this.store.find("Id", gridRecord.data["Id"]);
		var storeRecord = this.store.getAt(storeRecordIndex);
		storeRecord.set(field.name+"_value", record.data.Description);
		storeRecord.set(field.name, record.data.Id);
		gridRecord.set(field.name, record.data.Id);

		return false;
	}
	
	function removeValuePostfix(name) {
		var index = name.lastIndexOf("_value");
		return name.slice(0,index);
	}
})();