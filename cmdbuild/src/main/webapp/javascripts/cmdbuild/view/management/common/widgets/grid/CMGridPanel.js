(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.CMGridPanel', {
		extend: 'Ext.grid.Panel',

		border: false,

		/**
		 * @cfg {Array}
		 */
		columns: [],

		initComponent: function() {
			Ext.apply(this, {
				plugins: [
					Ext.create('Ext.grid.plugin.CellEditing', {
						clicksToEdit: 1
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();