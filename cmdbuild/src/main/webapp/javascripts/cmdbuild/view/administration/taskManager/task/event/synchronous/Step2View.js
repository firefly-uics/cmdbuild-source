(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.event.synchronous.Step2View', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.event.synchronous.Step2}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.configurator.ConfiguratorView}
		 */
		fieldFilter: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,
		layout: 'fit',
		overflowY: 'auto',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.fieldFilter = Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.ConfiguratorView', {
						border: true,
						name: CMDBuild.core.constants.Proxy.FILTER
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
