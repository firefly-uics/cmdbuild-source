(function() {
	var NAME = _CMProxy.parameter.NAME;

	Ext.define("CMDBuild.delegate.administration.common.basepanel.CMBaseFormFiledsManager", {
		extend: "CMDBuild.delegate.administration.common.basepanel.CMFormFiledsManager",

		/**
		 * @return {array} an array of Ext.component to use as form items
		 */
		// override
		build: function() {
			this.name = new Ext.form.TextField({
				fieldLabel: CMDBuild.Translation.administration.modClass.attributeProperties.name,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name: NAME,
				allowBlank: false,
				vtype: "alphanum",
				cmImmutable: true
			});

			this.description= Ext.create('CMDBuild.view.common.field.translatable.Text', {
				fieldLabel: CMDBuild.Translation.administration.modClass.attributeProperties.description,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name: _CMProxy.parameter.DESCRIPTION,
				allowBlank: false,
				vtype: "commentextended"
			});

			return [this.name, this.description];
		},

		/**
		 *
		 * @param {Ext.data.Model} record
		 * the record to use to fill the field values
		 */
		// override
		loadRecord: function(record) {
			this.reset();
			this.name.setValue(record.get(NAME));
			this.description.setValue(record.get(_CMProxy.parameter.DESCRIPTION));
		},

		/**
		 * @return {object} values
		 * a key/value map with the values of the fields
		 */
		// override
		getValues: function() {
			var values = {};
			values[_CMProxy.parameter.NAME] = this.name.getValue();
			values[_CMProxy.parameter.DESCRIPTION] = this.description.getValue();

			return values;
		},

		/**
		 * clean up all the fields
		 */
		// override
		reset: function() {
			this.name.reset();
			this.description.reset();
		}
	});
})();