(function() {

	Ext.define('CMDBuild.controller.administration.accordion.Menu', {
		extend: 'CMDBuild.controller.accordion.CMBaseAccordionController',

		/**
		 * @property {Ext.data.Store}
		 */
		store: undefined,

		/**
		 * @param {CMDBuild.cache.CMGroupModel} group
		 */
		updateStore: function(group) {
			if (!Ext.isEmpty(group)) {
				this.accordion.onGroupAdded(group);
			} else {
				_warning('empty group object', this);
			}
		}
	});

})();