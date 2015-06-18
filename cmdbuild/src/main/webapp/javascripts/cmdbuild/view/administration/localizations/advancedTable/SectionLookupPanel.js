(function() {

	Ext.define('CMDBuild.view.administration.localizations.advancedTable.SectionLookupPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.model.localizations.advancedTable.TreeStore'],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.advancedTable.SectionLookup}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.localizations.common.AdvancedTableGrid}
		 */
		grid: undefined,

		bodyCls: 'cmgraypanel',
		layout: 'fit',
		title: '@@ Lookup',

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.Expand', {
								text: CMDBuild.Translation.expandAll,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onAdvancedTableExpandAll', this.grid);
								}
							}),
							Ext.create('CMDBuild.core.buttons.Collapse', {
								text: CMDBuild.Translation.collapseAll,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onAdvancedTableCollapseAll', this.grid);
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.localizations.common.AdvancedTableGrid', {
						delegate: this.delegate,
						columns: this.delegate.cmfg('onAdvancedTableBuildColumns'),
						store: this.delegate.cmfg('onAdvancedTableBuildStore')
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onAdvancedTableLookupShow');
			}
		}
	});

})();