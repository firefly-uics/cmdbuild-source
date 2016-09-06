(function () {

	Ext.define('CMDBuild.view.management.workflow.panel.tree.toolbar.TopView', {
		extend: 'Ext.toolbar.Toolbar',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.workflow.panel.tree.toolbar.Top}
		 */
		delegate: undefined,

		dock: 'top',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,
			});

			this.callParent(arguments);
		}
	});

})();
