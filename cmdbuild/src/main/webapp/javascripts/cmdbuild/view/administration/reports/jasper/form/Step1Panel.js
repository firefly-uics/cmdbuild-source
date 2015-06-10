(function() {

	Ext.define('CMDBuild.view.administration.reports.jasper.form.Step1Panel', {
		extend: 'Ext.form.Panel',

		border: false,
		cls: 'x-panel-body-default-framed',
		encoding: 'multipart/form-data',
		fileUpload: true,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.name = Ext.create('Ext.form.field.Text',{
						name: CMDBuild.core.proxy.CMProxyConstants.TITLE, // TODO: should be renamed in "name"
						fieldLabel: CMDBuild.Translation.name,
						allowBlank: false,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						cmImmutable: true
					}),
					this.description = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						translationsKeyType: 'Report',
						translationsKeyField: 'Description',
						allowBlank: false
					}),
					this.groups = Ext.create('CMDBuild.view.common.field.CMGroupSelectionList', {
						name: CMDBuild.core.proxy.CMProxyConstants.GROUPS,
						fieldLabel: CMDBuild.Translation.enabledGroups,
						height: 300,
						valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH
					}),
					this.fileField = Ext.create('Ext.form.field.File', {
						name: CMDBuild.core.proxy.CMProxyConstants.JRXML,
						fieldLabel: CMDBuild.Translation.masterReportJrxml,
						allowBlank: false,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH
					}),
					this.reportId = Ext.create('Ext.form.field.Hidden', {
						name: CMDBuild.core.proxy.CMProxyConstants.ID
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();