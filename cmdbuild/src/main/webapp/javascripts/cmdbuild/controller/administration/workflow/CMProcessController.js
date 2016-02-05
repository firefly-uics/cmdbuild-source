(function() {

	Ext.define('CMDBuild.controller.administration.workflow.CMProcessController', {
		extend: 'CMDBuild.controller.administration.classes.CMModClassController',

		// override
		buildSubcontrollers: function() {
			this.attributePanelController = Ext.create('CMDBuild.controller.administration.classes.CMClassAttributeController', this.view.attributesPanel);
			this.domainTabController = Ext.create('CMDBuild.controller.administration.classes.CMDomainTabController', this.view.domainGrid);
			this.processFormController = Ext.create('CMDBuild.controller.administration.workflow.CMProcessFormController', this.view.processForm);
			this.taskManagerPanelController = Ext.create('CMDBuild.controller.administration.workflow.CMProcessTasksController', this.view.taskManagerPanel);
		},

		// override
		registerToCacheEvents: function() {
			_CMCache.on('cm_process_deleted', this.view.onClassDeleted, this.view);
		},

		// override
		onViewOnFront: function(selection) {
			var processId, process;

			if (selection) {
				processId = selection.data.id;

				if (processId)
					process = _CMCache.getProcessById(processId);

				this.view.onProcessSelected(selection.data);

				this.processFormController.onProcessSelected(processId);
				this.attributePanelController.onClassSelected(processId);
				this.domainTabController.onClassSelected(processId);
				this.taskManagerPanelController.onProcessSelected(processId, process);
			}
		},

		// override
		onAddClassButtonClick: function() {
			this.processFormController.onAddClassButtonClick();
			this.domainTabController.onAddClassButtonClick();
			this.attributePanelController.onAddClassButtonClick();

			this.view.taskManagerPanel.setDisabled(true);
			this.view.onAddClassButtonClick();

			_CMMainViewportController.deselectAccordionByName('process');
		}
	});

})();