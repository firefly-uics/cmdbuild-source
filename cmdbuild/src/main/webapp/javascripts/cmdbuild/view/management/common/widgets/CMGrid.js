(function() {
	Ext.define("CMDBuild.view.management.common.widgets.CMGrid", {
		extend: "Ext.panel.Panel",
		autoScroll: true,	
		statics: {
			WIDGET_NAME: ".Grid"
		},

		initComponent: function() {
			this.WIDGET_NAME = this.self.WIDGET_NAME;
			this.grid = new CMDBuild.view.management.widgets.grid.CMGridPanel();
			this.items = [this.grid];
			this.addButton = addButton(this);
			this.removeButton = removeButton(this);
			this.editButton = editButton(this);
			this.tbar = [this.addButton, this.removeButton, this.editButton];
			this.callParent(arguments);
		},

		getData: function() {
			return this.grid.getData();
		},

		addRendererToHeader: function(h) {
			h.renderer = function(value, metadata, record, rowIndex, colIndex, store, view) {
				value = value || record.get(h.dataIndex);
				if (typeof value == "undefined" 
					|| value == null) {

					return "";
				}

				if (typeof value == "object") {
					/*
					 * Some values (like reference or lookup) are
					 * serialized as object {id: "", description:""}.
					 * Here we display the description
					 */
					value = value.description;
				} else if (typeof value == "boolean") {
					/*
					 * Localize the boolean values
					 */
					value = value ? Ext.MessageBox.buttonText.yes : Ext.MessageBox.buttonText.no;
				}

				return value;
			};
		},

		loadAttributes: function(classId, cb) {
			_CMCache.getAttributeList(classId, cb);
		},
		
		setColumnsForClass: function(classAttributes) {
			var columns = this.buildColumnsForAttributes(classAttributes);
			var s = this.getStoreForFields(columns.fields);

			this.suspendLayouts();
			this.grid.reconfigure(s, columns.headers);
			this.resumeLayouts(true);
		},

		getStoreForFields: function(fields) {
			var s = this.buildStore(fields);
			return s;
		},

		buildStore: function(fields) {
			fields.push({name: "Id", type: "int"});
			fields.push({name: "IdClass", type: "int"});
			return new Ext.data.Store({
				fields: fields,
				data: []
			});
		},

		buildColumnsForAttributes: function(classAttributes) {
			this.classAttributes = classAttributes;
			var headers = [];
			var fields = [];
			var classId = this.delegate.getCurrentClass().get("id");
			if (_CMUtils.isSuperclass(classId)) {
				headers.push(this.buildClassColumn());
			}

			for (var i=0; i<classAttributes.length; i++) {
				var attribute = classAttributes[i];
				var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);

				if (header) {

					this.addRendererToHeader(header);
					headers.push(header);

					fields.push(header.dataIndex);
				} 
				else if (attribute.name == "Description") {
					// FIXME Always add Description, even if hidden, for the reference popup
					fields.push("Description");
				}
			}

			return {
				headers: headers,
				fields: fields
			};
		}


		
	});

	Ext.define('CMDBuild.model.widget.GridRowModel', {
		extend: 'Ext.data.Model',
		fields: []
	});

	Ext.define("CMDBuild.view.management.widgets.grid.CMGridPanel", {
		extend: "Ext.grid.Panel",
        selModel: {
            selType: 'cellmodel'
        },
        border: false,
		initComponent: function() {

			this.columns = [{
				dataIndex: 'text',
				flex: 3,
				sortable: false
			}];

			this.callParent(arguments);
		},
		
		getData: function() {
			var data = [];
			return data;
		}
	});
	function addButton(me) {
		return Ext.create("Ext.button.Button", {
			iconCls: 'add',
			text: "@@ Add row",
			disabled: false,
			handler: function() {
				me.delegate.cmOn("onAdd");
			}
			
		});
	}
	function removeButton(me) {
		return Ext.create("Ext.button.Button", {
			iconCls: 'delete',
			text: "@@ Delete row",
			disabled: false,
			handler: function() {
				me.delegate.cmOn("onDelete");
			}
		});
	}
	function editButton(me) {
		return Ext.create("Ext.button.Button", {
			iconCls: 'modify',
			text: "@@ Edit row",
			disabled: false,
			handler: function() {
				me.delegate.cmOn("onEdit");
			}
		});
	}
	function buildGraphIconColumn(entity, headers) {
		 if (entity && entity.get("tableType") != "simpletable") {
			var graphHeader = {
					noWrap: true,
				header: '&nbsp', 
				width: 30,
				tdCls: "grid-button",
				fixed: true,
				sortable: false, 
				renderer: renderGraphIcon, 
				align: 'center', 
				dataIndex: 'Id',
				menuDisabled: true,
				hideable: false
			};
			headers.push(graphHeader);
		}
	};
        
})();