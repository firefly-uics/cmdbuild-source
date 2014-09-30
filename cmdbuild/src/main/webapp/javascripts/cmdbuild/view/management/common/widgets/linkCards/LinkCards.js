(function() {

	var tr = CMDBuild.Translation;

	Ext.define('CMDBuild.view.management.common.widgets.linkCards.LinkCards', {
		extend: 'Ext.panel.Panel',

		statics: {
			WIDGET_NAME: '.LinkCards'
		},

		delegate: undefined,

		grid: undefined,
		mapButton: undefined,
		mapPanel: undefined,
		selectionModel: undefined,
		toggleGridFilterButton: undefined,
		widgetConf: undefined,

		hideMode: 'offsets',
		border: false,
		frame: false,

		layout: {
			type: 'border'
		},

		initComponent: function() {
			var allowEditCard = false;
			var allowShowCard = false;

			if (this.widgetConf.allowCardEditing) {
				var priv = _CMUtils.getClassPrivilegesByName(this.widgetConf.className);

				if (priv && priv.write) {
					allowEditCard = true;
				} else {
					allowShowCard = true;
				}
			}

			this.toggleGridFilterButton = Ext.create('Ext.button.Button', {
				text: tr.disableFilter,
				iconCls: 'clear_filter',
				scope: this,
				filterEnabled: true, // FilterEnabled (true/false) used to mark state grid's filter

				handler: function() {
					this.delegate.cmOn('onToggleGridFilterButtonClick');
				}
			});

			this.grid = Ext.create('CMDBuild.view.management.common.widgets.linkCards.LinkCardsGrid', {
				autoScroll: true,
				selModel: this.getSelectionModel(),
				hideMode: 'offsets',
				region: 'center',
				border: false,
				cmAllowEditCard: allowEditCard,
				cmAllowShowCard: allowShowCard
			});

			this.selectionModel = this.grid.getSelectionModel();

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [this.toggleGridFilterButton]
					}
				],
				items: [this.grid]
			});

			if (this.widgetConf.enableMap && CMDBuild.Config.gis.enabled)
				this.buildMap();

			this.callParent(arguments);
		},

		buildMap: function() {
			this.mapButton = Ext.create('Ext.button.Button', {
				text: tr.management.modcard.tabs.map,
				iconCls: 'map',
				scope: this,

				handler: function() {
					this.delegate.cmOn('onToggleMapButtonClick');
				}
			});

			this.mapPanel = Ext.create('CMDBuild.view.management.classes.map.CMMapPanel', {
				frame: false,
				border: false,

				lon: this.widgetConf.StartMapWithLongitude || this.widgetConf.mapLongitude,
				lat: this.widgetConf.StartMapWithLatitude || this.widgetConf.mapLatitude,
				initialZoomLevel: this.widgetConf.StartMapWithZoom || this.widgetConf.mapZoom
			});

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [this.toggleGridFilterButton, '->', this.mapButton]
					}
				],
				layout: {
					type: 'card'
				},
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
			if (this.widgetConf.readOnly)
				return Ext.create('Ext.selection.RowModel');

			return Ext.create('Ext.selection.CheckboxModel', {
				mode: this.widgetConf.singleSelect ? 'SINGLE' : 'MULTI',
				showHeaderCheckbox: false
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