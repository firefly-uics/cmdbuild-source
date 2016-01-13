(function() {

	Ext.define('CMDBuild.view.management.dataView.sql.SqlView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.dataView.Sql}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.dataView.sql.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.dataView.sql.GridPanel}
		 */
		grid: undefined,

		/**
		 * @cfg {Boolean}
		 */
		whitMap: false,

		border: false,
		frame: false,
		header: false,
		layout: 'border',

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.grid = Ext.create('CMDBuild.view.management.dataView.sql.GridPanel', {
						delegate: this.delegate,
						region: 'center'
					}),
					this.form = Ext.create('CMDBuild.view.management.dataView.sql.FormPanel', {
						delegate: this.delegate,
						height: (CMDBuild.Config.cmdbuild.grid_card_ratio || 50) + '%',
						region: 'south',
						split: true
					})
				],
				tools: []
			});

			this.callParent(arguments);
		}
	});

})();