(function() {
	var tr = CMDBuild.Translation.administration.modClass.classProperties;

	Ext.define("CMDBuild.view.administration.classes.CMClassForm", {
		extend : "Ext.panel.Panel",
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
				store : _CMCache.getSuperclassesAsStore()
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

			this.typeCombo.setValue = Ext.Function.createInterceptor(this.typeCombo.setValue, function(value) {
				if (value == "simpletable") {
					this.isSuperClass.hide();
					this.inheriteCombo.hide();
				} else {
					this.isSuperClass.show();
					this.inheriteCombo.show();
				}
			}, this);

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
				title : tr.title_add,
				border : false,
				frame : true,
				tbar : this.cmTBar,
				items : [ this.form ],
				buttonAlign : 'center',
				buttons : this.cmButtons
			});

			this.callParent(arguments);
		},

		getForm : function() {
			return this.form.getForm();
		},

		onSelectClass : function(selection) {
			this.getForm().loadRecord(selection);
			this.disableModify(enableCMTbar = true);
		},

		onAddClassButtonClick: function() {
			this.reset();
			this.enableModify(all=true);
			this.setDefaults();
		},

		setDefaults: function() {
			this.isActive.setValue(true);
			this.typeCombo.setValue("standard");
			this.inheriteCombo.setValue(_CMCache.getClassRootId())
		}

	});

})();