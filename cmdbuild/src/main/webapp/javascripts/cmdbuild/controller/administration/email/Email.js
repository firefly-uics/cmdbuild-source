(function() {

	Ext.define('CMDBuild.controller.administration.email.Email', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Mixed}
		 */
		sectionController: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.email.EmailView}
		 */
		view: undefined,

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.accordion.Generic} node
		 *
		 * @override
		 */
		onViewOnFront: function(node) {
			if (!Ext.Object.isEmpty(node)) {
				this.view.removeAll(true);

				switch (node.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]) {
					case 'queue': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.email.Queue', { parentDelegate: this });
					} break;

					case 'templates': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.email.templates.Templates', { parentDelegate: this });
					} break;

					case 'accounts':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.email.Accounts', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.DESCRIPTION));

				if (!Ext.isEmpty(this.sectionController) && Ext.isFunction(this.sectionController.onViewOnFront))
					this.sectionController.onViewOnFront();

				this.callParent(arguments);
			}
		}
	});

})();