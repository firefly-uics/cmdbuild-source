(function () {

	Ext.define('CMDBuild.view.management.utility.UtilityView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.utility.Utility}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.utility,

		bodyCls: 'cmdb-blue-panel-no-padding',
		border: true,
		frame: false,
		layout: 'fit',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				tools: [
					Ext.create('CMDBuild.controller.common.panel.gridAndForm.tools.properties.Properties', { withSpacer: false }).getView()
				]
			});

			this.callParent(arguments);
		}
	});

})();
