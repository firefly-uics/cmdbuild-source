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

			this.fileField = new Ext.form.TextField({
				inputType : "file",
				fieldLabel : tr.master_report_jrxml,
                labelWidth: CMDBuild.CM_LABEL_WIDTH,
				allowBlank : false,
				name : 'jrxml'
			});

			this.name = new Ext.form.field.Text({
				fieldLabel : tr.name,
                labelWidth: CMDBuild.CM_LABEL_WIDTH,
				allowBlank : false,
				name : 'name',
				cmImmutable: true
			});

			this.description = new Ext.form.field.TextArea({
				fieldLabel : tr.description,
                labelWidth: CMDBuild.CM_LABEL_WIDTH,
				allowBlank : false,
				name : 'description',             
				maxLength : 100
			});

			this.groups = new Ext.ux.form.MultiSelect({
				fieldLabel : tr.enabled_groups,
                width: 305,
                labelWidth: CMDBuild.CM_LABEL_WIDTH,
				name : "groups",
				dataFields : [ 'id', 'description' ],
				valueField : 'id',
				displayField : 'description',
				allowBlank : true,
				store : new Ext.data.Store( {
					fields : [ 'id', 'description' ],
					proxy : {
						type : "ajax",
						url : 'services/json/management/modreport/getgroups',
						reader : {
							type : "json",
							root : "rows"
						}
					},
					autoLoad : true
				})
			});
			
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
		// if disabled, the mystical multiselect is not able to set his value
		m.enable();
		m.setValue(v);
		m.disable();
	}
})();