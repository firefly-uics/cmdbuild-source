(function() {
	Ext.ns("CMDBuild.administration.domain");
	CMDBuild.administration.domain.ModDomainController = function(panel, accordion) {
		this.panel = panel;
		this.accordion = accordion;

		this.formController = new CMDBuild.administration.domain.CMDomainFormController(this.panel.domainForm);

		this.panel.addButton.on("click", function() {
			this.formController.onAddButtonClick();
			this.accordion.deselect();
		}, this);

		this.panel.on("select", function(params) {
			this.formController.onDomainSelected(params);
		}, this);
	}

})();