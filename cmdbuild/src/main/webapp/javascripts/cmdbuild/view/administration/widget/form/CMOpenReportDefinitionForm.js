(function() {

	Ext.define("CMDBuild.view.administration.widget.form.CMOpenReportDefinitionPresetGrid", {
		extend: "Ext.grid.Panel",
		frame: false,
		flex: 1,
		initComponent: function() {
			this.cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit : 1
			});
			var widgetName = CMDBuild.view.administration.widget.form.CMOpenReportDefinitionForm.WIDGET_NAME,
				tr = CMDBuild.Translation.administration.modClass.widgets[widgetName].presetGrid;

			Ext.apply(this, {
				columns: [{
					header: tr.attribute,
					dataIndex : CMDBuild.model.CMReportAttribute._FIELDS.name,
					flex: 1
				},{
					header: tr.value,
					dataIndex: CMDBuild.model.CMReportAttribute._FIELDS.value,
					editor: {
						xtype: "textfield"
					},
					flex: 1
				}],
				store: new Ext.data.Store({
					model: "CMDBuild.model.CMReportAttribute",
					data: []
				}),
				plugins: [this.cellEditing]
			});

			this.callParent(arguments);
		},

		fillWithData: function(data) {
			this.store.removeAll();
			if (data) {
				var fields = CMDBuild.model.CMReportAttribute._FIELDS;

				for (var key in data) {
					var recordConf = {};
					recordConf[fields.name] = key;
					recordConf[fields.value] = data[key] || "";

					this.store.add(new CMDBuild.model.CMReportAttribute(recordConf));
				}
			}
		},

		count: function() {
			return this.store.count();
		},

		getData: function() {
			var records = this.store.getRange(),
				fields = CMDBuild.model.CMReportAttribute._FIELDS,
				data = {};

			for (var i=0, l=records.length; i<l; ++i) {
				var recData = records[i].data;
				data[recData[fields.name]] = recData[fields.value];
			}

			return data;
		}
	});

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
			var tr = CMDBuild.Translation.administration.modClass.widgets;
			var me = this;

			this.reportCode = new CMDBuild.field.CMBaseCombo({
				name: "code",
				fieldLabel: tr[me.self.WIDGET_NAME].fields.report,
				labelWidth: CMDBuild.LABEL_WIDTH,
				valueField: CMDBuild.model.CMReportAsComboItem._FIELDS.value,
				displayField: CMDBuild.model.CMReportAsComboItem._FIELDS.description,
				store: _CMCache.getReportComboStore()
			});

			this.buttonLabel = new Ext.form.Field({
				name: "label",
				fieldLabel: tr.commonFields.buttonLabel,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			this.active = new Ext.form.field.Checkbox({
				name: "active",
				fieldLabel: tr.commonFields.active,
				labelWidth: CMDBuild.LABEL_WIDTH
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

			this.presetGrid = new CMDBuild.view.administration.widget.form.CMOpenReportDefinitionPresetGrid({
				title: tr[me.self.WIDGET_NAME].fields.presets
			});

			Ext.apply(this, {
				layout: "hbox",
				items: [{
					xtype: "panel",
					frame: true,
					border: true,
					items: [this.buttonLabel, this.reportCode, this.forceFormat, this.active],
					margins: "0 5 0 0",
					flex: 1
				}, this.presetGrid]
			});
		},

		fillPresetWithData: function(data) {
			this.presetGrid.fillWithData(data);
		},

		fillWithModel: function(model) {
			this.buttonLabel.setValue(model.get("label"));
			this.reportCode.setValue(model.get("reportCode"));
			this.active.setValue(model.get("active"));

			var forceFormat = model.get("forceFormat");
			if (forceFormat) {
				this.forceFormatCheck.setValue(true);
				this.forceFormatOptions.setValue(forceFormat);
			}

			this.fillPresetWithData(model.get("preset"));
		},

		disableNonFieldElements: function() {
			this.presetGrid.disable();
		},

		enableNonFieldElements: function() {
			this.presetGrid.enable();
		},

		getWidgetDefinition: function() {
			var me = this;
			return {
				type: me.self.WIDGET_NAME,
				label: me.buttonLabel.getValue(),
				forceFormat: (function() {
					if (me.forceFormatCheck.getValue()) {
						return me.forceFormatOptions.getValue();
					}
				})(),
				reportCode: me.reportCode.getValue(),
				active: me.active.getValue(),
				preset: me.presetGrid.getData()
			};
		}
	});
})();