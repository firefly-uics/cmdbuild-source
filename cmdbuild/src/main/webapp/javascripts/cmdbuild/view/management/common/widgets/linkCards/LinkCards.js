(function() {

	Ext.define('CMDBuild.view.management.common.widgets.linkCards.LinkCards', {
		extend: 'Ext.panel.Panel',

		statics: {
			WIDGET_NAME: '.LinkCards'
		},

		delegate: undefined,
		widget: undefined,

		hideMode: 'offsets',
		border: false,
		frame: false,
		cls: 'x-panel-body-default-framed',

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

			this.grid = Ext.create('CMDBuild.view.management.common.widgets.linkCards.LinkCardsGrid', {
				autoScroll: true,
				selModel: this.selectionModelFromConfiguration(),
				readOnly: this.widget.readOnly,
				hideMode: 'offsets',
				region: 'center',
				border: false,
				filterSubcategory : this.widget.id,
				cmAllowEditCard: allowEditCard,
				cmAllowShowCard: allowShowCard
			});

			this.selectionModel = this.grid.getSelectionModel();

			Ext.apply(this, {
				items: [this.grid]
			});

			if (this.widget.enableMap && CMDBuild.Config.gis.enabled)
				this.buildMap();

			this.callParent(arguments);
		},

		buildMap: function() {
			this.mapButton = Ext.create('Ext.button.Button', {
				text: CMDBuild.Translation.management.modcard.tabs.map,
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
						items: ['->', this.mapButton]
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
		 * @return {Boolean}
		 */
		hasMap: function() {
			return this.mapPanel != undefined;
		},

		/**
		 * @return {Ext.selection.RowModel}
		 * @return {CMDBuild.selection.CMMultiPageSelectionModel} single select or multi select
		 */
		selectionModelFromConfiguration: function() {
			if (this.widget.readOnly) {
				return Ext.create('Ext.selection.RowModel');
			}

			if (this.widget.singleSelect) {
				return Ext.create('CMDBuild.selection.CMMultiPageSelectionModel', {
					mode: 'SINGLE',
					idProperty: 'Id' // Required to identify the records for the data and not the id of ext
				});
			}

			return Ext.create('CMDBuild.selection.CMMultiPageSelectionModel', {
				mode: 'MULTI',
				avoidCheckerHeader: true,
				idProperty: 'Id' // Required to identify the records for the data and not the id of ext
			});
		}
	});

})();