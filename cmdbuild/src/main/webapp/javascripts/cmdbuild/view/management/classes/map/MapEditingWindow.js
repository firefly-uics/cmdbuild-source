(function() {

	var ICON_CLS = {
		"POINT": "mapFeaturePoint",
		"LINESTRING": "mapFeatureLine",
		"POLYGON": "mapFeaturePolygon"
	};

	Ext.define("CMDBuild.view.management.map.CMMapEditingToolsWindow", {
		addFeatureButtonHasBeenToggled: Ext.emptyFn,
		removeFeatureButtonHasBeenClicked: Ext.emptyFn,
		geoAttributeMenuItemHasBeenClicked: Ext.emptyFn
	});

	/**
	 * @class CMDBuild.view.management.map.MapEditingWindow
	 * @extends Ext.Window
	 * 
	 * This window looks like an external toolbar (Photoshop style)
	 * After the creation is possible to say to the window to show the tools
	 * for a specific geometry type
	 * 
	 * The window can be shown only if the owner panel is visible
	 */
	Ext.define("CMDBuild.view.management.map.CMMapEditingToolsWindow", {
		extend: "Ext.Window",
		editingControls: {},
		toMove: true, //to initialize the position of the window the first time that be shown	
		translation: CMDBuild.Translation,

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		owner: {
			getPosition: function() { return [0,0]; }
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this, "CMDBuild.view.management.map.CMMapEditingToolsWindow");
			this.callParent(arguments);
		},

		initComponent: function() {

			this.closable = false;
			this.hideBorders = true;
			this.resizable = false;
			this.frame = false;

			this.layout = {
				type : 'hbox',
				padding : '2',
				align : 'stretch'
			};

			this.geoAttrMenuButton = new Ext.Button({
				text: this.translation.management.modcard.gis.geo_attributes,
				menu: new Ext.menu.Menu({
					items: []
				})
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
					this.callDelegates("addFeatureButtonHasBeenToggled", state);
				}
			});

			this.removeButton = new Ext.Button({
				text: this.translation.common.buttons.remove,
				iconCls: 'delete',
				scope: this,
				disabled: true,
				handler: function() {
					this.callDelegates("removeFeatureButtonHasBeenClicked");
				}
			});

			this.callParent(arguments);
		},

		// add the buttons on render
		// to allow the window to resize it
		// automatically
		onRender: function() {
			this.callParent(arguments);

			this.add([
				this.geoAttrMenuButton,
				this.addButton,
				this.removeButton
			]);
		},

		show: function() {
			if (this.geoAttrMenuButton.menu.items.length > 0) {

				this.callParent(arguments);

				var firstItemOfMenu = this.geoAttrMenuButton.menu.items.first();
				if (firstItemOfMenu) {
					onAddMenuitemSelect.call(this, firstItemOfMenu);
				}

			}
		},
	
		update: function(layers) {
			this.geoAttrMenuButton.menu.removeAll(true);

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

	function onAddMenuitemSelect(item) {
		this.geoAttrMenuButton.setText(item.text);
		this.geoAttrMenuButton.setIconCls(item.iconCls);
		this.addButton.enable();
		this.addButton.toggle(false);
		this.removeButton.enable();

		this.callDelegates("geoAttributeMenuItemHasBeenClicked", item.editLayer);
	};

})();