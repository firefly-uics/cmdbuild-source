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

//						buildEditControls : function buildEditControls(layer,
//								type) {
//						},
//
//						destroyEditControls : function destroyEditControls(
//								layer) {
//						},
//
						addFeatureButtonHasBeenToggled : function onAddFeatureButtonToggle(
								toggled) {
							this.interactionDocument.feature.operation = "Draw";
							this.interactionDocument.changedFeature();
						},

						removeFeatureButtonHasBeenClicked : function onRemoveFeatureButtonClick() {
						},

						geoAttributeMenuItemHasBeenClicked : function(item) {
							this.interactionDocument.setCurrentFeature(item.name, item.geoType, 'Modify');
							this.interactionDocument.changedFeature();
						},

//						activateTransformConrol : function(layerName) {
//						},
//
//						deactivateEditControls : function deactivateEditControls() {
//						}
					});

})();