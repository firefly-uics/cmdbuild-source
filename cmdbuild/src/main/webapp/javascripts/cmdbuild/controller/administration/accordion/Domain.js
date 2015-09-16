(function() {

	Ext.define('CMDBuild.controller.administration.accordion.Domain', {
		extend: 'CMDBuild.controller.accordion.CMBaseAccordionController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.view.administration.accordion.Domain}
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

			_CMCache.on('cm_domain_saved', this.updateStore, this);
			_CMCache.on('cm_domain_deleted', this.onDomainDeleted, this);
		},

		expandForAdd: function() {
			this.accordion.expandSilently();

			_CMMainViewportController.bringTofrontPanelByCmName(this.accordion.cmName);
			_CMMainViewportController.panelControllers['domain'].cmfg('onDomainAddButtonClick');
		},

		/**
		 * @param {String} id
		 *
		 * TODO: should be Numeric format
		 */
		onDomainDeleted: function(id) {
			if (!Ext.isEmpty(id)) {
				this.accordion.removeNodeById(id);
				this.accordion.selectFirstSelectableNode();
			} else {
				_warning('Cannot delete domain, ID empty', this);
			}
		},

		/**
		 * @param {CMDBuild.cache.CMDomainModel} domain
		 */
		updateStore: function(domain) {
			if (!Ext.isEmpty(domain)) {
				this.accordion.updateStore();
				this.accordion.selectNodeById(domain.get(CMDBuild.core.constants.Proxy.ID));
			} else {
				_warning('Cannot select domain, object is empty', this);
			}
		}
	});

})();