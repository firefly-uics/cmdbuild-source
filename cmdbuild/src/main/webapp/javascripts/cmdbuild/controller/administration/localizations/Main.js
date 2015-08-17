(function() {

	Ext.define('CMDBuild.controller.administration.localizations.Main', {
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
		 * @cfg {CMDBuild.view.administration.tasks.CMTasksForm}
		 */
		form: undefined,

		/**
		 * @property {Ext.selection.Model}
		 */
		selectionModel: undefined,

		/**
		 * @cfg {Array}
		 */
		subSections: [
			'baseTranslations', // Default
			'advancedTranslations',
			'advancedTranslationsTable'
		],

		/**
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

		/**
		 * @cfg {CMDBuild.view.administration.localizations.MainPanel}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.localizations.MainPanel} view
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
_debug('parameters', parameters);
				var subSection = Ext.Array.contains(this.subSections, parameters.get(CMDBuild.core.proxy.CMProxyConstants.ID))
					? parameters.get(CMDBuild.core.proxy.CMProxyConstants.ID) : this.subSections[0];

				this.view.removeAll(true);

				switch(subSection) {
					case 'advancedTranslations': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.localizations.AdvancedTranslations', {
							parentDelegate: this
						});
					} break;

					case 'advancedTranslationsTable': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.localizations.advancedTranslationsTable.Main', {
							parentDelegate: this
						});
					} break;

					case 'baseTranslations':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.localizations.BaseTranslations', {
							parentDelegate: this
						});
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(parameters.get(CMDBuild.core.proxy.CMProxyConstants.TEXT));

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