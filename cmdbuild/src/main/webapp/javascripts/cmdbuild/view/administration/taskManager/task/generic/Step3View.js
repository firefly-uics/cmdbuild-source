(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.generic.Step3View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.generic.Step3}
		 */
		delegate: undefined,

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
					Ext.create('CMDBuild.view.common.field.grid.KeyValue', {
						enableCellEditing: true,
						enableRowAdd: true,
						enableRowDelete: true,
						keyEditor: {
							xtype: 'textfield',
							vtype: 'alphanumlines'
						},
						name: CMDBuild.core.constants.Proxy.CONTEXT,
						title: CMDBuild.Translation.contextVariables
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
