(function() {

	Ext.define('CMDBuild.view.administration.accordion.Lookup', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.common.accordion.Lookup'],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Lookup}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.common.accordion.Lookup',

		title: CMDBuild.Translation.lookupTypes
	});

})();