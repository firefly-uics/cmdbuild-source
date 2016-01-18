(function() {

	Ext.define('CMDBuild.view.patchManager.PatchManagerViewport', {
		extend: 'Ext.container.Viewport',

		/**
		 * @cfg {CMDBuild.controller.patchManager.PatchManager}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.patchManager.GridContainer}
		 */
		gridContainer: undefined,

		layout: 'border',

		initComponent: function() {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.panel.Panel', {
						border: false,
						frame: false,
						height: 45,
						region: 'north',
						contentEl: 'header'
					}),
					this.gridContainer = Ext.create('CMDBuild.view.patchManager.GridContainer', {
						delegate: this.delegate,
						region: 'center'
					}),
					Ext.create('Ext.panel.Panel', {
						border: false,
						frame: false,
						height: 16,
						region: 'south',
						contentEl: 'footer'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();