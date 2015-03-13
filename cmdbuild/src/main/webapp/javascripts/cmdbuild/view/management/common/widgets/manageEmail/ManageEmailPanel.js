(function () {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.ManageEmailPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

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
			this.grid = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.GridPanel', {
				readOnly: this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY],
				region: 'center'
			});

			Ext.apply(this, {
				items: [this.grid]
			});

			this.callParent(arguments);
		},

		/**
		 * Extra buttons to add on widget window
		 *
		 * @return {Array}
		 */
		getExtraButtons: function() {
// TODO: future implementation
//			return [
//				Ext.create('Ext.button.Button', {
//					text: '@@ Send all',
//					scope: this,
//
//					handler: function() {
//						this.delegate.cmOn('onSendAllButtonClick');
//					}
//				})
//			];
		}
	});

})();