(function() {
	
	Ext.define("CMDBuild.controller.administration.workflow.CMModProcessController", {
		extend: "CMDBuild.controller.administration.classes.CMModClassController",
		// override
		buildSubcontrollers: function() {
			this.processFormController = new CMDBuild.controller.administration.workflow.CMProcessFormController(this.view.processForm);
			this.attributePanelController = new CMDBuild.controller.administration.classes.CMClassAttributeController(this.view.attributesPanel);
			this.domainTabController = new CMDBuild.controller.administration.classes.CMDomainTabController(this.view.domainGrid);
			this.xpdlPanelController = new CMDBuild.controller.administration.workflow.CMXpdlPanelController(this.view.xpdlPanel);
			this.cronPanelController = new CMDBuild.controller.administration.workflow.CMCronPanelController(this.view.cronPanel);
		},

		// override
		registerToCacheEvents: function() {
			_CMCache.on("cm_process_deleted", this.view.onClassDeleted, this.view);
		},

		// override
		onViewOnFront: function(selection) {
			if (selection) {
				this.view.onProcessSelected(selection.data);

				this.processFormController.onProcessSelected(selection.data.id);
				this.xpdlPanelController.onProcessSelected(selection.data.id);
				this.cronPanelController.onProcessSelected(selection.data.id);
				this.attributePanelController.onClassSelected(selection.data.id);
				this.domainTabController.onClassSelected(selection.data.id);
			}
		},

		// override
		onAddClassButtonClick: function() {
			this.processFormController.onAddClassButtonClick();
			this.domainTabController.onAddClassButtonClick();
			this.attributePanelController.onAddClassButtonClick();
			this.xpdlPanelController.onAddClassButtonClick();
			this.cronPanelController.onAddClassButtonClick();
			
			this.view.onAddClassButtonClick();
			_CMMainViewportController.deselectAccordionByName("process");
		}
	});

})();