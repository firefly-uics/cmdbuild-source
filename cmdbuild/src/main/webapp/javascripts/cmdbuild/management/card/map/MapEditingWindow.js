(function() {
	
function syncSize() {
	var size = 20;
	this.topToolbar.items.each(function() {
		size += this.getWidth();
	});
	this.setWidth(size);
};

var itemHandler = function(item) {
	this.map.controller.activateEditControls(item.editLayer);
	this.geoAttrMenuButton.setText(item.text);
	this.geoAttrMenuButton.setIconClass(item.iconCls);
	this.addButton.enable();
	this.addButton.toggle(false);
	this.removeButton.enable();
	syncSize.call(this);	
};

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
CMDBuild.Management.MapEditingWindow = Ext.extend(Ext.Window, {
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

		this.geoAttrMenuButton = new Ext.Button({
			text: this.translation.management.modcard.gis.geo_attributes,
			menu: geoAttrMenu
		});
		
		this.addButton = new Ext.Button({
			text: this.translation.common.buttons.add,
			creationControl: undefined,
			iconCls: 'add',
			enableToggle: true,
			allowDepress: true,
			disabled: true,
			scope: this,
			toggleHandler: function(button, state) {
				this.map.controller.activateCreationControl(state);		
			}
		});
		
		this.removeButton = new Ext.Button({
			text: this.translation.common.buttons.remove,			
			iconCls: 'delete',
			scope: this,
			disabled: true,
			handler: function() {
				this.map.controller.removeCurrentEditFeature();
			}
		});
		
		Ext.apply(this, {
			autoHeight: true,
			width: 300,
			closable: false,
			hideBorders: true,
			resizable: false,
			items: [],
			tbar: [this.geoAttrMenuButton, this.addButton, this.removeButton]
		});
		CMDBuild.Management.MapEditingWindow.superclass.initComponent.apply(this, arguments);
		
		var moveToContainerOrigin = function() {
			if (this.toMove) {
				var ownerPosition = this.owner.getPosition();
				var newX = ownerPosition[0]+30;
				var newY = ownerPosition[1]+18;
				this.setPosition(newX, newY);
				this.toMove = false;
			}
		};
		
		this.on('show', moveToContainerOrigin, this);
	},
	
	show: function() {
		if (this.owner && this.owner.isVisible() 
				&& this.geoAttrMenuButton.menu.items.length > 0) {
			CMDBuild.Management.MapEditingWindow.superclass.show.call(this);
			var firstItemOfMenu = this.geoAttrMenuButton.menu.items.first();
			if (firstItemOfMenu) {
				itemHandler.call(this, firstItemOfMenu);
			}
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
					handler: itemHandler
				});
			}
		}
	}	
});
})();