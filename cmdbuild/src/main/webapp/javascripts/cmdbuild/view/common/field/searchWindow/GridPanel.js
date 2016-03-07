(function() {

	Ext.define('CMDBuild.view.common.field.searchWindow.GridPanel', {
		extend: 'CMDBuild.view.management.common.CMCardGrid',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.field.searchWindow.SearchWindow}
		 */
		delegate: undefined,

		/**
		 * @cfg {Boolean}
		 */
		cmVisible: true,

		/**
		 * @cfg {String}
		 */
		CQL: undefined,

		/**
		 * @cfg {Boolean}
		 */

		cmAddGraphColumn: false,

		/**
		 * @cfg {Boolean}
		 */

		cmAddPrintButton: false,

		/**
		 * @cfg {Boolean}
		 */
		cmAdvancedFilter: false,

		/**
		 * @cfg {Object}
		 */
		selModel: undefined,

		border: false,
		columns: [],
		frame: false,

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onFieldSearchWindowItemDoubleClick');
			},

			selectionchange: function(selectionModel, selected, eOpts) {
				this.delegate.cmfg('onFieldSearchWindowSelectionChange');
			}
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		getStoreExtraParams: function() {
			return Ext.isEmpty(this.delegate.cmfg('fiedlGetStore')) ? null : this.delegate.cmfg('fiedlGetStore').getProxy().extraParams;
		}
	});

})();