(function() {

	var tr = CMDBuild.Translation.administration.modClass.widgets;

	Ext.define("CMDBuild.view.administration.widget.form.CMOpenReportDefinitionForm", {
		extend: "CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm",

		statics: {
			WIDGET_NAME: ".OpenReport"
		},

		initComponent: function() {
			this.callParent(arguments);

			this.addEvents(
				/* fired when is set the report in the combo-box*/
				"cm-selected-report"
			);

			var me = this;
			this.mon(this.reportCode, "select", function(field, records) {
				me.fireEvent("cm-selected-report", records);
			}, this.reportCode);

		},

		// override
		buildForm: function() {
			var me = this;

			this.callParent(arguments);

			this.reportCode = new CMDBuild.field.CMBaseCombo({
				name: "code",
				fieldLabel: tr[me.self.WIDGET_NAME].fields.report,
				labelWidth: CMDBuild.LABEL_WIDTH,
				valueField: CMDBuild.model.CMReportAsComboItem._FIELDS.value,
				displayField: CMDBuild.model.CMReportAsComboItem._FIELDS.description,
				store: _CMCache.getReportComboStore()
			});

			this.forceFormatCheck = new Ext.form.field.Checkbox({
				flex: 1
			});

			this.forceFormatOptions = new CMDBuild.field.CMBaseCombo({
				store : new Ext.data.ArrayStore({
					autoDestroy: true,
					fields : [ 'value', 'text' ],
					data : [
						[ 'pdf', 'PDF' ],
						[ 'csv', 'CSV' ]
					]
				}),
				displayField: "text",
				valueField: "value",
				queryMode: "local",
				flex: 3
			});

			this.forceFormat = new Ext.form.FieldContainer({
				width: 300,
				fieldLabel: tr[me.self.WIDGET_NAME].fields.force,
				labelWidth: CMDBuild.LABEL_WIDTH,
				layout: 'hbox',
				items: [this.forceFormatCheck, this.forceFormatOptions]
			});

			// PresetGrid
				this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', {
					clicksToEdit: 1
				});

				this.presetGrid = Ext.create('Ext.grid.Panel', {
					title: tr[me.self.WIDGET_NAME].fields.presets,
					considerAsFieldToDisable: true,
					margin: '0 0 0 3',
					flex: 1,

					plugins: [this.gridEditorPlugin],

					columns: [
						{
							header: tr[me.self.WIDGET_NAME].presetGrid.attribute,
							dataIndex: CMDBuild.core.proxy.CMProxyConstants.NAME,
							editor: { xtype: 'textfield' },
							flex: 1
						},
						{
							header: tr[me.self.WIDGET_NAME].presetGrid.value,
							dataIndex: CMDBuild.core.proxy.CMProxyConstants.VALUE,
							editor: { xtype: 'textfield' },
							flex: 1
						},
						{
							xtype: 'checkcolumn',
							header: CMDBuild.Translation.readOnly,
							dataIndex: CMDBuild.core.proxy.CMProxyConstants.READ_ONLY,
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

			// defaultFields is inherited
			this.defaultFields.add(this.reportCode, this.forceFormat);

			Ext.apply(this, {
				layout: {
					type: "hbox"
				},
				items: [this.defaultFields, this.presetGrid]
			});
		},

		fillPresetWithData: function(data, readOnlyAttributes) {
			this.presetGrid.store.removeAll();

			if (!Ext.isEmpty(data)) {
				for (var key in data) {
					var recordConf = {};

					recordConf[CMDBuild.core.proxy.CMProxyConstants.NAME] = key;
					recordConf[CMDBuild.core.proxy.CMProxyConstants.VALUE] = data[key] || '';

					if (Ext.Array.contains(readOnlyAttributes, key)) {
						recordConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY] = true;
					} else {
						recordConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY] = false;
					}

					this.presetGrid.store.add(recordConf);
				}
			} else {
				var recordConf = {};

				recordConf[CMDBuild.core.proxy.CMProxyConstants.NAME] = '';
				recordConf[CMDBuild.core.proxy.CMProxyConstants.VALUE] = '';
				recordConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY] = false;

				this.presetGrid.store.add(recordConf);
			}
		},

		getPresetData: function() {
			var records = this.presetGrid.store.getRange();
			var data = {};
			var readOnly = [];

			for (var i = 0, l = records.length; i < l; ++i) {
				var recData = records[i].data;

				if (!Ext.isEmpty(recData[CMDBuild.core.proxy.CMProxyConstants.NAME]) && !Ext.isEmpty(recData[CMDBuild.core.proxy.CMProxyConstants.VALUE])) {
					data[recData[CMDBuild.core.proxy.CMProxyConstants.NAME]] = recData[CMDBuild.core.proxy.CMProxyConstants.VALUE];

					if (recData[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY])
						readOnly.push(recData[CMDBuild.core.proxy.CMProxyConstants.NAME]);
				}
			}

			return {
				data: data,
				readOnly: readOnly
			}
		},

		// override
		fillWithModel: function(model) {
			this.callParent(arguments);
			this.reportCode.setValue(model.get("reportCode"));

			var forceFormat = model.get("forceFormat");
			if (forceFormat) {
				this.forceFormatCheck.setValue(true);
				this.forceFormatOptions.setValue(forceFormat);
			}

			this.fillPresetWithData(model.get("preset"), model.get(CMDBuild.core.proxy.CMProxyConstants.READ_ONLY_ATTRIBUTES));
		},

		// override
		disableNonFieldElements: function() {
			this.presetGrid.disable();
		},

		// override
		enableNonFieldElements: function() {
			this.presetGrid.enable();
		},

		// override
		getWidgetDefinition: function() {
			var me = this;
			var returnObject = {};
			var presetData = me.getPresetData();

			returnObject['forceFormat'] = function() {
				if (me.forceFormatCheck.getValue()) {
					return me.forceFormatOptions.getValue();
				}
			};
			returnObject['reportCode'] = me.reportCode.getValue();
			returnObject['preset'] = presetData.data;
			returnObject[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY_ATTRIBUTES] = presetData.readOnly;

			return Ext.apply(me.callParent(arguments), returnObject);
		}
	});
})();