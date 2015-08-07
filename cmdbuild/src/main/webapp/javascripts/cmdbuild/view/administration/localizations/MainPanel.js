(function() {

	Ext.define('CMDBuild.view.administration.localizations.MainPanel', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Main}
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