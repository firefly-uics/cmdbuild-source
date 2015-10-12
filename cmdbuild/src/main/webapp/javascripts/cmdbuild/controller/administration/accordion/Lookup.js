(function() {

	Ext.define('CMDBuild.controller.administration.accordion.Lookup', {
		extend: 'CMDBuild.controller.accordion.CMBaseAccordionController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.view.administration.accordion.Lookup}
		 */
		accordion: undefined,

		/**
		 * @property {Ext.data.Store}
		 */
		store: undefined,

		/**
		 * @param {CMDBuild.view.administration.accordion.Domain} accordion
		 */
		constructor: function(accordion) {
			this.callParent(arguments);

			this.store = this.accordion.getStore();

			_CMCache.on('cm_new_lookuptype', this.updateStore, this);
			_CMCache.on('cm_modified_lookuptype', this.updateStore, this);
		},

		/**
		 * @param {Object} domain
		 */
		updateStore: function(lookupType) {
			this.accordion.updateStore();
			this.accordion.selectNodeById(lookupType[CMDBuild.core.constants.Proxy.ID]);
		}
	});

})();