(function() {

	Ext.define('CMDBuild.view.administration.localizations.MainPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants'
		],

		/**
		 * @cfg {Mixed}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: '@@ Localizations',

		/**
		 * @property {Ext.panel.Panel}
		 */
		wrapper: undefined,

		bodyCls: 'cmgraypanel',
		border: true,
		frame: false,
		layout: 'border',

		initComponent: function() {
			// Buttons configuration
				this.cmButtons = [
					Ext.create('CMDBuild.buttons.SaveButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onSaveButtonClick');
						}
					}),
					Ext.create('CMDBuild.buttons.AbortButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onAbortButtonClick');
						}
					})
				];
			// END: Buttons configuration

			// Wrapper
			this.wrapper = Ext.create('Ext.panel.Panel', {
				region: 'center',
				border: false,
				buttonAlign: 'center',
				buttons: this.cmButtons,
				frame: false,

				layout: 'fit'
			});

			Ext.apply(this, {
				items: [this.wrapper]
			});

			this.callParent(arguments);
		}
	});

})();