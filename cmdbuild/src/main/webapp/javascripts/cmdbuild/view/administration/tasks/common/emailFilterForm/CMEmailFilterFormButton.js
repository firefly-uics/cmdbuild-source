(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormButton', {
		extend: 'Ext.button.Button',

		delegate: undefined,

		// To setup
		name: undefined,
		id: undefined,
		fieldLabel: undefined,
		titleWindow: undefined,

		icon: 'images/icons/table.png',
		considerAsFieldToDisable: true,
		border: true,
		margin: 2,

		initComponent: function() {
			this.callParent(arguments);
		},

		handler: function() {
			this.delegate.cmOn('onFilterButtonClick', {
				titleWindow: this.titleWindow
			});
		}
	});

})();