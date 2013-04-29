(function() {

	var NOTE = "Notes";

	Ext.define('CMDBuild.view.administration.group.CMPrivilegeWindowAttributeModel', {
		extend: 'Ext.data.Model',
		fields: [{
			name: 'name',
			type: 'string'
		}, {
			name: 'description',
			type: 'string'
		}, {
			name: 'enabled',
			type: 'boolean'
		}],

		isDisabled: function() {
			return !this.get("enabled");
		},

		getName: function() {
			return this.get("name");
		}
	});

	Ext.define("CMDBuild.view.administration.group.CMPrivilegeWindow", {
		extend: "CMDBuild.view.common.field.CMFilterChooserWindow",
	
		// configuration
		/**
		 * the model of the group to which
		 * want to set the privileges
		 */
		group: undefined,

		/**
		 * {CMDBuild.model.CMFilterModel}
		 */
		filter: undefined,

		/**
		 * an array of objects that defines the attributes
		 */
		attributes: [],

		/**
		 * the name of the class to which
		 * apply the filter
		 */
		className: "",
		// configuration

		initComponent: function() {
			this.saveButtonText = CMDBuild.Translation.common.buttons.save;
			this.callParent(arguments);
			this.layout = "fit";
		},

		getDisabledAttributeNames: function() {
			var disabledAttributes = [];
			var store = this.columnPrivilegeGrid.getStore();
			store.each(function(record) {
				if (record.isDisabled()) {
					disabledAttributes.push(record.getName());
				}
			});

			return disabledAttributes;
		},

		// protected
		// override
		setWindowTitle: function() {
			this.title = CMDBuild.Translation.row_and_column_privileges;
		},

		// protected
		// override
		buildItems: function() {
			this.callParent(arguments);

			var data = [];
			var disabledAttributes = this.group.getDisabledAttributes();

			for (var i=0, l=this.attributes.length; i<l; ++i) {
				var attribute = this.attributes[i];
				// As usual, the notes attribute
				// is managed in a special way
				if (attribute.name == NOTE) {
					continue;
				}

				var enabled = !Ext.Array.contains(disabledAttributes, attribute.name);

				data.push({
					name: attribute.name,
					description: attribute.description,
					enabled: enabled
				});
			}

			this.columnPrivilegeGrid = new Ext.grid.Panel({
				title: CMDBuild.Translation.privileges_on_columns,
				border: false,
				columns: [{
					header: CMDBuild.Translation.name,
					dataIndex: "name",
					flex: 1
				}, {
					header: CMDBuild.Translation.description_,
					dataIndex: "description",
					flex: 1
				}, {
					xtype: "checkcolumn",
					header: CMDBuild.Translation.active,
					align: "center",
					dataIndex: "enabled",
					width: 70,
					fixed: true
				}],
				store: new Ext.data.Store({
					model: "CMDBuild.view.administration.group.CMPrivilegeWindowAttributeModel",
					data: data
				})
			});
	
			var filterChooserWindowItem = this.items;

			this.rowPrivilegePanel = new Ext.panel.Panel({
				title: CMDBuild.Translation.privileges_on_rows,
				layout: "border",
				border: false,
				items: filterChooserWindowItem
			});

			this.items = [{
				xtype: "tabpanel",
				border: false,
				items: [this.columnPrivilegeGrid, this.rowPrivilegePanel]
			}];
		}
	});

})();