(function() {

	Ext.define('CMDBuild.view.management.common.widgets.linkCards.LinkCards', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {Object}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.Reload}
		 */
		applyDefaultSelectionButton: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.linkCards.LinkCardsGrid}
		 */
		grid: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		mapButton: undefined,

		/**
		 * @property {CMDBuild.view.management.classes.map.CMMapPanel}
		 */
		mapPanel: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		toggleGridFilterButton: undefined,

		/**
		 * @cfg {Object}
		 */
		widgetConf: undefined,

		hideMode: 'offsets',
		border: false,
		frame: false,

		layout: 'border',

		initComponent: function() {
			var allowEditCard = false;
			var allowShowCard = false;

			if (this.widgetConf[CMDBuild.core.constants.Proxy.ALLOW_CARD_EDITING]) {
				var priv = CMDBuild.core.Utils.getEntryTypePrivilegesByName(this.widgetConf[CMDBuild.core.constants.Proxy.CLASS_NAME]);

				if (priv && priv.write) {
					allowEditCard = true;
				} else {
					allowShowCard = true;
				}
			}

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
//							this.toggleGridFilterButton = Ext.create('Ext.button.Button', { // TODO: build toggle/cycle button class
//								text: CMDBuild.Translation.disableGridFilter,
//								iconCls: 'clear_filter',
//								filterEnabled: true, // FilterEnabled (true/false) used to mark state grid's filter
//								scope: this,
//
//								handler: function(button, e) {
//									this.delegate.cmOn('onToggleGridFilterButtonClick');
//								}
//							}),
							this.toggleGridFilterButton = Ext.create('CMDBuild.core.buttons.iconized.state.Double', {
								state1text: CMDBuild.Translation.disableGridFilter,
								state1icon: 'clear_filter',
//								state1stateId: true, // TODO: implementation of enable/disable
								state2text: CMDBuild.Translation.enableGridFilter,
								state2icon: 'find',
//								state1stateId: false, // TODO: implementation of enable/disable
								scope: this,

								handler: function(button, e) {
									this.delegate.cmOn('onToggleGridFilterButtonClick', button.getActiveState());
								}
							}),
							this.applyDefaultSelectionButton = Ext.create('CMDBuild.core.buttons.iconized.Reload', {
								text: CMDBuild.Translation.applyDefaultSelection,
								disabled: Ext.isEmpty(this.widgetConf[CMDBuild.core.constants.Proxy.DEFAULT_SELECTION]),
								scope: this,

								handler: function(button, e) {
									this.delegate.cmOn('onLinkCardApplyDefaultSelectionButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.management.common.widgets.linkCards.LinkCardsGrid', {
						autoScroll: true,
						selModel: this.getSelectionModel(),
						hideMode: 'offsets',
						region: 'center',
						border: false,
						cmAllowEditCard: allowEditCard,
						cmAllowShowCard: allowShowCard
					})
				]
			});

			if (this.widgetConf[CMDBuild.core.constants.Proxy.ENABLE_MAP] && CMDBuild.configuration.gis.get(CMDBuild.core.constants.Proxy.ENABLED))
				this.buildMap();

			this.callParent(arguments);

			// To listener to select right cards on pageChange
			this.grid.pagingBar.on('change', function(pagingBar, options) {
				this.delegate.cmOn('onGridPageChange');
			}, this);
		},

		buildMap: function() {
			this.mapButton = Ext.create('Ext.button.Button', {
				text: CMDBuild.Translation.management.modcard.tabs.map,
				iconCls: 'map',
				scope: this,

				handler: function(button, e) {
					this.delegate.cmOn('onToggleMapButtonClick');
				}
			});

			this.mapPanel = Ext.create('CMDBuild.view.management.classes.map.CMMapPanel', {
				frame: false,
				border: false,

				lon: this.widgetConf['StartMapWithLongitude'] || this.widgetConf[CMDBuild.core.constants.Proxy.MAP_LONGITUDE],
				lat: this.widgetConf['StartMapWithLatitude'] || this.widgetConf[CMDBuild.core.constants.Proxy.MAP_LATITATUDE],
				initialZoomLevel: this.widgetConf['StartMapWithZoom'] || this.widgetConf[CMDBuild.core.constants.Proxy.MAP_ZOOM]
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [this.toggleGridFilterButton, '->', this.mapButton]
					})
				],
				layout: 'card',
				items: [this.grid, this.mapPanel]
			});

			// Function definitions
				this.showMap = function() {
					this.layout.setActiveItem(this.mapPanel.id);

					this.mapPanel.updateSize();

					this.mapPanel.setCmVisible(true);
					this.grid.setCmVisible(false);
				};

				this.showGrid = function() {
					this.layout.setActiveItem(this.grid.id);

					this.grid.setCmVisible(true);
					this.mapPanel.setCmVisible(false);
				};

				this.getMapPanel = function() {
					return this.mapPanel;
				};
		},

		/**
		 * @return {Ext.selection.RowModel}
		 * @return {CMDBuild.selection.CMMultiPageSelectionModel} single select or multi select
		 */
		getSelectionModel: function() {
			if (this.widgetConf[CMDBuild.core.constants.Proxy.READ_ONLY])
				return Ext.create('Ext.selection.RowModel');

			return Ext.create('CMDBuild.selection.CMMultiPageSelectionModel', {
				avoidCheckerHeader: true,
				mode: this.widgetConf[CMDBuild.core.constants.Proxy.SINGLE_SELECT] ? 'SINGLE' : 'MULTI',
				idProperty: 'Id' // Required to identify the records for the data and not the id of ext
			});
		},

		/**
		 * @return {Boolean}
		 */
		hasMap: function() {
			return !Ext.isEmpty(this.mapPanel);
		}
	});

})();