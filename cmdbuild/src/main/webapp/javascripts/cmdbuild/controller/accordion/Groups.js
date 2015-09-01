(function() {

	Ext.define('CMDBuild.controller.accordion.Groups', {
		extend: 'CMDBuild.controller.accordion.CMBaseAccordionController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @param {CMDBuild.view.administration.accordion.Groups} accordion
		 */
		constructor: function(accordion) {
			this.store = accordion.store;
			this.callParent(arguments);

			_CMCache.on('cm_group_saved', this.updateStore, this);
		},

		/**
		 * @param {CMDBuild.cache.CMGroupModel} group
		 */
		updateStore: function(group) {
			this.accordion.updateStore();
			this.accordion.deselect();
			this.accordion.selectNodeById(group.get(CMDBuild.core.proxy.CMProxyConstants.ID));
		}
	});

})();