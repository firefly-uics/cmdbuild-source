(function () {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.MainPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		statics: {
			WIDGET_NAME: '.ManageEmail'
		},

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Main}
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
			this.grid = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.Grid', {
				readOnly: this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY],
				region: 'center'
			});

			Ext.apply(this, {
				items: [this.grid]
			});

			this.callParent(arguments);
		}
	});

})();