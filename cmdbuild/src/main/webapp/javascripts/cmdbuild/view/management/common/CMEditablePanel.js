(function() {
	var RIGHT_PADDING_FOR_MAX_FIELD_WIDTH = 40;
	var STATE = {
		edit: "editMode",
		display: "displayMode"
	};

	Ext.define("CMDBuild.Management.EditablePanel", {
		extend: "Ext.panel.Panel",
		attributes: undefined, //passed on new
		layout: "card",
		activeItem: 0,
		hideMode: "offsets",
		autoScroll: true,

		_state: STATE.display,
		_stateSwitchFail: false,

		initComponent : function() {
			var editSubpanel = new CMDBuild.Management.EditablePanel.SubPanel({
					attributes: this.attributes
				}),

				displaySubpanel = new CMDBuild.Management.EditablePanel.SubPanel({
					editable: false,
					attributes: this.attributes
				});

			this.items = [displaySubpanel,editSubpanel];

			// privileged functions
			this.editMode = function() {
				var layout = this.getLayout();
				this._state = STATE.edit;

				if (layout.setActiveItem) {
					try {
						layout.setActiveItem(editSubpanel.id);
					} catch (e) {
						this._stateSwitchFail = true;
					}
				}
			};

			this.displayMode = function() {
				var layout = this.getLayout();
				this._state = STATE.display;

				if (layout.setActiveItem) {
					try {
						layout.setActiveItem(displaySubpanel.id);
					} catch (e) {
						this._stateSwitchFail = true;
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

			this.mon(this, "activate", function() {
				if (this._stateSwitchFail) {
					this[this._state]();
				}
			}, this);
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

			this.on("afterlayout", function() {
				var tollerance = this.getWidth() - RIGHT_PADDING_FOR_MAX_FIELD_WIDTH;
				this.cascade(function(item) {
					if (item && (item instanceof Ext.form.Field)) {
						if (item.getWidth() > tollerance ||
								(item instanceof Ext.form.DisplayField && !item.cmSkipAfterLayoutResize)) {

							item.setWidth(tollerance);
						}
					}
				});
			}, this);
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
