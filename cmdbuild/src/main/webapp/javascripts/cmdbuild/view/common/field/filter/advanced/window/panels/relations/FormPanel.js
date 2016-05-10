(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.panels.relations.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.panels.relations.Relations}
		 */
		delegate: undefined,

		bodyCls: 'x-panel-default-framed',
		border: false,
		frame: false,
		overflowY: 'auto',
		split: true,
		items: [],

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {

			});

			this.callParent(arguments);
		}
	});

})();