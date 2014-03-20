Ext.define("CMDBuild.delegate.administration.emailTemplate.CMEmailTemplateFormFieldsManager", {
	extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseFormFiledsManager",

	/**
	 * @return {array} an array of Ext.component to use as form items
	 */
	build: function() {

		this.nameField = new Ext.form.field.Text({ //
			name: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE_NAME,
			fieldLabel: CMDBuild.Translation.name,
			labelWidth: CMDBuild.LABEL_WIDTH,
			cmImmutable: true,
			allowBlank: false
		});

		this.descriptionField = new Ext.form.field.TextArea({
			name: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
			labelWidth: CMDBuild.LABEL_WIDTH,
			fieldLabel: CMDBuild.Translation.description_
		});

		this.toField = new Ext.form.field.Text({
			name: CMDBuild.ServiceProxy.parameter.TO,
			labelWidth: CMDBuild.LABEL_WIDTH,
			fieldLabel: CMDBuild.Translation.to,
			allowBlank: false
		});

		this.ccField = new Ext.form.field.Text({
			name: CMDBuild.ServiceProxy.parameter.CC,
			labelWidth: CMDBuild.LABEL_WIDTH,
			fieldLabel: CMDBuild.Translation.cc
		});

		this.bccField = new Ext.form.field.Text({
			name: CMDBuild.ServiceProxy.parameter.BCC,
			labelWidth: CMDBuild.LABEL_WIDTH,
			fieldLabel: CMDBuild.Translation.bcc
		});

		this.subjectField = new Ext.form.field.Text({
			name: CMDBuild.ServiceProxy.parameter.SUBJECT,
			labelWidth: CMDBuild.LABEL_WIDTH,
			fieldLabel: CMDBuild.Translation.subject,
			allowBlank: false
		});

		this.bodyField = new CMDBuild.view.common.field.CMHtmlEditorField({
			name: CMDBuild.ServiceProxy.parameter.BODY,
			labelWidth: CMDBuild.LABEL_WIDTH,
			fieldLabel: CMDBuild.Translation.emailBody,
			considerAsFieldToDisable: true,
			enableFont: false
		});

		this.globalCheckBox = new Ext.form.field.Checkbox({
			labelWidth: CMDBuild.LABEL_WIDTH,
			fieldLabel: CMDBuild.Translation.global
		});

		var fieldsets = [{
			xtype: "container",
			layout: {
				type: 'hbox',
				align: 'stretch'
			},
			frame: false,
			border: false,
			defaults: {
				flex: 1,
				layout: {
					type: 'vbox',
					align: 'stretch'
				}
			},
			items: [{
				xtype: "fieldset",
				title: CMDBuild.Translation.administration.modClass.attributeProperties.baseProperties,
				items: [this.nameField, this.descriptionField, this.globalCheckBox]
			}, {
				xtype: "fieldset",
				title: CMDBuild.Translation.emailTemplate,
				margins: "0 0 0 5",
				items: [this.toField, this.ccField, this.bccField, this.subjectField, this.bodyField]
			}]
		}];

		return fieldsets;
	},

	/**
	 * 
	 * @param {Ext.data.Model} record
	 * the record to use to fill the field values
	 */
	loadRecord: function(record) {
		this.nameField.setValue(record.get(CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE_NAME));
		this.descriptionField.setValue(record.get(CMDBuild.ServiceProxy.parameter.DESCRIPTION));
		this.toField.setValue(record.get(CMDBuild.ServiceProxy.parameter.TO));
		this.ccField.setValue(record.get(CMDBuild.ServiceProxy.parameter.CC));
		this.bccField.setValue(record.get(CMDBuild.ServiceProxy.parameter.BCC));
		this.subjectField.setValue(record.get(CMDBuild.ServiceProxy.parameter.SUBJECT));
		this.bodyField.setValue(record.get(CMDBuild.ServiceProxy.parameter.BODY));
		this.globalCheckBox.setValue(record.isGlobal());
	},

	/**
	 * @return {object} values
	 * a key/value map with the values of the fields
	 */
	// override
	getValues: function() {
		var values = {};

		values[CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE_NAME] = this.nameField.getValue();
		values[CMDBuild.ServiceProxy.parameter.DESCRIPTION] = this.descriptionField.getValue();
		values[CMDBuild.ServiceProxy.parameter.TO] = this.toField.getValue();
		values[CMDBuild.ServiceProxy.parameter.CC] = this.ccField.getValue();
		values[CMDBuild.ServiceProxy.parameter.BCC] = this.bccField.getValue();
		values[CMDBuild.ServiceProxy.parameter.SUBJECT] = this.subjectField.getValue();
		values[CMDBuild.ServiceProxy.parameter.BODY] = this.bodyField.getValue();

		// the flag say to the controller to set
		// the right classId
		values[CMDBuild.ServiceProxy.parameter.CLASS_ID] = !this.globalCheckBox.getValue();

		return values;
	},

	/**
	 * clear the values of his fields
	 */
	reset: function() {
		this.nameField.reset();
		this.descriptionField.reset();
		this.toField.reset();
		this.ccField.reset();
		this.bccField.reset();
		this.subjectField.reset();
		this.bodyField.reset();
	}
});