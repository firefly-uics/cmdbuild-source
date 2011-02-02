CMDBuild.Management.CSVGrid = Ext.extend(Ext.grid.EditorGridPanel, {
	columns: [],
	baseUrl: '',
	stripeRows: true,
	clickstoEdit: 1,
	remoteSort: false,
	translation: CMDBuild.Translation.management.modutilities.csv,
	
	initComponent: function() {		
		this.store = new Ext.data.JsonStore({
			root: "rows",
            totalProperty: 'results',
            local: true,
	        fields: [],
	        remoteSort: this.remoteSort,
	        grid: this
		});
		
		this.validFlag = new Ext.form.Checkbox({
			boxLabel: this.translation.shownonvalid,
			checked: false,		
			scope: this,
			handler: function(obj, checked) {			
				this.filterStore();			
			}
		});
		
		this.searchField = new CMDBuild.LocaleSearchField({
			grid: this
		});
		
		this.toolBar = new Ext.Toolbar({
			style: {padding: '5px 0 5px 10px'},
	        items: [this.validFlag, '-',this.searchField]
		});
		
		Ext.apply(this, {
            autoScroll: true,
            viewConfig: { forceFit:true },
            loadMask: true,
            layout: 'fit',            
            bbar: this.toolBar
        });
		CMDBuild.Management.CSVGrid.superclass.initComponent.apply(this, arguments);		
		
		this.searchField.onTrigger1Click = function() {
			this.grid.filterStore();
		};
	},
	
	updateGrid: function(attributes,c){		
		var attributesToShow = this.setAttributesToShow(attributes, c.headers || [])
		var editorFields = this.setEditorFields(attributesToShow);
		var headers = this.setHeaders(attributesToShow, editorFields);
		var storefields = this.setStorefields(headers);
		this.store = new Ext.data.JsonStore({
			root: "rows",
            totalProperty: 'results',
	        fields: storefields,
	        remoteSort: this.remoteSort
		});
		this.updateStoreRecords(c.rows)
		this.reconfigure(this.store, new Ext.grid.ColumnModel(headers));
		this.filterStore()
	},
	
	setEditorFields: function(attributes) {
		var editorFields = [];
		for (var i = 0; i < attributes.length; i++) {
			var attribute = attributes[i];
			var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute);
			if (attribute.type == "LOOKUP" || attribute.type == "REFERENCE")
				field.on('select', this.onComboSelect, this);
			if (attribute.type == "REFERENCE") {
				field.on('cmdbuild-reference-selected', function(record, combo) {
					var rec = new Ext.data.Record({
						'Id': record.Id,
						'Description' : record.Description
					});
					this.onComboSelect(combo, rec)
				}, this );
			}
			editorFields.push(field);
		}
		return editorFields;
	},
	
	setHeaders: function(attributes, editorfields){
		var headers = [];
		for (var i = 0; i < attributes.length; i++) {
			var attribute = attributes[i];			
			var cm = this.getColumnModel();
			var grid = this;			
			var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);
			if (attribute.type == "REFERENCE" || attribute.type == "LOOKUP") {
				var index = header.dataIndex.lastIndexOf("_value");
				header.dataIndex = header.dataIndex.slice(0,index);
			}
			if (header) {
				header.renderer = this.setRendererCondition.createDelegate(attribute,[header.dataIndex, cm, grid],true);
				header.hidden = false;
				header.editor = editorfields[i];
				headers.push(header);
			}
		}		
		return headers;
	},
	
	setStorefields: function(headers) {
		var storeFields = [];
		for (var i = 0; i < headers.length; i++) {
			var field = headers[i];
			storeFields[i] = field.dataIndex;
		}
		return storeFields;
	},
	
	updateStoreRecords: function(rows) {
		this.store.removeAll();
		var records = [];
		for ( var i = 0, len = rows.length ; i < len; i++ ) {
			var record = new Ext.data.Record(rows[i]);			
			records[i] = record;
		}
		this.store.add(records);			
	},
	
	filterStore: function() {		
		this.store.clearFilter(false);
		var nonValid = this.validFlag.getValue();
		var query = this.searchField.getRawValue().toUpperCase();
		if (query == "") {
			if (nonValid)
				this.store.filterBy(this.isInvalid, this);
		} else {
			if (nonValid)
				this.store.filterBy(this.isInvalidAndFilterQuery, this)
			else 
				this.store.filterBy(this.filterQuery, this)
		}
	},	
	
	isInvalidAndFilterQuery: function(record, id) {		
		var query = this.searchField.getRawValue().toUpperCase();
		if (this.isInvalid(record, id)) {
        	return this.filterQuery(record, id);
		} else 
			return false;
	},
	
	filterQuery: function(record, id) {
		var query = this.searchField.getRawValue().toUpperCase();
		for (var attr in record.data) {
			var attribute = record.data[attr];
    		if (attr == 'invalid_fields'){
    			for (var invalid in attribute) {
    				var attributeInString = (attribute[invalid]+"").toUpperCase();
    				var searchIndex = attributeInString.search(query);
    				if (searchIndex != -1)
    	    			return true
    			}
    		} else {
    			var attributeInString = (attribute+"").toUpperCase();
	    		var searchIndex = attributeInString.search(query);
	    		if (searchIndex != -1)
	    			return true
    		}
    	}
    	return false;
	},
	
	isInvalid: function(record, id) {
		var invalidFields = record.get("invalid_fields");
		for (var i in invalidFields)
			return true
		return false
	},
	
	setAttributesToShow: function(attributes, headerToShow) {
		var attrToshow = []
		for (var j = 0; j < attributes.length; j++) {
			var attribute = attributes[j];
			for (var i = 0 ; i < headerToShow.length ; i++) {
				var tmpAttribute = headerToShow[i];
				if (attribute.name === tmpAttribute) {
					attrToshow.push(attribute);
				}
			}
		}
		return attrToshow;
	},
	
	setRendererCondition: function(value, metadata, record, rowindex, collindex, store, nameColumn, cm, grid) {
		var cardId = record.get("Id");
		var storeRecordIndex = grid.store.find("Id", cardId)
		var storeRecord = grid.store.getAt(storeRecordIndex)
		
		if ( !value ) {
			var invalid_attr_list = record.get("invalid_fields");
			if (invalid_attr_list[nameColumn]) {
				metadata.css += " importcsv-invalid-cell";
				return invalid_attr_list[nameColumn];
			} else {
				metadata.css += " importcsv-empty-cell";
				return "";
			}
		} else {	
			if (this.type == "REFERENCE" || this.type == "LOOKUP") 
				return storeRecord.get(nameColumn+"_value");
			else 
				return storeRecord.get(nameColumn);
		}
	},
	
	onComboSelect: function(field, record, index) {
		var sm = this.getSelectionModel();
		var gridRecord = sm.selection.record;
		var storeRecordIndex = this.store.find("Id", gridRecord.data["Id"]);
		var storeRecord = this.store.getAt(storeRecordIndex);	
		storeRecord.set(field.name, record.data.Description);	
		var index = field.name.lastIndexOf("_value");
		var name = field.name.slice(0,index);	
		storeRecord.set(name, record.data.Id);	
		gridRecord.set(name, record.data.Id);
		return false;
	},	
	
	getRecordToUpload: function(){
		var records = this.store.getModifiedRecords();
		var data = [];
		for (var i = 0, len = records.length ; i < len ; i++) {
			data[i] = records[i].data;
			var changes = records[i].getChanges();
			var invalid_fields = data[i]['invalid_fields'];
			
			for (var invalid in invalid_fields) {
				if (changes[invalid])
					continue;
				data[i][invalid] = invalid_fields[invalid];
			}
		}
		return data;
	},
	
	clearRecords: function() {
		this.store.removeAll();
	}
});