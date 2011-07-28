(function() {
	Ext.ns("CMDBuild.administration");
	
	CMDBuild.administration.CMMainController = function(view) {
		this.view = view;
		
		var modDomainController = new CMDBuild.administration.domain.ModDomainController(
				this.view.getModByName("CMModDomain"),
				this.view.getTreeByName("CMDomainAccordion"));
		
		var modClassController = new CMDBuild.administration["class"].ModClassController(
				this.view.getModByName("CMModClass"),
				this.view.getTreeByName("CMClassAccordion"));
		
		var modWorkflowController = new CMDBuild.administration["class"].ModClassController(
				this.view.getModByName("CMModWorkflow"),
				this.view.getTreeByName("CMWorkflowAccordion"));
		
		modClassController.onAddDomainButtonClick = onAddDomainButtonClick;
		modClassController.onDomainDoubleClick = onDomainDoubleClick; 

		modWorkflowController.onAddDomainButtonClick = onAddDomainButtonClick;
		modWorkflowController.onDomainDoubleClick = onDomainDoubleClick; 

		function onAddDomainButtonClick() {
			modDomainController.onAddDomainButtonClick();
		}
		
		function onDomainDoubleClick(id) {
			modDomainController.onDomainDoubleClick(id);
		}
	};
})();