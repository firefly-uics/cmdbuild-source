(function() {
	Ext.define("CMDBuild.controller.administration.classes.CMDomainTabController", {
		constructor: function(view) {
			this.view = view;
			this.selection = null;
			
			this.view.on("itemdblclick", onItemDoubleClick, this);
			this.view.addDomainButton.on("click", onAddDomainButton, this);
		},

		onClassSelected: function(classId) {
			this.selection = classId;
			this.view.store.load({
				params : {
					idClass : classId || -1
				}
			});
			this.view.enable();
		},

		onAddClassButtonClick: function() {
			this.selection = null;
			this.view.disable();
		}

	});

	function onItemDoubleClick(grid, record) {
		var domainAccordion = _CMMainViewportController.findAccordionByCMName("domain");
		domainAccordion.expand();
		domainAccordion.selectNodeById(record.get("idDomain"));
	}

	function onAddDomainButton() {
		var domainAccordion = _CMMainViewportController.findAccordionByCMName("domain");
		domainAccordion.expand();
		_CMMainViewportController.panelControllers["domain"].onAddDomainButtonClick();
	}
})();