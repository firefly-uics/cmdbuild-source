(function() {

	Ext.define('CMDBuild.view.administration.report.jasper.form.Step1Panel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		description: undefined,

		/**
		 * @property {Ext.form.field.File}
		 */
		fileField: undefined,

		/**
		 * @property {CMDBuild.view.common.field.CMGroupSelectionList}
		 */
		groups: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		name: undefined,

		/**
		 * @property {Ext.form.field.Hidden}
		 */
		reportId: undefined,

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
						name: CMDBuild.core.proxy.Constants.TITLE, // TODO: should be renamed in "name"
						fieldLabel: CMDBuild.Translation.name,
						allowBlank: false,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						cmImmutable: true
					}),
					this.description = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: CMDBuild.core.proxy.Constants.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						allowBlank: false,

						translationFieldConfig: {
							type: CMDBuild.core.proxy.Constants.REPORT,
							identifier: { sourceType: 'form', key: CMDBuild.core.proxy.Constants.TITLE, source: this }, // TODO: should be renamed in "name"
							field: CMDBuild.core.proxy.Constants.DESCRIPTION
						}
					}),
					this.groups = Ext.create('CMDBuild.view.common.field.CMGroupSelectionList', {
						name: CMDBuild.core.proxy.Constants.GROUPS,
						fieldLabel: CMDBuild.Translation.enabledGroups,
						height: 300,
						valueField: CMDBuild.core.proxy.Constants.NAME,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH
					}),
					this.fileField = Ext.create('Ext.form.field.File', {
						name: CMDBuild.core.proxy.Constants.JRXML,
						fieldLabel: CMDBuild.Translation.masterReportJrxml,
						allowBlank: false,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH
					}),
					this.reportId = Ext.create('Ext.form.field.Hidden', {
						name: CMDBuild.core.proxy.Constants.ID
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();