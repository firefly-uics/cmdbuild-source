(function() {
	var tr = CMDBuild.Translation.administration.modClass.classProperties;

	Ext.define("CMDBuild.view.administration.classes.CMClassForm", {
		extend: "Ext.panel.Panel",

		alias: "classform",

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		defaultParent: "Class",
		layout: 'border',

		title: tr.title_add,

		// config
		/**
		 * set to false to deny
		 * the building of save and
		 * abort buttons
		 */
		whithSaveAndCancelButtons: true,
		// config

		initComponent : function() {
			this.plugins = [new CMDBuild.FormPlugin()];
			this.border = false;
			this.frame = false;
			this.cls = "x-panel-body-default-framed";
			this.bodyCls = 'cmdb-gray-panel';

			this.buildButtons();
			this.buildFormFields();
			this.buildItems();

			if (this.whithSaveAndCancelButtons) {
				this.buttonAlign = 'center';
				this.buttons = this.cmButtons;
			}

			this.tbar = this.cmTBar;

			this.callParent(arguments);

			this.typeCombo.on("select", onSelectType, this);
			this.className.on("change", function(fieldname, newValue, oldValue) {
				this.autoComplete(this.classDescription, newValue, oldValue);
			}, this);

			this.disableModify();
		},

		getForm : function() {
			return this.form.getForm();
		},

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel} selection
		 */
		onClassSelected: function(selection) {
			this.getForm().loadRecord(selection);

			this.disableModify(true);
		},

		onAddClassButtonClick: function() {
			this.reset();
			this.inheriteCombo.store.cmFill();
			this.enableModify(all=true);
			this.setDefaults();
		},

		setDefaults: function() {
			this.isActive.setValue(true);
			this.typeCombo.setValue("standard");
			this.inheriteCombo.setValue(_CMCache.getClassRootId());
		},

		buildButtons: function() {
			this.deleteButton = new Ext.button.Button( {
				iconCls : 'delete',
				text : tr.remove_class
			}),

			this.modifyButton = new Ext.button.Button( {
				iconCls: 'modify',
				text: tr.modify_class,
				handler: function() {
					this.enableModify();
				},
				scope: this
			}),

			this.printClassButton = Ext.create('CMDBuild.core.buttons.iconized.split.Print', {
				formatList: [
					CMDBuild.core.constants.Proxy.PDF,
					CMDBuild.core.constants.Proxy.ODT
				],
				mode: 'legacy',
				text: tr.print_class
			});

			if (this.whithSaveAndCancelButtons) {
				this.saveButton = new Ext.button.Button( {
					text : CMDBuild.Translation.common.buttons.save
				});

				this.abortButton = new Ext.button.Button( {
					text : CMDBuild.Translation.common.buttons.abort
				});

				this.cmButtons = [ this.saveButton, this.abortButton ];
			}

			this.cmTBar = [ this.modifyButton, this.deleteButton, this.printClassButton ];
		},

		// protected
		buildFormFields: function() {
			this.inheriteCombo = new Ext.form.ComboBox( {
				fieldLabel : tr.inherits,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name : 'parent',
				valueField : 'id',
				displayField : 'description',
				editable : false,
				cmImmutable : true,
				defaultParent : this.defaultParent,
				queryMode : "local",
				store : this.buildInheriteComboStore()
			});

			this.className = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr.name,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name: CMDBuild.core.constants.Proxy.NAME,
				allowBlank: false,
				vtype: 'alphanum',
				cmImmutable: true
			});

			this.classDescription = Ext.create('CMDBuild.view.common.field.translatable.Text', {
				name: CMDBuild.core.constants.Proxy.TEXT,
				fieldLabel: CMDBuild.Translation.descriptionLabel,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				allowBlank: false,
				vtype: 'commentextended',

				translationFieldConfig: {
					type: CMDBuild.core.constants.Proxy.CLASS,
					identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
					field: CMDBuild.core.constants.Proxy.DESCRIPTION
				}
			});

			this.isSuperClass = new Ext.ux.form.XCheckbox( {
				fieldLabel : tr.superclass,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				name : 'superclass',
				cmImmutable : true
			});

			this.isActive = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.active,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				name : 'active'
			});

			var types = Ext.create('Ext.data.Store', {
				fields: ['value', 'name'],
				data : [
					{"value":"standard", "name":tr.standard},
					{"value":'simpletable', "name":tr.simple}
				]
			});

			this.typeCombo = new Ext.form.field.ComboBox({
				fieldLabel : tr.type,
				labelWidth : CMDBuild.core.constants.FieldWidths.LABEL,
				width : CMDBuild.core.constants.FieldWidths.ADMINISTRATION_MEDIUM,
				name : 'tableType',
				hiddenName : 'tableType',
				valueField : 'value',
				displayField : 'name',
				editable : false,
				queryMode : "local",
				cmImmutable : true,
				store: types
			});

			this.typeCombo.setValue = Ext.Function.createInterceptor(this.typeCombo.setValue,
			onTypeComboSetValue, this);
		},

		// protected
		buildItems: function() {
			this.form = new Ext.form.FormPanel( {
				region: "center",
				frame: false,
				border: false,
				cls: "x-panel-body-default-framed",
				bodyCls: 'cmdb-gray-panel',
				defaultType: 'textfield',
				monitorValid: true,
				autoScroll: true,
				items: this.getFormItems()
			});

			this.items = [this.form];
		},

		// protected
		getFormItems: function() {
			return [
				this.className,
				this.classDescription,
				this.typeCombo,
				this.inheriteCombo,
				this.isSuperClass,
				this.isActive
			]
		},

		buildInheriteComboStore: function() {
			return _CMCache.getSuperclassesAsStore();
		}
	});

	function onSelectType(field, selections) {
		var s = selections[0];
		if (s) {
			onTypeComboSetValue.call(this, s.get("value"));
		}
	}

	function onTypeComboSetValue(value) {
		if (value == "simpletable") {
			this.isSuperClass.hide();
			this.inheriteCombo.hide();
		} else {
			this.isSuperClass.show();
			this.inheriteCombo.show();
		}
	}

})();