(function() {

	Ext.define('CMDBuild.view.patchManager.GridContainer', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.buttons.Buttons'],

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
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.Apply', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onPatchManagerApplyButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.patchManager.GridPanel', { delegate: this.delegate })
				],
			});

			this.callParent(arguments);
		}
	});

})();