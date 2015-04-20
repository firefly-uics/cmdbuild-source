(function() {

	Ext.define('CMDBuild.controller.administration.email.Email', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Mixed}
		 */
		sectionController: undefined,

		/**
		 * @cfg {Array}
		 */
		subSections: [
			'accounts', // Default
			'queue',
			'templates'
		],

		/**
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

		/**
		 * @cfg {CMDBuild.view.administration.email.EmailView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.email.EmailView} view
		 *
		 * @override
		 */
		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange
			this.view = view;
			this.view.delegate = this;
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} parameters
		 *
		 * @override
		 */
		onViewOnFront: function(parameters) {
			if (!Ext.Object.isEmpty(parameters)) {
_debug('onViewOnFront parameters', parameters);
				var subSection = Ext.Array.contains(this.subSections, parameters.get(CMDBuild.core.proxy.CMProxyConstants.ID))
					? parameters.get(CMDBuild.core.proxy.CMProxyConstants.ID) : this.subSections[0];

				this.view.removeAll(true);

				switch(subSection) {
					case 'queue': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.email.Queue', { parentDelegate: this });
					} break;

					case 'templates': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.email.templates.Templates', { parentDelegate: this });
					} break;

					case 'accounts':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.email.CMEmailAccountsController', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(parameters.get(CMDBuild.core.proxy.CMProxyConstants.TEXT));

				if (!Ext.isEmpty(this.sectionController) && Ext.isFunction(this.sectionController.onViewOnFront))
					this.sectionController.onViewOnFront();

				this.callParent(arguments);
			}
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Setup view panel title as a breadcrumbs component
		 *
		 * @param {String} titlePart
		 */
		setViewTitle: function(titlePart) {
			if (!Ext.isEmpty(titlePart))
				this.view.setTitle(this.view.baseTitle + this.titleSeparator + titlePart);
		}
	});

})();