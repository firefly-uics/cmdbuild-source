(function() {
	
	Ext.define("CMDBuild.Management.EditablePanel", {
		extend: "Ext.panel.Panel",
		attributes: undefined, //passed on new
		cmMaxFieldWidth: undefined, // passed at configuration, if set is used as max value for combo grow
		layout: "card",
		activeItem: 0,
		hideMode: "offsets",
		autoScroll: true,

		initComponent : function() {
			var editSubpanel = new CMDBuild.Management.EditablePanel.SubPanel({
				attributes: this.attributes,
				cmMaxFieldWidth: this.cmMaxFieldWidth
			});
			
			var displaySubpanel = new CMDBuild.Management.EditablePanel.SubPanel({
				editable: false,
				attributes: this.attributes,
				cmMaxFieldWidth: this.cmMaxFieldWidth
			});

			this.items = [displaySubpanel,editSubpanel];

			// privileged functions
			this.editMode = function() {
				var layout = this.getLayout();
				if (layout.setActiveItem) {
					layout.setActiveItem(editSubpanel.id);
				}
			};

			this.displayMode = function() {
				var layout = this.getLayout();
				if (layout.setActiveItem) {
					try {
						layout.setActiveItem(displaySubpanel.id);
					} catch (e) {

					}
				}
			};

			this.isEmpty = function() {
				return (displaySubpanel.fields().length == 0);
			};

			this.getFields = function() {
				return editSubpanel.fields();
			};

			this.callParent(arguments);
		}
	});
	
	Ext.define("CMDBuild.Management.EditablePanel.SubPanel", {
		extend: "Ext.panel.Panel",
		frame: false,
		border: false,
		bodyCls: "x-panel-body-default-framed",
		autoScroll: true,
		labelAlign: "right",
		labelWidth: 160,
		attributes: undefined, //configuration
		editable: true,
		hideMode: "offsets",
		fields: function() {
			return this.items.items;
		},
		initComponent: function() {
			if (this.attributes) {
				this.items = getFields.call(this, this.attributes, this.editable);
			}
			this.on("show", function() {
				if (this.editable) {
					this.switchFieldsToEdit();
				}
			}, this);
			
			this.callParent(arguments);
		},
		switchFieldsToEdit: function() {
			var fields = this.fields();
			for (var i=0;  i<fields.length; ++i) {
				var field = fields[i];
				resolveFieldTemplates(field);
			}
		}
	});

	CMDBuild.Management.EditablePanel.build = function(conf) {
		var panel = new CMDBuild.Management.EditablePanel(conf);
		if (panel.isEmpty()) {
			delete panel;
			return null;
		} else {
			return panel;
		}
	}
	
	function getFields(attributes, editable) {
		var fields = [];
		if (attributes) {
			for (var i=0; i<attributes.length; ++i) {
				var attribute = attributes[i];
				var field;
				if (editable) {
					field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, this.readOnlyForm);
				} else {
					field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, true); //true to have a displayField
					if (field && this.cmMaxFieldWidth) {
						field.width =  this.cmMaxFieldWidth;
					}
				}
				if (field) {
					fields.push(field);
				}
			}
		}
		return fields;
	};
	
	function resolveFieldTemplates(field) {
		if (field.resolveTemplate) {
			field.resolveTemplate();
		}
	};

})();
