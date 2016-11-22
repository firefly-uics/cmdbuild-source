(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.connector.step3.Step3View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.connector.Step3}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.connector.step3.DatabaseFieldset}
		 */
		fieldsetDatabase: undefined,

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
					this.fieldsetDatabase = Ext.create('CMDBuild.view.administration.taskManager.task.connector.step3.DatabaseFieldset', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();
