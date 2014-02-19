(function() {

	Ext.define("CMDBuild.delegate.administration.bim.CMBIMFormFieldsManager", {
		extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseFormFiledsManager",

		/**
		 * @return {array} an array of Ext.component to use as form items
		 */
		build: function() {
			var fields = this.callParent(arguments);

			this.activeCheckBox = new Ext.ux.form.XCheckbox( {
				fieldLabel: CMDBuild.Translation.active,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name: "active"
			});

			this.fileField = new Ext.form.field.File({
				fieldLabel: CMDBuild.Translation.ifc_file,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name: "fileIFC"
			});

			fields.push(this.activeCheckBox, this.fileField);
			return fields;
		},

		/**
		 * 
		 * @param {Ext.data.Model} record
		 * the record to use to fill the field values
		 */
		// override
		loadRecord: function(record) {
			// TODO: set active
			this.callParent(arguments);
			this.activeCheckBox.setValue(record.get("active"));
		},

		/**
		 * @return {object} values
		 * a key/value map with the values of the fields
		 */
		// override
		getValues: function() {
			var values = this.callParent(arguments);
			values["active"] = this.activeCheckBox.getValue();

			return values;
		},

		/**
		 * clear the values of his fields
		 */
		// override
		reset: function() {
			this.callParent(arguments);

			this.activeCheckBox.reset();
			this.fileField.reset();
		},

		enableFileField: function() {
			this.fileField.enable();
		}
	});
})();
