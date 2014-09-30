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
		widget: undefined,

		hideMode: 'offsets',
		border: false,
		frame: false,

		layout: {
			type: 'border'
		},

		initComponent: function() {
			var allowEditCard = false;
			var allowShowCard = false;

			if (this.widget.allowCardEditing) {
				var priv = _CMUtils.getClassPrivilegesByName(this.widget.className);

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

			if (this.widget.enableMap && CMDBuild.Config.gis.enabled)
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

				lon: this.widget.StartMapWithLongitude || this.widget.mapLongitude,
				lat: this.widget.StartMapWithLatitude || this.widget.mapLatitude,
				initialZoomLevel: this.widget.StartMapWithZoom || this.widget.mapZoom
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
			if (this.widget.readOnly)
				return Ext.create('Ext.selection.RowModel');

			return Ext.create('Ext.selection.CheckboxModel', {
				mode: this.widget.singleSelect ? 'SINGLE' : 'MULTI',
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