(function() {

	Ext.define('CMDBuild.controller.administration.localizations.AdvancedTranslationsTable', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localizations.Main} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel', {
				delegate: this
			});

			// GetAllClasses data to get default translations
			CMDBuild.core.proxy.Classes.read({
				params: {
					active: false
				},
				success: function(response, options, decodedResponse) {
_debug('decodedResponse', decodedResponse);

				}
			});

			// Build tabs
			CMDBuild.core.proxy.Localizations.getSectionsStore().each(function(record, id) { // TODO implementare la funzionalità con la creazione del pannello dinamica sull'onSuccess
				this.view.add(
					Ext.create('Ext.panel.Panel', {
						title: record.get(CMDBuild.core.proxy.CMProxyConstants.NAME),
						translationValue: record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE), // TODO "id" sezione traduzioni
						bodyCls: 'cmgraypanel',

						layout: 'fit',

						items: [ // TODO: dynamic setup of AdvancedTranslationsTableGrid columns and store
							Ext.create('CMDBuild.view.administration.localizations.panels.AdvancedTranslationsTableGrid', {
								sectionId: record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE) // TODO parametro di configurazione per indicare l'id della sezione
							})
						]
					})
				);
			}, this);

			this.view.setActiveTab(0);
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
				case 'onAdvancedTableAbortButtonClick':
					return this.onAdvancedTableAbortButtonClick();

				case 'onAdvancedTableSaveButtonClick':
					return this.onAdvancedTableSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return {CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel}
		 */
		getView: function() {
			return this.view;
		},

		onAdvancedTableAbortButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslationsTable ABORT');
		},

		onAdvancedTableSaveButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslationsTable SAVE');
		}
	});

})();