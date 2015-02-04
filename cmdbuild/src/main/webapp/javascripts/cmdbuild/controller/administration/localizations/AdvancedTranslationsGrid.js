(function() {

	Ext.define('CMDBuild.controller.administration.localizations.AdvancedTranslationsGrid', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.localizations.AdvancedTranslationsGridPanel}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.localizations.AdvancedTranslationsPanel} view
		 *
		 * @override
		 */
		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange and controller setup
			this.view = view;
			this.view.delegate = this;

			// Build tabs
			CMDBuild.core.proxy.Localizations.getSectionsStore().each(function(record, id) {
				this.view.add(
					Ext.create('Ext.tab.Tab', {
						title: record.get(CMDBuild.core.proxy.CMProxyConstants.NAME),
						sectionValue: record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE), // TODO
						bodyCls: 'cmgraypanel',
						closable: false
					})
				);
			}, this);

			this.view.setActiveTab(0);
		},

		/**
		 * Parent controller/view setup
		 *
		 * @override
		 */
		onViewOnFront: function() {
			this.parentDelegate.view.delegate = this;
			this.parentDelegate.setViewTitle('@@ Base translations table');

			this.callParent(arguments);
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
		}
	});

})();