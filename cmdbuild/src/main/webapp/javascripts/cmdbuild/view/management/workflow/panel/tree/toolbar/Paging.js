(function () {

	Ext.define('CMDBuild.view.management.workflow.panel.tree.toolbar.Paging', {
		extend: 'Ext.toolbar.Paging',

		/**
		 * @cfg {CMDBuild.controller.management.workflow.panel.tree.toolbar.Paging}
		 */
		delegate: undefined,

		/**
		 * @cfg {Ext.data.Store or Ext.data.TreeStore}
		 */
		store: undefined,

		dock: 'bottom',
		displayInfo: true,
		displayMsg: '{0} - {1} ' + CMDBuild.Translation.common.display_topic_of + ' {2}',
		emptyMsg: CMDBuild.Translation.common.display_topic_none,

		/**
		 * @param {Number} page
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		customLoadMethod: function (page) {
			return this.delegate.cmfg('panelGridAndFormGridStoreLoad', { page: page });
		},

		listeners: {
			show: function (toolbar, eOpts) {
				this.delegate.cmfg('onWorkflowTreeToolbarPagingShow');
			}
		}
	});

})();
