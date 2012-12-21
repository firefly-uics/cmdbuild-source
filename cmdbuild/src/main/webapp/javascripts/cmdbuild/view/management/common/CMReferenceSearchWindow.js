Ext.define("CMDBuild.Management.ReferenceSearchWindow", {
	extend: "CMDBuild.Management.CardListWindow",
	
	initComponent: function() {
		this.selection = null;

		this.saveButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.save,
			name: 'saveButton',
			disabled: true,
			handler : this.onSave,
			scope : this
		});

		this.buttonAlign = "center";
		this.buttons = [this.saveButton];

		this.callParent(arguments);
	},

	// override
	onSelectionChange: function(sm, selection) {
		if (selection.length > 0) {
			this.saveButton.enable();
			this.selection = selection[0];
		} else {
			this.saveButton.disable();
			this.selection = null;
		}
	},

	// override
	onGridDoubleClick: function() {
		this.onSave();
	},

	// private
	onSave: function() {
		if (this.selection != null) {
			this.fireEvent('cmdbuild-referencewindow-selected', this.selection);
		}
		var me = this;
		this.grid.clearFilter(function() {
			me.destroy();
		});
	}
});