(function() {
	
	Ext.define("CMDBuild.controller.administration.group.CMModGroupsController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
			this.groupFormController = new CMDBuild.controller.administration.group.CMGroupFormController(this.view.groupForm);
			this.groupUIConfigurationController = new CMDBuild.controller.administration.group.CMGroupUIConfigurationController(this.view.uiConfigurationPanel);
			this.view.addGroupButton.on("click", onAddGroupButtonClick, this);
		},

		onViewOnFront: function(selection) {
			this.view.onGroupSelected();
			if (selection) {
				var g = _CMCache.getGroupById(selection.get("id"));
				if (g) {
					this.groupFormController.onGroupSelected(g);
					this.groupUIConfigurationController.onGroupSelected(g);

					// Administrator groups have full privileges,
					// so this panel could be disabled
					this.view.privilegeGrid.setDisabled(g.isAdmin());

					// The CloudAdministrator could not change the users of
					// full administrator groups
					var currentGroup = _CMCache.getGroupById(CMDBuild.Runtime.DefaultGroupId);
					if (currentGroup.isCloudAdmin()
						&& g.isAdmin()
						&& !g.isCloudAdmin()) {

						this.view.userPerGroup.disable();
					} else {
						this.view.enable();
						this.view.userPerGroup.onGroupSelected(selection);
					}
				}

				this.view.privilegeGrid.onGroupSelected(selection);
			}
		}
	});

	function onAddGroupButtonClick() {
		this.groupFormController.onAddGroupButtonClick();
		this.view.onAddGroup();

		_CMMainViewportController.deselectAccordionByName("groups");
	}
})();