(function() {

	Ext.define('CMDBuild.view.administration.dataView.DataViewsView', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.views,

		bodyCls: 'cmgraypanel-nopadding',
		border: true,
		frame: false,
		layout: 'fit'
	});

})();