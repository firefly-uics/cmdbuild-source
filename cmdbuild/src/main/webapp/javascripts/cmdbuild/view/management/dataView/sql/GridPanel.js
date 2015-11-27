(function() {

	Ext.define('CMDBuild.view.management.dataView.sql.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.dataView.Sql}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmborderbottom',
		frame: false,

		initComponent: function() {
			Ext.apply(this, { store: this.delegate.cmfg('dataViewSqlBuildStore') });

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addCard,
								disabled: true
							})
						]
					}),
					Ext.create('Ext.toolbar.Paging', {
						dock: 'bottom',
						store: this.getStore(),
						displayInfo: true,
						displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of + ' {2}',
						emptyMsg: CMDBuild.Translation.common.display_topic_none,
						items: [
							new CMDBuild.field.GridSearchField({ grid: this }),
							new CMDBuild.view.management.common.filter.CMFilterMenuButton({ disabled: true }),
							Ext.create('CMDBuild.core.buttons.iconized.split.Print', {
								delegate: this.delegate,
								formatList: [
									CMDBuild.core.constants.Proxy.PDF,
									CMDBuild.core.constants.Proxy.CSV
								]
							})
						]
					})
				],
				columns: this.delegate.cmfg('dataViewSqlBuildColumns')
			});

			this.callParent(arguments);
		},

		listeners: {
			select: function(grid, record, index, eOpts) {
				this.delegate.cmfg('onDataViewSqlGridSelect');
			}
		}
	});

})();