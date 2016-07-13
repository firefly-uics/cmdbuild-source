(function() {
	Ext
			.define(
					"CMDBuild.controller.management.classes.map.CMMapEditingWindowDelegate",
					{
						extend : "CMDBuild.view.management.map.CMMapEditingToolsWindowDelegate",
						currentGisShape : undefined,
						constructor : function(master, interactionDocument) {
							this.master = master;
							this.interactionDocument = interactionDocument;
							this.callParent(arguments);
						},

						buildEditControls : function buildEditControls(layer,
								type) {
						},

						destroyEditControls : function destroyEditControls(
								layer) {
						},

						addFeatureButtonHasBeenToggled : function onAddFeatureButtonToggle(
								toggled) {
/*							var layerName = this.currentEditLayer.get("name");
							if (toggled) {
								activateControl(this, layerName, "creation");
								deactivateControl(this, layerName, "transform");
							} else {
								deactivateControl(this, layerName, "creation");
								activateControl(this, layerName, "transform");
							}
*/						},

						removeFeatureButtonHasBeenClicked : function onRemoveFeatureButtonClick() {
//							if (this.currentEditLayer) {
//								this.currentEditLayer.removeAllFeatures();
//							}
						},

						geoAttributeMenuItemHasBeenClicked : function(item) {
							this.interactionDocument.setCurrentFeature(item.name, item.geoType, 'Modify');
							this.interactionDocument.changedFeature();
						},

						activateTransformConrol : function(layerName) {
						},

						deactivateEditControls : function deactivateEditControls() {
						}
					});

})();