(function () {

	Ext.define('CMDBuild.view.management.workflow.panel.tree.TreePanel', {
		extend: 'CMDBuild.view.common.panel.gridAndForm.panel.tree.TreePanel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.management.workflow.panel.tree.Tree'
		],

		/**
		 * @cfg {CMDBuild.controller.management.workflow.panel.tree.Tree}
		 */
		delegate: undefined,

		columns: [],
		lines: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				store: CMDBuild.proxy.management.workflow.panel.tree.Tree.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			columnhide: function (ct, column, eOpts) {
				this.delegate.cmfg('onWorkflowTreeColumnChanged');
			},
			columnshow: function (ct, column, eOpts) {
				this.delegate.cmfg('onWorkflowTreeColumnChanged');
			},
			itemdblclick: function (grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onWorkflowFormActivityItemDoubleClick');
			},
			select: function (row, record, index) {
				this.delegate.cmfg('onWorkflowTreeRecordSelect', record);
			}
		}
	});

})();
