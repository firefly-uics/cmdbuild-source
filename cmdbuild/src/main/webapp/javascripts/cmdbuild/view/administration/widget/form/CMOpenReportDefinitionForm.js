(function() {

	var tr = CMDBuild.Translation.administration.modClass.widgets;

	Ext.define('CMDBuild.view.administration.widget.form.CMOpenReportDefinitionForm', {
		extend: 'CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm',

		requires: ['CMDBuild.core.proxy.Constants'],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		statics: {
			WIDGET_NAME: '.OpenReport'
		},

		/**
		 * @cfg {CMDBuild.controller.administration.widget.CMOpenReportController}
		 */
		delegate: undefined,

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

		layout: 'hbox',

		initComponent: function() {
			this.callParent(arguments);

			var me = this;

			this.addEvents('cm-selected-report'); // Fired when is set the report in the combo-box
			this.mon(this.reportCode, 'select', function(field, records) {
				me.fireEvent('cm-selected-report', records);
			}, this.reportCode);
		},

		/**
		 * @override
		 */
		buildForm: function() {
			var me = this;

			this.callParent(arguments);

			this.reportCode = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.core.proxy.Constants.CODE,
				fieldLabel: tr[me.self.WIDGET_NAME].fields.report,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				valueField: CMDBuild.core.proxy.Constants.TITLE,
				displayField: CMDBuild.core.proxy.Constants.DESCRIPTION,
				forceSelection: true,
				editable: false,

				store: CMDBuild.core.proxy.widgets.OpenReport.getReportsStore()
			});

			this.forceFormatCheck = Ext.create('Ext.form.field.Checkbox', {
				flex: 1
			});

			this.forceFormatOptions = Ext.create('Ext.form.field.ComboBox', {
				displayField: CMDBuild.core.proxy.Constants.TEXT,
				valueField: CMDBuild.core.proxy.Constants.VALUE,
				flex: 4,
				forceSelection: true,
				editable: false,

				store: Ext.create('Ext.data.ArrayStore', {
					autoDestroy: true,
					fields: [CMDBuild.core.proxy.Constants.VALUE, CMDBuild.core.proxy.Constants.TEXT],
					data: [
						['pdf', 'PDF'],
						['csv', 'CSV']
					]
				}),
				queryMode: 'local'
			});

			this.forceFormat = Ext.create('Ext.form.FieldContainer', {
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				fieldLabel: tr[me.self.WIDGET_NAME].fields.force,
				labelWidth: CMDBuild.LABEL_WIDTH,

				layout: {
					type: 'hbox'
				},

				items: [this.forceFormatCheck, this.forceFormatOptions]
			});

			// PresetGrid
				this.presetGrid = Ext.create('Ext.grid.Panel', {
					title: tr[me.self.WIDGET_NAME].fields.presets,
					considerAsFieldToDisable: true,
					margin: '0 0 0 3',
					flex: 1,

					plugins: [
						Ext.create('Ext.grid.plugin.CellEditing', {
							clicksToEdit: 1
						})
					],

					columns: [
						{
							header: tr[me.self.WIDGET_NAME].presetGrid.attribute,
							dataIndex: CMDBuild.core.proxy.Constants.NAME,
							editor: { xtype: 'textfield' },
							flex: 1
						},
						{
							header: tr[me.self.WIDGET_NAME].presetGrid.value,
							dataIndex: CMDBuild.core.proxy.Constants.VALUE,
							editor: { xtype: 'textfield' },
							flex: 1
						},
						{
							xtype: 'checkcolumn',
							header: CMDBuild.Translation.readOnly,
							dataIndex: CMDBuild.core.proxy.Constants.READ_ONLY,
							width: 60,
							align: 'center',
							sortable: false,
							hideable: false,
							menuDisabled: true,
							fixed: true
						}
					],

					store: Ext.create('Ext.data.Store', {
						model: 'CMDBuild.model.widget.CMModelOpenReport.presetGrid',
						data: []
					})
				});
			// END: PresetGrid

			// DefaultFields is inherited
			this.defaultFields.add(this.reportCode, this.forceFormat);

			Ext.apply(this, {
				items: [this.defaultFields, this.presetGrid]
			});
		},

		/**
		 * Fills presetGrid with data and merges if presetGrid is already filled
		 *
		 * @param {Object} data - Ex. { name: value }
		 * @param {Array} readOnlyAttributes
		 */
		fillPresetWithData: function(data, readOnlyAttributes) {
			readOnlyAttributes = readOnlyAttributes || [];

			if (!Ext.Object.isEmpty(data)) {
				for (var key in data) {
					var storeReportIndex = this.presetGrid.getStore().find(
						CMDBuild.core.proxy.Constants.NAME,
						key
					);

					// If present in presetGrid and is empty, remove and set as not present so will be added as data value or empty
					if (
						storeReportIndex >= 0
						&& Ext.isEmpty(this.presetGrid.getStore().getAt(storeReportIndex).get(CMDBuild.core.proxy.Constants.VALUE))
					) {
						this.presetGrid.getStore().removeAt(storeReportIndex);

						storeReportIndex = -1;
					}

					// If not present in presetGrid store, add
					if (storeReportIndex < 0) {
						var recordConf = {};

						recordConf[CMDBuild.core.proxy.Constants.NAME] = key;
						recordConf[CMDBuild.core.proxy.Constants.VALUE] = data[key] || '';

						if (Ext.Array.contains(readOnlyAttributes, key)) {
							recordConf[CMDBuild.core.proxy.Constants.READ_ONLY] = true;
						} else {
							recordConf[CMDBuild.core.proxy.Constants.READ_ONLY] = false;
						}

						this.presetGrid.getStore().add(recordConf);
					}
				}
			}
		},

		/**
		 * @return {Object}
		 */
		getPresetData: function() {
			var records = this.presetGrid.getStore().getRange();
			var data = {};
			var readOnly = [];

			for (var i = 0, l = records.length; i < l; ++i) {
				var recData = records[i].data;

				if (!Ext.isEmpty(recData[CMDBuild.core.proxy.Constants.NAME]) && !Ext.isEmpty(recData[CMDBuild.core.proxy.Constants.VALUE])) {
					data[recData[CMDBuild.core.proxy.Constants.NAME]] = recData[CMDBuild.core.proxy.Constants.VALUE];

					if (recData[CMDBuild.core.proxy.Constants.READ_ONLY])
						readOnly.push(recData[CMDBuild.core.proxy.Constants.NAME]);
				}
			}

			return {
				data: data,
				readOnly: readOnly
			}
		},

		/**
		 * Fills form with widget data
		 *
		 * @param {CMDBuild.model.widget.WidgetDefinition} model
		 *
		 * @override
		 */
		fillWithModel: function(model) {
			this.callParent(arguments);

			this.reportCode.setValue(model.get(CMDBuild.core.proxy.Constants.REPORT_CODE));

			var forceFormat = model.get(CMDBuild.core.proxy.Constants.FORCE_FORMAT);
			if (forceFormat) {
				this.forceFormatCheck.setValue(true);
				this.forceFormatOptions.setValue(forceFormat);
			}

			// Find selected report ID and manually calls onReportSelected to fill presetGrid
			this.reportCode.getStore().on('load', function() {
				var storeReportIndex = this.reportCode.getStore().find(
					CMDBuild.core.proxy.Constants.DESCRIPTION,
					model.get(CMDBuild.core.proxy.Constants.REPORT_CODE)
				);

				if(storeReportIndex >= 0)
					this.delegate.onReportSelected(
						this.reportCode.getStore().getAt(storeReportIndex).get(CMDBuild.core.proxy.Constants.ID),
						model.get(CMDBuild.core.proxy.Constants.PRESET),
						model.get(CMDBuild.core.proxy.Constants.READ_ONLY_ATTRIBUTES)
					);
			}, this);

			this.fillPresetWithData(model.get(CMDBuild.core.proxy.Constants.PRESET), model.get(CMDBuild.core.proxy.Constants.READ_ONLY_ATTRIBUTES));
		},

		/**
		 * @override
		 */
		disableNonFieldElements: function() {
			this.presetGrid.disable();
		},

		/**
		 * @override
		 */
		enableNonFieldElements: function() {
			this.presetGrid.enable();

			// Enable forceFormatOptions only if forceFormatCheck is checked
			if (!this.forceFormatCheck.getValue())
				this.forceFormatOptions.disable();
		},

		/**
		 * @return {Object} returnObject
		 *
		 * @override
		 */
		getWidgetDefinition: function() {
			var presetData = this.getPresetData();
			var returnObject = {};

			returnObject[CMDBuild.core.proxy.Constants.REPORT_CODE] = this.reportCode.getValue();
			returnObject[CMDBuild.core.proxy.Constants.FORCE_FORMAT] = this.forceFormatOptions.getValue();
			returnObject[CMDBuild.core.proxy.Constants.PRESET] = presetData.data;
			returnObject[CMDBuild.core.proxy.Constants.READ_ONLY_ATTRIBUTES] = presetData.readOnly;

			return Ext.apply(this.callParent(arguments), returnObject);
		}
	});

})();