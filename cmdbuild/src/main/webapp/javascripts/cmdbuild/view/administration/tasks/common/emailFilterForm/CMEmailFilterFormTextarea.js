(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormTextarea', {
		extend: 'Ext.form.field.TextArea',

		delegate: undefined,

		// To setup
		name: undefined,
		id: undefined,
		fieldLabel: undefined,

		labelWidth: CMDBuild.LABEL_WIDTH,
		readOnly: true,
		width: CMDBuild.CFG_BIG_FIELD_WIDTH,

		initComponent: function() {
			this.callParent(arguments);
		}
	});

})();