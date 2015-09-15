(function() {

	Ext.define('CMDBuild.view.administration.group.GroupView', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.administration.group.Group}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.usersAndGroups + ' - ' + CMDBuild.Translation.groups,

		/**
		 * @property {Ext.tab.Panel}
		 */
		tabPanel: undefined,

		bodyCls: 'cmgraypanel-nopadding',
		border: true,
		frame: false,
		layout: 'fit',

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addGroup,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onGroupAddButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.tabPanel = Ext.create('Ext.tab.Panel', {
						frame: false,
						border: false,

						items: []
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();