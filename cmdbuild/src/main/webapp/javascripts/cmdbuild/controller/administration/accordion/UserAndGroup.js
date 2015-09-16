(function() {

	Ext.define('CMDBuild.controller.administration.accordion.UserAndGroup', {
		extend: 'CMDBuild.controller.accordion.CMBaseAccordionController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onAccordionExpanded',
			'onAccordionNodeSelect'
		],

		/**
		 * @param {CMDBuild.view.administration.accordion.UserAndGroup} accordion
		 */
		constructor: function(accordion) {
			this.callParent(arguments);

			_CMCache.on('cm_group_saved', this.updateStore, this); // TODO: refactor to avoid cache usage
		},

		/**
		 * @param {CMDBuild.cache.CMGroupModel} group
		 */
		updateStore: function(group) {
			this.accordion.updateStore();
			this.accordion.deselect();
			this.accordion.selectNodeById(group.get(CMDBuild.core.constants.Proxy.ID));
		}
	});

})();