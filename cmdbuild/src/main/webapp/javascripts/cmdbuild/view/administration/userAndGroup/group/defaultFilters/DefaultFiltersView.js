(function() {

	Ext.define('CMDBuild.view.administration.userAndGroup.group.defaultFilters.DefaultFiltersView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.DefaultFilters}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.group.defaultFilters.TreePanel}
		 */
		tree: undefined,

		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.defaultFilters,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUserAndGroupGroupDefaultFiltersSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUserAndGroupGroupDefaultFiltersAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.tree = Ext.create('CMDBuild.view.administration.userAndGroup.group.defaultFilters.TreePanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onUserAndGroupGroupDefaultFiltersTabShow');
			}
		}
	});

})();