(function() {

	Ext.define('CMDBuild.view.administration.localizations.LocalizationsView', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Localizations}
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