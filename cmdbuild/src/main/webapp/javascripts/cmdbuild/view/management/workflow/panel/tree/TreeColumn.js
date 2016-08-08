(function () {

	Ext.define('CMDBuild.view.management.workflow.panel.tree.TreeColumn', {
		extend: 'Ext.tree.Column',

		maxWidth: 20,
		menuDisabled: true,
		minWidth: 20,
		resizable: false,
		sortable: false,

		/**
		 * @param {Mixed} value
		 * @param {Object} metadata
		 * @param {CMDBuild.model.workflow.management.Node} record
		 * @param {Number} rowIndex
		 * @param {Number} colIndex
		 * @param {Ext.data.NodeStore} store
		 * @param {Ext.tree.View} view
		 *
		 * @returns {Mixed} value
		 *
		 * @override
		 */
		defaultRenderer: function (value, metadata, record, rowIndex, colIndex, store, view) {
			this.cmfg('workflowTreeRendererTreeColumn', {
				metadata: metadata,
				record: record
			});

			return value;
		}
	});

})();
