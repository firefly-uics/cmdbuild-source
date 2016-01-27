(function() {

	Ext.define('CMDBuild.view.patchManager.GridContainer', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.patchManager.PatchManager}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.patchManager.GridPanel}
		 */
		grid: undefined,

		border: true,
		frame: true,
		layout: 'fit',
		title: CMDBuild.Translation.availablePatchesList,

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
							Ext.create('CMDBuild.core.buttons.text.Apply', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onPatchManagerViewportApplyButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.patchManager.GridPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();