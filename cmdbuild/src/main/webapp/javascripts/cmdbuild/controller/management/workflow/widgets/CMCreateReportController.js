(function() {
	Ext.define("CMDBuild.controller.management.workflow.widgets.CMCreateReportController", {
		extend: "CMDBuild.controller.management.workflow.widget.CMBaseWFWidgetController",
		cmName: "Create Report",

		constructor: function() {
			this.callParent(arguments);
			this.widgetConf = this.view.widgetConf;

			this.view.mon(this.view.saveButton, "click", onSaveCardClick, this);
		},

		// override
		beforeActiveView: function() {
			Ext.Ajax.request( {
				url : 'services/json/management/modreport/createreportfactorybytypecode',
				params : {
					type : this.widgetConf.ReportType,
					code : this.widgetConf.ReportCode
				},
				success : function(response) {
					var ret = Ext.JSON.decode(response.responseText);
					if (ret.filled) { // report with no parameters
					} else { // show form with launch parameters
						this.view.attributeList = ret.attribute;
					}

					this.view.configureForm();
					this.view.fillFormValues();
				},
				scope : this
			});
		}
	});

	function onSaveCardClick() {
		var form = this.view.formPanel.getForm();
		this.view.formatCombo.enable();
		if (form.isValid()) {
			CMDBuild.LoadMask.get().show();

			form.submit({
				method : 'POST',
				url : 'services/json/management/modreport/updatereportfactoryparams',
				scope: this,
				success : function(form, action) {
					var popup = window.open("services/json/management/modreport/printreportfactory?donotdelete=true", "Report", "height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable");
					if(!popup) {
						CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
					}
					this.view.formatCombo.disable();
					CMDBuild.LoadMask.get().hide();
				},
				failure: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	}
})();