(function() {

	Ext.define('CMDBuild.view.common.field.translatable.TextArea', {
		extend: 'CMDBuild.view.common.field.translatable.Base',

		/**
		 * @return {Ext.form.field.TextArea}
		 */
		createField: function() {
			return Ext.create('Ext.form.field.TextArea', {
				name: this.name,
				allowBlank: this.allowBlank,
				vtype: this.vtype,
				flex: 1 // Full TextField width
			});
		}
	});

})();