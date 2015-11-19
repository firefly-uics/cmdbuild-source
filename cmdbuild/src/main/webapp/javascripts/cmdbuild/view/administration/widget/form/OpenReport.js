(function() {

	Ext.define('CMDBuild.view.administration.widget.form.OpenReport', {
		extend: 'CMDBuild.view.administration.widget.form.AbstractWidgetDefinitionForm',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.widgets.OpenReport',
			'CMDBuild.model.widget.openReport.PresetGrid'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.OpenReport}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.panel.Panel}
		 */
		defaultFields: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		forceFormatCheck: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		forceFormatOptions: undefined,

		/**
		 * @property {Ext.grid.Panel}
		 */
		presetGrid: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		reportCode: undefined,

		/**
		 * Enable forceFormatOptions only if forceFormatCheck is checked
		 */
		enableNonFieldElements: function() {
			if (!this.forceFormatCheck.getValue())
				this.forceFormatOptions.disable();
		},

		/**
		 * Builds widget configuration custom fields
		 *
		 * @returns {Array}
		 *
		 * @override
		 */
		widgetDefinitionFormBasePropertiesGet: function() {
			return Ext.Array.push(this.widgetDefinitionFormCommonBasePropertiesGet(), [
				this.reportCode = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.core.constants.Proxy.REPORT_CODE,
					fieldLabel: CMDBuild.Translation.report,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					valueField: CMDBuild.core.constants.Proxy.TITLE,
					displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
					forceSelection: true,
					editable: false,

					store: CMDBuild.core.proxy.widgets.OpenReport.getStoreReports(),
					queryMode: 'local',

					listeners: {
						scope: this,
						select: function(combo, records, eOpts) {
							this.delegate.cmfg('onWidgetOpenReportReportSelect', { selectedReport: records[0] });
						}
					}
				}),
				this.forceFormat = Ext.create('Ext.form.FieldContainer', {
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					fieldLabel: CMDBuild.Translation.forceFormat,
					labelWidth: CMDBuild.LABEL_WIDTH,

					layout: {
						type: 'hbox'
					},

					items: [
						this.forceFormatCheck = Ext.create('Ext.form.field.Checkbox', { flex: 1 }),
						this.forceFormatOptions = Ext.create('Ext.form.field.ComboBox', {
							displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
							valueField: CMDBuild.core.constants.Proxy.VALUE,
							flex: 4,
							forceSelection: true,
							editable: false,

							store: CMDBuild.core.proxy.widgets.OpenReport.getStoreForceFormat(),
							queryMode: 'local'
						})
					]
				})
			]);
		},

		/**
		 * @returns {Array}
		 *
		 * @override
		 */
		widgetDefinitionFormCustomPropertiesGet: function() {
			return [
				this.presetGrid = Ext.create('Ext.grid.Panel', {
					title: CMDBuild.Translation.reportAttributes,
					considerAsFieldToDisable: true,
					margin: '8 0 0 0',

					plugins: [
						Ext.create('Ext.grid.plugin.CellEditing', { clicksToEdit: 1 })
					],

					columns: [
						{
							dataIndex: CMDBuild.core.constants.Proxy.NAME,
							text: CMDBuild.Translation.attribute,
							editor: { xtype: 'textfield' },
							flex: 1
						},
						{
							dataIndex: CMDBuild.core.constants.Proxy.VALUE,
							text: CMDBuild.Translation.value,
							editor: { xtype: 'textfield' },
							flex: 1
						},
						Ext.create('Ext.grid.column.CheckColumn', {
							dataIndex: CMDBuild.core.constants.Proxy.READ_ONLY,
							text: CMDBuild.Translation.readOnly,
							width: 60,
							align: 'center',
							hideable: false,
							menuDisabled: true,
							fixed: true,
						})
					],

					store: Ext.create('Ext.data.Store', {
						model: 'CMDBuild.model.widget.openReport.PresetGrid',
						data: [],
						sorters: [
							{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
						]
					})
				})
			];
		}
	});

})();