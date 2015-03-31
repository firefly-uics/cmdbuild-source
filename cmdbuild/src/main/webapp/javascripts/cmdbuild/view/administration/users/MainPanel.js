(function() {

	Ext.define('CMDBuild.view.administration.users.MainPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.users.Main}
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
			var me = this;

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
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [
							Ext.create('Ext.button.Button', {
								iconCls: 'add',
								text: CMDBuild.Translation.addUser,

								handler: function(button, e) {
									me.delegate.cmOn('onUserAddButtonClick');
								}
							})
						]
					}
				],
				items: [this.grid, this.form]
			});

			this.callParent(arguments);
		}
	});

})();