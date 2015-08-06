(function() {

	Ext.define('CMDBuild.view.administration.users.UsersView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.administration.users.Users}
		 */
		delegate: undefined,

		border: true,
		frame: false,
		layout: 'border',
		title: CMDBuild.Translation.usersAndGroups + ' - ' + CMDBuild.Translation.users,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,
						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addUser,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUserAddButtonClick');
								}
							})
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();