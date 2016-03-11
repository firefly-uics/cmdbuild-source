(function () {

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

		initComponent: function () {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.panel.Panel', {
						region: 'north',
						border: false,
						contentEl: 'header',
						frame: false,
						height: 45
					}),
					Ext.create('Ext.panel.Panel', {
						region: 'center',
						border: false,
						frame: false,
						id: 'login-wrapper',

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
