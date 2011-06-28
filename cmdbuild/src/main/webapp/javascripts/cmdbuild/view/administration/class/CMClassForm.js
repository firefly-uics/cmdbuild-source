(function() {
	var tr = CMDBuild.Translation.administration.modClass.classProperties;

	Ext.define("CMDBuild.view.administration.classes.CMClassForm", {
		extend : "Ext.panel.Panel",
		title : tr.title_add,
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},
		alias : "classform",
		defaultParent : "Class",
		layout : 'border',

		initComponent : function() {

			this.deleteButton = new Ext.button.Button( {
				iconCls : 'delete',
				text : tr.remove_class
			}),

			this.modifyButton = new Ext.button.Button( {
				iconCls : 'modify',
				text : tr.modify_class,
				handler: function() {
					this.enableModify()
				},
				scope: this
			}),

			this.saveButton = new Ext.button.Button( {
				text : CMDBuild.Translation.common.buttons.save
			});

			this.abortButton = new Ext.button.Button( {
				text : CMDBuild.Translation.common.buttons.abort
			});

			this.printClassButton = new CMDBuild.PrintMenuButton( {
				text : tr.print_class,
				formatList : [ 'pdf', 'odt' ]
			});

			this.inheriteCombo = new Ext.form.ComboBox( {
				fieldLabel : tr.inherits,
				name : 'parent',
				valueField : 'id',
				displayField : 'description',
				editable : false,
				cmImmutable : true,
				defaultParent : this.defaultParent,
				queryMode : "local",
				store : this.buildInheriteComboStore()
			});

			this.className = new Ext.form.field.Text( {
				fieldLabel : tr.name,
				name : 'name',
				allowBlank : false,
				vtype : 'alphanum',
				cmImmutable : true
			});

			this.classDescription = new Ext.form.field.Text( {
				fieldLabel : tr.description,
				name : 'text',
				allowBlank : false,
				vtype : 'cmdbcomment'
			});

			this.isSuperClass = new Ext.ux.form.XCheckbox( {
				fieldLabel : tr.superclass,
				name : 'superclass',
				cmImmutable : true
			});

			this.isActive = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.active,
				name : 'active'
			});

			var types = Ext.create('Ext.data.Store', {
				fields: ['value', 'name'],
				data : [
					{"value":"standard", "name":tr.standard},
					{"value":CMDBuild.Constants.cachedTableType.simpletable, "name":tr.simple}
				]
			});

			this.typeCombo = new Ext.form.field.ComboBox({
				fieldLabel : tr.type,
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

			this.form = new Ext.form.FormPanel( {
				region : "center",
				frame : true,
				border : true,
				defaultType : 'textfield',
				monitorValid : true,
				autoScroll : true,
				items : [
					this.className,
					this.classDescription,
					this.typeCombo,
					this.inheriteCombo,
					this.isSuperClass,
					this.isActive
				]
			});

			this.cmButtons = [ this.saveButton, this.abortButton ];
			this.cmTBar = [ this.modifyButton, this.deleteButton, this.printClassButton ];

			Ext.apply(this, {
			 	plugins : [new CMDBuild.FormPlugin()],
				border : false,
				frame : true,
				tbar : this.cmTBar,
				items : [ this.form ],
				buttonAlign : 'center',
				buttons : this.cmButtons
			});

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

		onClassSelected : function(selection) {
			this.getForm().loadRecord(selection);
			this.disableModify(enableCMTbar = true);
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
			this.inheriteCombo.setValue(_CMCache.getClassRootId())
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