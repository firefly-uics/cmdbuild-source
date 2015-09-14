(function() {

	Ext.define('CMDBuild.view.administration.localization.LocalizationView', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localization.Localization}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: '@@ Localizations',

		bodyCls: 'cmgraypanel',
		border: true,
		frame: false,
		layout: 'fit'
	});

})();