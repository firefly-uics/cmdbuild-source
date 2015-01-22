(function () {

	Ext.define('CMDBuild.view.management.common.widgets.CMManageEmail', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		statics: {
			WIDGET_NAME: '.ManageEmail'
		},

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.CMManageEmailController}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.CMEmailGrid}
		 */
		emailGrid: undefined,

		/**
		 * @property {Object}
		 */
		widgetConf: undefined,

		border: false,
		cls: 'x-panel-body-default-framed',
		frame: false,
		layout: 'border',

		initComponent: function() {
			this.emailGrid = Ext.create('CMDBuild.view.management.common.widgets.CMEmailGrid', {
				readOnly: this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY],
				region: 'center'
			});

			Ext.apply(this, {
				items: [this.emailGrid]
			});

			this.callParent(arguments);
		}
	});

})();