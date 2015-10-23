(function() {

	Ext.define('CMDBuild.view.management.dataView.sql.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.dataView.Sql}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.Print}
		 */
		printButton: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				store: CMDBuild.core.proxy.dataView.Sql.getStore()
			});

			this.pagingBar = Ext.create('Ext.toolbar.Paging', {
				store: this.store,
				displayInfo: true,
				displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of + ' {2}',
				emptyMsg: CMDBuild.Translation.common.display_topic_none,
				items: [
					new CMDBuild.field.GridSearchField({ grid: this }),
					new CMDBuild.view.management.common.filter.CMFilterMenuButton({ disabled: true }),
					this.printButton = Ext.create('CMDBuild.core.buttons.iconized.Print', {
						delegate: this.delegate,
						formatList: [
							CMDBuild.core.proxy.CMProxyConstants.PDF,
							CMDBuild.core.proxy.CMProxyConstants.CSV
						]
					})
				]
			});

			Ext.apply(this, {
				bbar: this.pagingBar,
				columns: [],
				tbar: [
					Ext.create('CMDBuild.core.buttons.Add', {
						text: CMDBuild.Translation.addCard,
						disabled: true
					})
				]
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