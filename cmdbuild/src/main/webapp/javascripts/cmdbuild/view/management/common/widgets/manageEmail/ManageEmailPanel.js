(function () {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.ManageEmailPanel', {
		extend: 'Ext.panel.Panel',

//		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		statics: {
			WIDGET_NAME: '.ManageEmail'
		},

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.widgets.manageEmail.Grid}
		 */
		grid: undefined,

		/**
		 * @cfg {Object}
		 */
		widgetConf: undefined,

		border: false,
		cls: 'x-panel-body-default-framed',
		frame: false,
		layout: 'border',

		initComponent: function() {
			Ext.apply(this, {
//				items: [this.grid]
			});

			this.callParent(arguments);
		}
	});

})();