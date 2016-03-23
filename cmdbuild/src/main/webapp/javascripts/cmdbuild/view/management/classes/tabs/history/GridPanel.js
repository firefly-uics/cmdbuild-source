(function() {

	Ext.define('CMDBuild.view.management.classes.tabs.history.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.History}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		includeRelationsCheckbox: undefined,

		config: {
			plugins: [
				Ext.create('CMDBuild.view.management.common.tabs.history.RowExpander', {
					id: 'classesHistoryTabRowExpander'
				})
			]
		},

		autoScroll: true,
		border: false,
		cls: 'history_panel', // To apply right style to grid rows
		disableSelection: true,
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							'->',
							this.includeRelationsCheckbox = Ext.create('Ext.form.field.Checkbox', {
								boxLabel: CMDBuild.Translation.includeRelations,
								boxLabelCls: 'cmdb-toolbar-item',
								checked: false, // Default as false
								scope: this,

								handler: function(checkbox, checked) {
									this.delegate.cmfg('onTabHistoryIncludeRelationCheck');
								}
							})
						]
					})
				],
				columns: this.delegate.cmfg('tabHistoryGridColumnsGet'),
				store: this.delegate.cmfg('tabHistoryGridStoreGet')
			});

			this.callParent(arguments);
		},

		listeners: {
			viewready: function(view, eOpts) {
				this.getView().on('expandbody', function(rowNode, record, expandRow, eOpts) {
					this.doLayout(); // To refresh the scrollbar status and seems to fix also a glitch effect on row collapse

					this.delegate.cmfg('onTabHistoryRowExpand', record);
				}, this);
			}
		}
	});

})();