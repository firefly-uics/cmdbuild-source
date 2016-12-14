(function () {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.view.common.panel.gridAndForm.GridAndFormView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.GridAndForm}
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
				tools: this.delegate.cmfg('panelGridAndFormToolsArrayBuild')
			});

			this.callParent(arguments);
		}
	});

})();
