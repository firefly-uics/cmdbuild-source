(function () {

	Ext.define('CMDBuild.override.form.field.Date', {
		override: 'Ext.form.field.Date',

		/**
		 * The date field return null if is empty, so the form does not send anything. The hack is to return "" if the field is empty - 02/04/2013
		 *
		 * @return {String}
		 */
		getSubmitValue: function () {
			var me = this,
				format = me.submitFormat || me.format,
				value = me.getValue();

			return value ? Ext.Date.format(value, format) : "";
		}
	});

})();
