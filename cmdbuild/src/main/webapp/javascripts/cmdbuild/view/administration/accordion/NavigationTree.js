(function () {

	Ext.define('CMDBuild.view.administration.accordion.NavigationTree', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.administration.navigationTree.Accordion'],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.NavigationTree}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.administration.navigationTree.Accordion',

		title: CMDBuild.Translation.navigationTrees
	});

})();
