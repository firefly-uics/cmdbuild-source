(function() {

	Ext.define('CMDBuild.view.administration.accordion.Task', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Task}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.administration.tasks.title
	});

})();