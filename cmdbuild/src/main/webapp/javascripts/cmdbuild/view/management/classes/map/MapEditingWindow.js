(function() {

	var ICON_CLS = {
		"POINT": "mapFeaturePoint",
		"LINESTRING": "mapFeatureLine",
		"POLYGON": "mapFeaturePolygon"
	};

	/**
	 * @class CMDBuild.Management.MapEditingWindow
	 * @extends Ext.Window
	 * 
	 * This window looks like an external toolbar (Photoshop style)
	 * After the creation is possible to say to the window to show the tools
	 * for a specific geometry type
	 * 
	 * The window can be shown only if the owner panel is visible
	 */	
	Ext.define("CMDBuild.Management.MapEditingWindow", {
		extend: "Ext.Window",
		editingControls: {},
		toMove: true, //to initialize the position of the window the first time that be shown	
		translation: CMDBuild.Translation,
		/**
		 * configuration options
		 */
		map: undefined,

		owner: {
			getPosition: function() { return [0,0]; }
		},

		initComponent: function() {
			var geoAttrMenu = new Ext.menu.Menu({
				items: []
			});

			this.cmButtons = [
				this.geoAttrMenuButton = new Ext.Button({
					text: this.translation.management.modcard.gis.geo_attributes,
					menu: geoAttrMenu
				}),

				this.addButton = new Ext.Button({
					text: this.translation.common.buttons.add,
					creationControl: undefined,
					iconCls: 'add',
					enableToggle: true,
					allowDepress: true,
					disabled: true,
					scope: this
				}),

				this.removeButton = new Ext.Button({
					text: this.translation.common.buttons.remove,
					iconCls: 'delete',
					scope: this,
					disabled: true
				})
			];

			Ext.apply(this, {
				overlapHeader: true,
				width: 300,
				closable: false,
				hideBorders: true,
				resizable: false,
				tbar: [this.geoAttrMenuButton, this.addButton, this.removeButton],
				layout: "hbox"
			});

			this.callParent(arguments);
		},

		show: function() {
			if (this.owner && this.owner.isVisible() 
					&& this.geoAttrMenuButton.menu.items.length > 0) {

				this.callParent(arguments);

				var firstItemOfMenu = this.geoAttrMenuButton.menu.items.first();
				if (firstItemOfMenu) {
					onAddMenuitemSelect.call(this, firstItemOfMenu);
				}

				moveToContainerOrigin.call(this);
				this.header.setHeight(0);
			}
		},
	
		update: function() {
			this.geoAttrMenuButton.menu.removeAll(true);
			var layers = this.map.cmdbLayers || [];
			for (var i=0, len=layers.length; i<len; i++) {
				var l = layers[i];
				if (l.editLayer) {
					this.geoAttrMenuButton.menu.add({
						iconCls: ICON_CLS[l.geoAttribute.type],
						text: l.geoAttribute.description,
						editLayer: l.editLayer,
						geoType: l.geoAttribute.type,
						scope: this,
						handler: onAddMenuitemSelect
					});
				}
			}
		}
	});

	function syncSize() {
		var size = 20;
		Ext.Array.each(this.cmButtons, function(button) {
			size += button.getWidth();
		});

		this.setWidth(size);
	};

	function onAddMenuitemSelect(item) {
//		this.map.controller.activateEditControls(item.editLayer);
		this.geoAttrMenuButton.setText(item.text);
		this.geoAttrMenuButton.setIconCls(item.iconCls);
		this.addButton.enable();
		this.addButton.toggle(false);
		this.removeButton.enable();

		syncSize.call(this);
		this.fireEvent("cmGeoAttrMenuClicked", item.editLayer);
	};

	function moveToContainerOrigin() {
		if (this.toMove) {
			var ownerPosition = this.owner.getPosition();
			var newX = ownerPosition[0]+30;
			var newY = ownerPosition[1]+18;
			this.setPosition(newX, newY);
			this.toMove = false;
		}
	};

})();