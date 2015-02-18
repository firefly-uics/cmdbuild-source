(function() {

	var tr = CMDBuild.Translation.administration.modsecurity;

	Ext.define('CMDBuild.view.administration.user.CMModUser', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @params {Ext.button.Button}
		 */
		addUserButton: undefined,

		/**
		 * @param {CMDBuild.view.administration.user.CMUserGrid}
		 */
		userForm: undefined,

		/**
		 * @param {CMDBuild.view.administration.user.CMUserForm}
		 */
		userGrid: undefined,

		cmName: 'users',

		title: tr.user.title,
		layout: 'border',
		frame: false,
		border: false,

		initComponent: function() {
			this.addUserButton = Ext.create('Ext.button.Button', {
				iconCls: 'add',
				text: tr.user.add_user
			});

			this.userGrid = Ext.create('CMDBuild.view.administration.user.CMUserGrid', {
				region: 'north',
				border: false,
				split: true,
				height: '30%'
			});

			this.userForm = Ext.create('CMDBuild.view.administration.user.CMUserForm', {
				region: 'center'
			});

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [this.addUserButton]
					}
				],
				items: [this.userGrid, this.userForm]
			});

			this.callParent(arguments);
		}
	});

})();