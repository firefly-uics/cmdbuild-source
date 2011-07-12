(function() {
	
	Ext.define("CMDBuild.controller.administration.group.CMModGroupsController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
			this.groupFormController = new CMDBuild.controller.administration.group.CMGroupFormController(this.view.groupForm);
			this.view.addGroupButton.on("click", onAddGroupButtonClick, this);
		},

		onViewOnFront: function(selection) {
			if (selection) {
				this.groupFormController.onGroupSelected(selection);
				
				this.view.onGroupSelected();
				this.view.privilegeGrid.onGroupSelected(selection);
				this.view.userPerGroup.onGroupSelected(selection);
			}
		}
	});
	
	function onAddGroupButtonClick() {
		this.groupFormController.onAddGroupButtonClick();
		this.view.onAddGroup();

		_CMMainViewportController.deselectAccordionByName("groups");
	}
})();