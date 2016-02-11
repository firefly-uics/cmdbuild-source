(function() {

	Ext.define('CMDBuild.view.administration.group.defaultFilters.DefaultFiltersView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.group.DefaultFilters}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.group.defaultFilters.TreePanel}
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
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onGroupDefaultFiltersSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onGroupDefaultFiltersAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.tree = Ext.create('CMDBuild.view.administration.group.defaultFilters.TreePanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onGroupDefaultFiltersTabShow');
			}
		}
	});

})();