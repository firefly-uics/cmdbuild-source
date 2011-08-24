(function() {
	Ext.define("CMDBuild.controller.management.menu.CMMenuAccordionController", {
		extend: "CMDBuild.controller.accordion.CMBaseAccordionController",
		
		constructor: function() {
			this.callParent(arguments);
		},

		onAccordionExpanded: function() {
			_CMMainViewportController.bringTofrontPanelByCmName("class");
			this.reselectCurrentNodeIfExistsOtherwiseSelectTheFisrtLeaf.call(this);
		},

		onAccordionNodeSelect: function(selection) {
			this.callParent(arguments);
		}

	});

})();