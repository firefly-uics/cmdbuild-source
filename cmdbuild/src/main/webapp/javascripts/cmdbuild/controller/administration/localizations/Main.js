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
		 * @property {CMDBuild.controller.administration.localizations.AdvancedTranslations}
		 */
		advancedTranslationsController: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localizations.AdvancedTranslationsGrid}
		 */
		advancedTranslationsGridController: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localizations.BaseTranslations}
		 */
		baseTranslationsController: undefined,

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
			'advancedTranslationsGrid'
		],

		/**
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

		/**
		 * @cfg {CMDBuild.view.administration.localizations.Form}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.localizations.Form} view
		 *
		 * @override
		 */
		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange and controller setup
			this.view = view;
			this.view.delegate = this;
_debug(this.view);
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
				var formViewItem = undefined;
				var subSection = Ext.Array.contains(this.subSections, parameters.get(CMDBuild.core.proxy.CMProxyConstants.ID))
					? parameters.get(CMDBuild.core.proxy.CMProxyConstants.ID) : this.subSections[0];

				this.view.wrapper.removeAll(true);

				switch(subSection) {
					case 'advancedTranslations': {
						formViewItem = Ext.create('CMDBuild.view.administration.localizations.AdvancedTranslationsPanel');

						this.advancedTranslationsController = Ext.create('CMDBuild.controller.administration.localizations.AdvancedTranslations', formViewItem);
						this.advancedTranslationsController.parentDelegate = this;
						this.advancedTranslationsController.onViewOnFront();
					} break;

					case 'advancedTranslationsGrid': {
						formViewItem = Ext.create('CMDBuild.view.administration.localizations.AdvancedTranslationsGridPanel');

						this.advancedTranslationsGridController = Ext.create('CMDBuild.controller.administration.localizations.AdvancedTranslationsGrid', formViewItem);
						this.advancedTranslationsGridController.parentDelegate = this;
						this.advancedTranslationsGridController.onViewOnFront();
					} break;

					case 'baseTranslations':
					default: {
						formViewItem = Ext.create('CMDBuild.view.administration.localizations.BaseTranslationsPanel');

						this.baseTranslationsController = Ext.create('CMDBuild.controller.administration.localizations.BaseTranslations', formViewItem);
						this.baseTranslationsController.parentDelegate = this;
						this.baseTranslationsController.onViewOnFront();
					}
				}

				this.view.wrapper.add(formViewItem);

_debug('this.view.wrapper', this.view.wrapper);
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
//				case 'onAddButtonClick':
//					return this.onAddButtonClick(name, param, callBack);

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