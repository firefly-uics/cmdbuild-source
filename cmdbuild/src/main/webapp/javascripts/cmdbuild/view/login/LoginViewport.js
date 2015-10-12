(function() {

	Ext.define('CMDBuild.view.login.LoginViewport', {
		extend: 'Ext.container.Viewport',

		/**
		 * @cfg {CMDBuild.controller.login.Login}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.login.FormContainer}
		 */
		formContainer: undefined,

		border: false,
		frame: true,
		layout: 'border',

		initComponent: function() {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.panel.Panel', {
						border: false,
						frame: false,
						height: 45,
						region: 'north',
						contentEl: 'header'
					}),
					Ext.create('Ext.panel.Panel', {
						border: false,
						frame: false,
						region: 'center',

						id: 'login_box_wrap',

						items: [
							this.formContainer = Ext.create('CMDBuild.view.login.FormContainer', { delegate: this.delegate })
						]
					}),
				]
			});

			this.callParent(arguments);
		}
	});

})();