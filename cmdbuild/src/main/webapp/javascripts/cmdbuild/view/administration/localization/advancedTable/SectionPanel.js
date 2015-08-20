(function() {

	Ext.define('CMDBuild.view.administration.localization.advancedTable.SectionPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.advancedTable.SectionClass}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		activeOnlyCheckbox: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.common.AdvancedTableGrid}
		 */
		grid: undefined,

		/**
		 * @cfg {Boolean}
		 */
		hideActiveOnlyCheckbox: false,

		bodyCls: 'cmgraypanel',
		layout: 'fit',

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Expand', {
								text: CMDBuild.Translation.expandAll,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLocalizationAdvancedTableExpandAll', this.grid);
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Collapse', {
								text: CMDBuild.Translation.collapseAll,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLocalizationAdvancedTableCollapseAll', this.grid);
								}
							}),
							'->',
							this.activeOnlyCheckbox = Ext.create('Ext.form.field.Checkbox', {
								boxLabel: '@@ Only active',
								boxLabelCls: 'cmtoolbaritem',
								checked: true, // Default as true
								hidden: this.hideActiveOnlyCheckbox,
								scope: this,

								handler: function(checkbox, checked) {
									this.delegate.cmfg('onLocalizationAdvancedTableOnlyEnabledEntitiesCheck');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.localization.common.AdvancedTableGrid', {
						delegate: this.delegate,
						columns: this.delegate.cmfg('onLocalizationAdvancedTableBuildColumns'),
						store: this.delegate.cmfg('onLocalizationAdvancedTableBuildStore')
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onLocalizationAdvancedTableShow');
			}
		}
	});

})();