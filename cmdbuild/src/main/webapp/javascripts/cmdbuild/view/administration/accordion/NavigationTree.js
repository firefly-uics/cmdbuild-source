(function() {

	Ext.define('CMDBuild.view.administration.accordion.NavigationTree', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.common.accordion.NavigationTree'],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.NavigationTree}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.common.accordion.NavigationTree',

		title: CMDBuild.Translation.navigationTrees
	});

})();