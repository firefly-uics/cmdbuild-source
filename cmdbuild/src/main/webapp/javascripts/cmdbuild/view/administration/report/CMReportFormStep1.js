(function() {

	var tr = CMDBuild.Translation.administration.modreport.importJRFormStep1;

	Ext.define("CMDBuild.view.administration.report.CMReportFormStep1", {
		extend : "Ext.form.Panel",

		mixins : {
			cmFormFunction: "CMDBUild.view.common.CMFormFunctions"
		},

		encoding : 'multipart/form-data',
		fileUpload : true,
		defaultType : 'textfield',
		plugins : [ new CMDBuild.CallbackPlugin() ],
		autoScroll: true,

		initComponent : function() {

			this.fileField = new Ext.form.field.File({
				fieldLabel : tr.master_report_jrxml,
				allowBlank : false,
				name : 'jrxml'
			});

			this.name = new Ext.form.field.Text({
				fieldLabel : tr.name,
				allowBlank : false,
				name : 'name',
				cmImmutable: true
			});

			this.description = new Ext.form.field.TextArea({
				fieldLabel : tr.description,
				allowBlank : false,
				name : 'description',
				maxLength : 100
			});

			this.groups = new CMDBuild.view.common.field.CMGroupSelectionList();

			this.items = [
				this.name,
				this.description,
				this.groups,
				this.fileField
			];

			this.callParent(arguments);

			this.disableFields();
		},

		onReportSelected: function(report) {
			this.reset();
			this.name.setValue(report.get("title"));
			this.description.setValue(report.get("description"));
			setValueToMultiselect(this.groups, report.get("groups"));
		}
	});

	function setValueToMultiselect(m, stringValue) {
		var v = stringValue.split(",");
		m.setValue(v);
	}
})();