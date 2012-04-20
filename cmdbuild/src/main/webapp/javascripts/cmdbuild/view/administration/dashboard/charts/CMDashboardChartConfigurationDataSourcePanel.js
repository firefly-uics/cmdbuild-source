(function() {
	var tr = CMDBuild.Translation.administration.modDashboard.charts,

	_integerStore = new Ext.data.SimpleStore({
		fields: ["value", "description"],
		data: [
			["free", tr.typeFieldOptions.freeInt],
			["classes", tr.typeFieldOptions.classes],
			["lookup", tr.typeFieldOptions.lookup]
		]
	}),

	_stringStore = new Ext.data.SimpleStore({
		fields: ["value", "description"],
		data: [
			["free", tr.typeFieldOptions.freeString],
			["classes", tr.typeFieldOptions.classes],
			["lookup", tr.typeFieldOptions.lookup],
			["user", tr.typeFieldOptions.user],
			["group", tr.typeFieldOptions.group],
		]
	});

	Ext.define("CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationDataSourcePanel", {
		extend: "Ext.panel.Panel",

		inputFieldSets: [],

		afterComboValueChanged: Ext.emptyFn,

		afterInputFieldTypeChanged: Ext.emptyFn,

		initComponent: function() {

			this.dataSourceCombo = new Ext.form.field.ComboBox({
				name: "dataSource",
				fieldLabel: tr.fields.dataSource,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				valueField: "name",
				displayField: "name",
				queryMode: "local",
				editable: false,
				allowBlank: false,
				store: _CMCache.getAvailableDataSourcesStore(),
				disabled: true
			});

			Ext.apply(this, {
				items: [
					this.dataSourceCombo
				]
			});

			this.dataSourceCombo.setValue = Ext.Function.createSequence(this.dataSourceCombo.setValue, this.afterComboValueChanged);

			this.callParent(arguments);
		},

		setDataSourceInputFields: function(input) {
			this.removeDataSourceInputFields();
			var me = this;
			for (var i=0, l=input.length, item; i<l; ++i) {
				item = input[i];
				this.inputFieldSets.push(
					CMDBuild.view.administration.dashboard.
						_DataSourceInputFildSet.builders[item.type](item,
						this.afterInputFieldTypeChanged,
						function dataSourceComboIsDisabled() {
							return me.dataSourceCombo.isDisabled()
						})
				);
			}

			this.add(this.inputFieldSets);
		},

		removeDataSourceInputFields: function() {
			for (var i=0, l=this.inputFieldSets.length, item; i<l; ++i) {
				item = this.inputFieldSets[i];
				this.remove(item)
			}

			this.inputFieldSets = [];
		},

		getData: function() {
			var data = {
				name: this.dataSourceCombo.getValue(),
				input: []
			}

			this.items.each(function(item) {
				if (Ext.getClassName(item) == "CMDBuild.view.administration.dashboard._DataSourceInputFildSet") {
					data.input.push(item.getData())
				}
			});

			return data;
		},

		fillInputFieldsWith: function(inputItems) {
			inputItems = Ext.isArray(inputItems) ? inputItems : [inputItems];
			for (var i=0, l=inputItems.length, item; i<l; ++i) {
				item = inputItems[i];
				for (var j=0, len=this.inputFieldSets.length, fieldSet; j<len; j++) {
					fieldSet = this.inputFieldSets[j];
					if (Ext.getClassName(fieldSet) == "CMDBuild.view.administration.dashboard._DataSourceInputFildSet"
						&& fieldSet.input.name == item.name) {

						fieldSet.setData(item);
						break;
					}
				};
			}
		}
	});

	var SUBFIELD_LABEL_WIDTH = CMDBuild.LABEL_WIDTH - 15;

	Ext.define("CMDBuild.view.administration.dashboard._DataSourceInputFildSet", {

		extend: "Ext.form.FieldSet",

		statics: {
			builders: {
				date: function(input, afterInputFieldTypeChanged, typeComboIsdisabled) {
					return new CMDBuild.view.administration.dashboard._DataSourceInputFildSet({
						input: input,
						typeComboIsdisabled: typeComboIsdisabled,
						afterInputFieldTypeChanged: afterInputFieldTypeChanged
					});
				},
		
				integer: function(input, afterInputFieldTypeChanged, typeComboIsdisabled) {
					return new CMDBuild.view.administration.dashboard._DataSourceInputFildSet({
						input: input,
						typeComboIsdisabled: typeComboIsdisabled,
						fieldTypeStore: _integerStore,
						afterInputFieldTypeChanged: afterInputFieldTypeChanged
					});
				},
		
				string: function(input, afterInputFieldTypeChanged, typeComboIsdisabled) {
					return new CMDBuild.view.administration.dashboard._DataSourceInputFildSet({
						input: input,
						typeComboIsdisabled: typeComboIsdisabled,
						fieldTypeStore: _stringStore,
						afterInputFieldTypeChanged: afterInputFieldTypeChanged
					});
				}
			}
		},

		// callback definid on instantiation
		afterInputFieldTypeChanged: Ext.emptyFn,

		initComponent: function() {

			if (!this.input) {
				return;
			}

			var me = this,
				name = me.input.name,
				type = me.input.type;

			if (me.fieldTypeStore) {
				me.fieldType = new Ext.form.field.ComboBox({
					fieldLabel: tr.fields.fieldType,
					labelWidth: SUBFIELD_LABEL_WIDTH,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					valueField: "value",
					displayField: "description",
					queryMode: "local",
					editable: false,
					allowBlank: false,
					store: me.fieldTypeStore,
					disabled: me.typeComboIsdisabled()
				});

				me.fieldType.setValue = Ext.Function.createSequence(me.fieldType.setValue,
					function(value) {
						me.afterInputFieldTypeChanged(value, me);
					}
				);

				me.items = [me.fieldType];
			} else {
				// is a input of type date. It hasn't a
				// combo to select the field, has only the
				// default that is a datefield

				me.defaultField = new Ext.form.field.Date({
					format : 'd/m/Y',
					fieldLabel: tr.fields.defaultValue,
					labelWidth: SUBFIELD_LABEL_WIDTH,
					width: CMDBuild.ADM_MEDIUM_FIELD_WIDTH,
					disabled: me.typeComboIsdisabled()
				});

				me.items = [me.defaultField];
			}

			me.baseCls = "cmfieldset";
			me.title = name + " (" + tr.inputTypes[type] + ")";
			me.callParent(arguments);
		},

		addTextFieldForDefault: function() {
			this.resetFieldset();

			this.defaultField = new Ext.form.field.Text({
				fieldLabel: tr.fields.defaultValue,
				labelWidth: SUBFIELD_LABEL_WIDTH,
				disabled: this.typeComboIsdisabled()
			});

			this.add(this.defaultField);
		},

		addLookupFieldForDefault: function(type) {
			this.defaultField = CMDBuild.Management.LookupCombo.build({
				description: tr.fields.defaultValue,
				fieldmode: "write",
				lookup: type,
				disabled: this.typeComboIsdisabled(),
				lookupchain: _CMCache.getLookupchainForType(type)
			});

			if (Ext.getClassName(this.defaultField) == "CMDBuild.field.LookupCombo") {
				this.defaultField.labelWidth = SUBFIELD_LABEL_WIDTH;
				this.defaultField.labelAlign = "left";
			} else if (Ext.getClassName(this.defaultField) == "CMDBuild.field.MultiLevelLookupPanel") {
				this.defaultField.items.each(function(f, index) {
					if (index == 0) {
						f.labelWidth = SUBFIELD_LABEL_WIDTH;
						f.labelAlign = "left";
					} else  {
						f.padding = "0 0 0 " + (SUBFIELD_LABEL_WIDTH + 5);
					}
				});
			}

			this.add(this.defaultField);
		},

		addClassesFieldForDefault: function() {
			this.resetFieldset();

			this.defaultField = new CMDBuild.field.ErasableCombo({
				fieldLabel : tr.fields.defaultValue,
				labelWidth: SUBFIELD_LABEL_WIDTH,
				valueField : 'id',
				displayField : 'description',
				editable: false,
				store : _CMCache.getClassesAndProcessesStore(),
				queryMode: 'local',
				disabled: this.typeComboIsdisabled()
			});

			this.add(this.defaultField);
		},

		addLookupTypesField: function() {
			var me = this;

			this.resetFieldset();

			this.lookupTypeField = new CMDBuild.field.ErasableCombo({
				fieldLabel: tr.fields.lookupType,
				labelWidth: SUBFIELD_LABEL_WIDTH,
				queryMode : 'local',
				displayField : 'type',
				valueField : 'type',
				store: _CMCache.getLookupTypeAsStore(),
				disabled: me.typeComboIsdisabled()
			})

			this.lookupTypeField.setValue = Ext.Function.createSequence(this.lookupTypeField.setValue, function(v) {
				if (Ext.isArray(v)) {
					v = v[0];
				}

				if (typeof v.get == "function") {
					v = v.get("type");
				}

				if (me.defaultField) {
					me.remove(me.defaultField);
				}
				me.addLookupFieldForDefault(v);
			});

			this.add(this.lookupTypeField);
		},

		resetFieldset: function() {
			if (!this.fieldType) {
				return;
			}

			if (this.defaultField) {
				this.remove(this.defaultField);
			}

			if (this.lookupTypeField) {
				this.remove(this.lookupTypeField);
			}
		},

		getData: function() {
			var data = {
				name: this.input.name,
				type: this.input.type
			}

			if (this.fieldType) {
				data.fieldType = this.fieldType.getValue();
			}

			if (this.defaultField) {
				if (Ext.getClassName(this.defaultField) == "Ext.form.field.Date") {
					data.defaultValue = this.defaultField.getRawValue();
				} else {
					data.defaultValue = this.defaultField.getValue();
				}
			}

			if (this.lookupTypeField) {
				data.lookupType = this.lookupTypeField.getValue();
			}

			return data;
		},

		setData: function(data) {
			if (data.fieldType && this.fieldType) {
				this.fieldType.setValue(data.fieldType);
				if (data.lookupType && this.lookupTypeField) {
					this.lookupTypeField.setValue(data.lookupType);
				}
			}

			if (data.defaultValue && this.defaultField) {
				this.defaultField.setValue(data.defaultValue);
			}
		}
	});
})();
