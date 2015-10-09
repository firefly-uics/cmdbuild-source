(function() {

	Ext.define('CMDBuild.view.administration.group.users.UsersView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.group.Users}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.group.users.GridPanel}
		 */
		availableGrid: undefined,

		/**
		 * @property {CMDBuild.view.administration.group.users.GridPanel}
		 */
		selectedGrid: undefined,

		bodyCls: 'cmgraypanel-nopadding',
		border: false,
		frame: false,
		overflowY: 'auto',
		split: true,
		title: CMDBuild.Translation.users,

		layout: {
			type: 'hbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.Confirm', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onGroupUsersSaveButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.availableGrid = Ext.create('CMDBuild.view.administration.group.users.GridPanel', {
						delegate: this.delegate,
						title: CMDBuild.Translation.availableUsers,

						viewConfig: {
							plugins: {
								ptype: 'gridviewdragdrop',
								dragGroup: 'firstGridDDGroup',
								dropGroup: 'secondGridDDGroup'
							}
						},
					}),
					{ xtype: 'splitter' },
					this.selectedGrid = Ext.create('CMDBuild.view.administration.group.users.GridPanel', {
						delegate: this.delegate,
						title: CMDBuild.Translation.selectedUsers,

						viewConfig: {
							plugins: {
								ptype: 'gridviewdragdrop',
								dragGroup: 'secondGridDDGroup',
								dropGroup: 'firstGridDDGroup'
							}
						},
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onGroupUsersTabShow');
			}
		}
	});

})();