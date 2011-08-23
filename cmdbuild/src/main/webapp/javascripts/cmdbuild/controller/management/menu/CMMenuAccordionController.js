(function() {
	Ext.define("CMDBuild.controller.management.menu.CMMenuAccordionController", {
		extend: "CMDBuild.controller.accordion.CMBaseAccordionController",
		
		constructor: function() {
			this.callParent(arguments);
			
			this.onAccordionNodeSelect = function(selection) {
				_debug("menu selection ", selection)
			}
		},
		onAccordionExpanded: function() {
			_CMMainViewportController.bringTofrontPanelByCmName("class");
			this.reselectCurrentNodeIfExistsOtherwiseSelectTheFisrtLeaf.call(this);
		}
	});

})();