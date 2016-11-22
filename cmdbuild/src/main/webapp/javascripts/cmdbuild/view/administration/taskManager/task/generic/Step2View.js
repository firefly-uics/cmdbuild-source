(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.generic.Step2View', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.generic.Step2}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.common.field.cron.CronView}
		 */
		fieldCronExpression: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.fieldCronExpression = Ext.create('CMDBuild.view.administration.taskManager.task.common.field.cron.CronView', {
						name: CMDBuild.core.constants.Proxy.CRON_EXPRESSION
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (view, eOpts) {
				this.delegate.cmfg('onTaskManagerFormTaskGenericStep2Show');
			}
		}
	});

})();
