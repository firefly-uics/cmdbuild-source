(function() {

	Ext.define('CMDBuild.view.common.field.translatable.Text', {
		extend: 'CMDBuild.view.common.field.translatable.Base',

		/**
		 * @return {Ext.form.field.Text}
		 */
		createField: function() {
			return Ext.create('Ext.form.field.Text', {
				name: this.name,
				allowBlank: this.allowBlank,
				vtype: this.vtype,
				flex: 1 // Full TextField width
			});
		}
	});

})();