(function() {

	Ext.define('CMDBuild.controller.administration.localizations.AdvancedTranslationsTable', {
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
		 * @cfg {CMDBuild.view.administration.localizations.AdvancedTranslationsTablePanel}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.localizations.AdvancedTranslationsPanel} view
		 *
		 * @override
		 */
		constructor: function(view) {
			var me = this;

			this.callParent(arguments);

			// Handlers exchange and controller setup
			this.view = view;
			this.view.delegate = this;

			// Build tabs
			CMDBuild.core.proxy.Localizations.getSectionsStore().each(function(record, id) {
				this.view.add(
					Ext.create('Ext.panel.Panel', {
						title: record.get(CMDBuild.core.proxy.CMProxyConstants.NAME),
						translationValue: record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE), // TODO
						bodyCls: 'cmgraypanel',

						layout: 'fit',

						// TODO: dynamic setup of AdvancedTranslationsTableGrid columns and store
						items: [
							Ext.create('CMDBuild.view.administration.localizations.panels.AdvancedTranslationsTableGrid', {
								columns: [
									{
										xtype: 'treecolumn',
										text: '@@ Translation object',
										dataIndex: 'task',
										width: 300,
										locked: true, // TODO
										sortable: false
									},
									{
										text: '@@ Default',
										dataIndex: 'duration',
										width: 300,
										sortable: false
									},
									{
										text: 'Assigned To',
										dataIndex: 'user',
										width: 300,
										sortable: false
									},
									{
										text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/en.png" alt="Language icon" /> @@ defaultTranslation',
										dataIndex: '@@ defaultTranslation',
										width: 300,
										sortable: false
									},
									{
										text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/en.png" alt="Language icon" /> @@ langTag1',
										dataIndex: '@@ langTag1',
										width: 300,
										sortable: false
									},
									{
										text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/en.png" alt="Language icon" /> @@ langTag2',
										dataIndex: '@@ langTag2',
										width: 300,
										sortable: false
									},
									{
										text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/en.png" alt="Language icon" /> @@ langTag3',
										dataIndex: '@@ langTag3',
										width: 300,
										sortable: false
									}
								],
								store: CMDBuild.core.proxy.Localizations.getSectionTranslationsStore()
							})
						]
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
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();

				case 'onSaveButtonClick':
					return this.onSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onAbortButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslationsTable ABORT');
		},

		onSaveButtonClick: function() {
_debug('CMDBuild.controller.administration.localizations.AdvancedTranslationsTable SAVE');
		}
	});

})();
