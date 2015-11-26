(function() {

	Ext.define('CMDBuild.view.administration.email.EmailView', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.email.Email}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.email,

		bodyCls: 'cmgraypanel-nopadding',
		border: true,
		frame: false,
		layout: 'fit'
	});

})();