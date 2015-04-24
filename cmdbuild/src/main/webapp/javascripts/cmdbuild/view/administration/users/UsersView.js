(function() {

	Ext.define('CMDBuild.view.administration.users.UsersView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.users.Users}
		 */
		delegate: undefined,

		/**
		 * @param {CMDBuild.view.administration.users.FormPanel}
		 */
		form: undefined,

		/**
		 * @param {CMDBuild.view.administration.users.GridPanel}
		 */
		grid: undefined,

		border: true,
		frame: false,
		layout: 'border',
		title: CMDBuild.Translation.usersAndGroups + ' - ' + CMDBuild.Translation.users,

		initComponent: function() {
			this.grid = Ext.create('CMDBuild.view.administration.users.GridPanel', {
				region: 'north',
				split: true,
				height: '30%'
			});

			this.form = Ext.create('CMDBuild.view.administration.users.FormPanel', {
				region: 'center'
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [
							Ext.create('CMDBuild.core.buttons.Add', {
								text: CMDBuild.Translation.addUser,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmOn('onUserAddButtonClick');
								}
							})
						]
					})
				],
				items: [this.grid, this.form]
			});

			this.callParent(arguments);
		}
	});

})();