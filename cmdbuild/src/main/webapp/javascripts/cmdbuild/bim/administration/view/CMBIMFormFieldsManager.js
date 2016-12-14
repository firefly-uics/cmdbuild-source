(function() {

	Ext.define('CMDBuild.delegate.administration.bim.CMBIMFormFieldsManager', {

		/**
		 * @return (Array) an array of Ext.component to use as form items
		 */
		build: function() {
			this.name = new Ext.form.TextField({
				fieldLabel: CMDBuild.Translation.administration.modClass.attributeProperties.name,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name: CMDBuild.core.constants.Proxy.NAME,
				allowBlank: false,
				vtype: "alphanum",
				cmImmutable: true
			});

			this.description= new Ext.form.TextField({
				fieldLabel : CMDBuild.Translation.administration.modClass.attributeProperties.description,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name: CMDBuild.core.constants.Proxy.DESCRIPTION,
				allowBlank : false,
				vtype: 'comment'
			});

			this.activeCheckBox = Ext.create('Ext.form.field.Checkbox', {
				fieldLabel: CMDBuild.Translation.active,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				uncheckedValue: false,
				inputValue: true,
				name: 'active'
			});

			this.fileField = Ext.create('Ext.form.field.File', {
				fieldLabel: CMDBuild.Translation.ifc_file,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name: 'fileIFC'
			});

			this.importIfcButton = Ext.create('Ext.button.Button', {
				iconCls: 'import',
				text: CMDBuild.Translation.bimMod.administration.projects.importIfc
			});

			this.cardBinding = new CMDBuild.Administration.bim.BindCardFieldsetItem({});

			var fields = [this.name, this.description, this.activeCheckBox, this.fileField, this.cardBinding, this.importIfcButton];

			return fields;
		},

		/**
		 * @param (Ext.data.Model) record - record used to fill the field values
		 */
		loadRecord: function(record) {
			this.reset();
			this.name.setValue(record.get(CMDBuild.core.constants.Proxy.NAME));
			this.description.setValue(record.get(CMDBuild.core.constants.Proxy.DESCRIPTION));
			this.activeCheckBox.setValue(record.get('active'));
			this.cardBinding.setValue(record.get('cardBinding'));
		},

		/**
		 * @return (Object) values - a key/value map with the values of the fields
		 */
		getValues: function() {
			var values = {};
			values[CMDBuild.core.constants.Proxy.NAME] = this.name.getValue();
			values[CMDBuild.core.constants.Proxy.DESCRIPTION] = this.description.getValue();
			values['active'] = this.activeCheckBox.getValue();
			var ret = '';
			if (this.cardBinding) {
				ret = this.cardBinding.getValue();
			}
			values['card'] = ret;
			return values;
		},

		/**
		 * Clear the values of his fields
		 */
		reset: function() {
			this.name.reset();
			this.description.reset();
			this.activeCheckBox.reset();
			this.fileField.reset();
			this.cardBinding.setValue('');
		},

		enableFileField: function() {
			this.fileField.enable();
		},

		getImportIfcButton: function() {
			return this.importIfcButton;
		}
	});

	Ext.define('CMDBuild.Administration.bim.BindCardFieldsetItem', {
		extend: 'Ext.container.Container',

		rootClassName: undefined,
		delegate: null, // pass it on creation

		cls: 'x-panel-body-default-framed cmdb-border-top',
		itemId: 'bimCardBinding',
		layout: 'hbox',
		padding: '10 0 0 0',

		constructor: function(view) {
			this.callParent(arguments);
			// This must be loaded with BIM configuration before to initialize the application
		},

		setValue: function(value) {
			if (value <= 0)
				value = '';

			if (this.cardCombo)
				this.cardCombo.setValue(value);
		},

		getValue: function() {
			var ret = '';

			if (this.cardCombo)
				ret = this.cardCombo.getValue();

			return ret;
		},

		initializeItems: function(rootClassName) {
			this.remove(this.className);
			this.remove(this.cardCombo);
			delete this.className;
			delete this.cardCombo;

			if (!rootClassName)
				return;

			this.rootClassName = rootClassName;

			this.className = Ext.create('Ext.form.field.Text', {
				margin: '0 5 0 0',
				fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.card_binding,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				disabled: true,
				cmImmutable: true,
				value: rootClassName
			});

			if (this.rootClassName) {
				this.cardCombo = CMDBuild.Management.ReferenceField.build(
					{
						name: 'card',
						referencedClassName: this.rootClassName,
						isnotnull: true
					},
					null,
					{
						margin: '0 5 0 0',
						gridExtraConfig: {
							cmAdvancedFilter: false,
							cmAddGraphColumn: false,
							cmAddPrintButton: false
						},
						searchWindowReadOnly: true
					}
				);

				Ext.apply(this.cardCombo, {
					allowBlank: true
				});

			} else {
				this.cardCombo = undefined;
			}

			this.insert(0, this.className);
			this.className.setDisabled(true);

			if (this.cardCombo) {
				this.cardCombo.fieldLabel = '';
				this.cardCombo.labelAlign = 'right';
				this.cardCombo.labelWidth = 0;
				this.insert(1, this.cardCombo);
				this.cardCombo.setDisabled(true);
			}
		}
	});

})();