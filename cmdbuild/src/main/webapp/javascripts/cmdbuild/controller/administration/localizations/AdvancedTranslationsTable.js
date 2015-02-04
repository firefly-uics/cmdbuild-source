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
//									{
//										xtype: 'treecolumn',
//										dataIndex: '@@ key',
//										text: '@@ key',
//										flex: 1
//									},
//									{
//										dataIndex: '@@ defaultTranslation',
//										header: '@@ defaultTranslation',
//										flex: 3
//									},
//									{
//										dataIndex: '@@ langTag1',
//										header: '@@ langTag1',
//										flex: 2
//									},
//									{
//										dataIndex: '@@ langTag2',
//										header: '@@ langTag2',
//										flex: 2
//									},
//									{
//										dataIndex: '@@ langTag3',
//										header: '@@ langTag3',
//										flex: 2
//									}
									{
											xtype: 'treecolumn', //this is so we know which column will show the tree
											text: 'Task',
											dataIndex: 'task',
											sortable: false,
											width: 300,
											locked: true // TODO
									},{
											//we must use the templateheader component so we can use a custom tpl
											xtype: 'templatecolumn',
											text: 'Duration',
											sortable: false,
											dataIndex: 'duration',
											align: 'center',
											width: 300,
											//add in the custom tpl for the rows
											tpl: Ext.create('Ext.XTemplate', '{duration:this.formatHours}', {
													formatHours: function(v) {
															if (v < 1) {
																	return Math.round(v * 60) + ' mins';
															} else if (Math.floor(v) !== v) {
																	var min = v - Math.floor(v);
																	return Math.floor(v) + 'h ' + Math.round(min * 60) + 'm';
															} else {
																	return v + ' hour' + (v === 1 ? '' : 's');
															}
													}
											})
									},{
											text: 'Assigned To',
											dataIndex: 'user',
											width: 300,
											sortable: false
									},
									{
										dataIndex: '@@ defaultTranslation',
										header: '@@ defaultTranslation',
										width: 300,
									},
									{
										dataIndex: '@@ langTag1',
										header: '@@ langTag1',
										width: 300,
									},
									{
										dataIndex: '@@ langTag2',
										header: '@@ langTag2',
										width: 300,
									},
									{
										dataIndex: '@@ langTag3',
										header: '@@ langTag3',
										width: 300,
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

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
	});

})();
