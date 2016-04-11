(function () {

	Ext.define('CMDBuild.view.common.sessionExpired.SessionExpiredWindow', {
		extend: 'Ext.window.Window',

		/**
		 * @cfg {CMDBuild.view.common.sessionExpired.FormPanel}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		height: 155,
		layout: 'fit',
		modal: true,
		title: CMDBuild.Translation.sessionExpired,
		width: 300,

		/**
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.common.sessionExpired.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();
