(function() {

	var tr = CMDBuild.Translation.administration.setup; // Path to translation

	Ext.define("CMDBuild.controller.administration.configuration.CMModConfigurationEmailController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		constructor: function(view) {
			this.callParent(arguments);

			this.grid = this.view.emailGrid;
			this.form = this.view.emailForm;
			this.currentEmailAccount = null;
			this.sm = this.grid.getSelectionModel();
			this.sm.on('selectionchange', onRowSelected , this);
		}
	});

	function onRowSelected(sm, selection) {
		if (selection.length > 0) {
			this.currentEmailAccount = selection[0];
			this.form.onEmailAccountSelected(this.currentEmailAccount);
		}
	}

})();