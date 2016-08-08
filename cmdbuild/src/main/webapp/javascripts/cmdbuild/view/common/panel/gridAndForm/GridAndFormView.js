(function () {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.view.common.panel.gridAndForm.GridAndFormView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.panel.grid.GridAndForm}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-blue-panel-no-padding',
		border: true,
		frame: false,
		layout: 'border',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				tools: [
					Ext.create('CMDBuild.view.common.panel.gridAndForm.tools.Properties'),
					Ext.create('CMDBuild.view.common.panel.gridAndForm.tools.Minimize'),
					Ext.create('CMDBuild.view.common.panel.gridAndForm.tools.Maximize'),
					Ext.create('CMDBuild.view.common.panel.gridAndForm.tools.Restore')
				]
			});

			this.callParent(arguments);
		}
	});

})();
