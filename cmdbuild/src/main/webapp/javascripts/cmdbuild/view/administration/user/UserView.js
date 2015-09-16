(function() {

	Ext.define('CMDBuild.view.administration.user.UserView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.user.User}
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
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,
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