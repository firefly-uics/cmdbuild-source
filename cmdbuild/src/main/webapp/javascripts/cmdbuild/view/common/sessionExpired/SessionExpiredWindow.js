(function () {

	Ext.define('CMDBuild.view.common.sessionExpired.SessionExpiredWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		/**
		 * @cfg {CMDBuild.view.common.sessionExpired.FormPanel}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		border: false,
		dimensionsMode: 'absolute',
		frame: false,
		title: CMDBuild.Translation.sessionExpired,

		dimensions: {
			height: 155,
			width: 300
		},

		/**
		 * @returns {Void}
		 *
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
