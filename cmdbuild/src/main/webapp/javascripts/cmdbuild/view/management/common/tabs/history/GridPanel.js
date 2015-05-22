(function() {

	/**
	 * Abstract class to extends to implements custom row expander instance
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.view.management.common.tabs.history.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.History}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		includeRelationsCheckbox: undefined,

		autoScroll: true,
		border: false,
		cls: 'history_panel', // To apply right style to grid rows
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							'->',
							this.includeRelationsCheckbox = Ext.create('Ext.form.field.Checkbox', {
								boxLabel: CMDBuild.Translation.includeRelations,
								boxLabelCls: 'cmtoolbaritem',
								checked: false, // Default as false
								scope: this,

								handler: function(checkbox, checked) {
									this.delegate.cmfg('onHistoryIncludeRelationCheck');
								}
							})
						]
					})
				],
				columns: this.delegate.cmfg('getHistoryGridColumns'),
				store: this.delegate.cmfg('getHistoryGridStore')
			});

			this.callParent(arguments);
		},

		listeners: {
			viewready: function(view, eOpts) {
				this.getView().on('expandbody', function(rowNode, record, expandRow, eOpts) {
					this.doLayout(); // To refresh the scrollbar status and seems to fix also a glitch effect on row collapse

					this.delegate.cmfg('onHistoryRowExpand', record);
				}, this);
			}
		},

		reset: function() {
			this.getStore().removeAll();
		}
	});

})();