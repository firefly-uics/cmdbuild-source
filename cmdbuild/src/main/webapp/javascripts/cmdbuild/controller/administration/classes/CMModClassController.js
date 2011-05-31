(function() {
	
	Ext.define("CMDBuild.controller.administration.classes.CMModClassController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);

			this.classFormController = new CMDBuild.controller.administration.classes.CMClassFormController(this.view.classForm);
			this.domainTabController = new CMDBuild.controller.administration.classes.CMDomainTabController(this.view.domainGrid);
			this.geoAttributesController = new CMDBuild.controller.administration.classes.CMGeoAttributeControllerController(this.view.geoAttributesPanel);

			this.view.addClassButton.on("click", onAddClassButtonClick, this);
			this.view.printSchema.on("click", onPrintSchema, this);
		},

		onViewOnFront: function(selection) {
			this.view.onSelectClass(selection.data);
			this.classFormController.onSelectClass(selection.data.id);
			this.domainTabController.onSelectClass(selection.data.id);
			this.geoAttributesController.onSelectClass(selection.data.id);
		}
	});

	function onAddClassButtonClick() {
		this.classFormController.onAddClassButtonClick();
		
		_CMMainViewportController.deselectAccordionByName("class");
	}

	function onPrintSchema(format) {
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			url : 'services/json/schema/modreport/printschema',
			params: {format: format},
			method : 'POST',
			success: function(response) {
				CMDBuild.LoadMask.get().hide();
				var popup = window.open(
					"services/json/management/modreport/printreportfactory",
					"Report",
					"height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable"
				);
				
				if (!popup) {
					CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
				}
			},
			failure: function(response) {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
})();