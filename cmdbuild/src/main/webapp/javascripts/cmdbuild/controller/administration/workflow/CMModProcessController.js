(function() {

	Ext.define("CMDBuild.controller.administration.workflow.CMModProcessController", {
		extend: "CMDBuild.controller.administration.classes.CMModClassController",
		// override
		buildSubcontrollers: function() {
			this.processFormController = new CMDBuild.controller.administration.workflow.CMProcessFormController(this.view.processForm);
			this.attributePanelController = new CMDBuild.controller.administration.classes.CMClassAttributeController(this.view.attributesPanel);
			this.domainTabController = new CMDBuild.controller.administration.classes.CMDomainTabController(this.view.domainGrid);
			this.cronPanelController = new CMDBuild.controller.administration.workflow.CMCronPanelController(this.view.cronPanel);
		},

		// override
		registerToCacheEvents: function() {
			_CMCache.on("cm_process_deleted", this.view.onClassDeleted, this.view);
		},

		// override
		onViewOnFront: function(selection) {
			var processId, process;

			if (selection) {
				processId = selection.data.id;
				if (processId) {
					process = _CMCache.getProcessById(processId);
				}
				this.view.onProcessSelected(selection.data);

				this.processFormController.onProcessSelected(processId);
				this.cronPanelController.onProcessSelected(processId, process);
				this.attributePanelController.onClassSelected(processId);
				this.domainTabController.onClassSelected(processId);
			}
		},

		// override
		onAddClassButtonClick: function() {
			this.processFormController.onAddClassButtonClick();
			this.domainTabController.onAddClassButtonClick();
			this.attributePanelController.onAddClassButtonClick();
			this.cronPanelController.onAddClassButtonClick();

			this.view.onAddClassButtonClick();
			_CMMainViewportController.deselectAccordionByName("process");
		}
	});

})();