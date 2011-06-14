(function() {
	var tableTypeMap = {
		simpletable: "SIMPLECLASS",
		standard: "CLASS"
	};

	var tr =  CMDBuild.Translation.administration.modClass.attributeProperties;

	Ext.define("CMDBuild.view.administration.classes.CMAttributeForm", {
		extend: "Ext.form.Panel",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		constructor:function() {

			this.modifyButton = new Ext.button.Button({
				iconCls : "modify",
				text : tr.modify_attribute,
				scope : this,
				handler: function() {
					this.enableModify();
				}
			});

			this.deleteButton = new Ext.button.Button({
				iconCls : "delete",
				text : tr.delete_attribute
			});

			this.saveButton = new CMDBuild.buttons.SaveButton();
			this.abortButton = new CMDBuild.buttons.AbortButton();

			this.cmTBar = [this.modifyButton, this.deleteButton];
			this.cmButtons = [this.saveButton, this.abortButton];

			this.fieldMode = new Ext.form.ComboBox({ 
				name: "fieldmode",
				fieldLabel: tr.field_visibility,
				valueField: "value",
				displayField: "name",
				hiddenName: "fieldmode",
				queryMode: "local",
				editable: false,
				allowBlank: false,
				store: new Ext.data.SimpleStore({
					fields: ["value","name"],
					data : [
						["write",tr.field_write],
						["read",tr.field_read],
						["hidden",tr.field_hidden]
					]
				})
			});

			this.attributeGroup = new Ext.form.ComboBox({
				name: "group",
				fieldLabel: tr.group,
				valueField: "value",
				displayField: "value",
				hiddenName: "group",
				queryMode: "local",
				editable: true,
				allowBlank: true,
				store: new Ext.data.SimpleStore({
					fields: ["value"],
					data : []
				})
			});

			this.attributeName = new Ext.form.TextField( {
				fieldLabel : tr.name,
				name : "name",
				allowBlank : false,
				vtype : "alphanum",
				cmImmutable : true
			});

			this.attributeDescription = new Ext.form.TextField({
				fieldLabel : tr.description,
				name : "description",
				allowBlank : false,
				vtype : "cmdbcomment"
			});

			this.attributeNotNull = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.isnotnull,
				name : "isnotnull"
			});

			this.attributeUnique = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.isunique,
				name : "isunique"
			});

			this.isBasedsp = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.isbasedsp,
				name : "isbasedsp"
			});

			this.isActive = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.isactive,
				name : "isactive"
			});

			this.attributeTypeStore = new Ext.data.JsonStore({
				autoLoad : false,
				fields : ["name", "value"],
				proxy: {
					type: 'ajax',
					url : "services/json/schema/modclass/getattributetypes",
					reader: {
						type: 'json',
						root : "types"
					}
				},
				sorters: {
					property: 'value',
					direction: 'ASC'
				}
			});

			this.comboType = new Ext.form.ComboBox({
				plugins: [new CMDBuild.SetValueOnLoadPlugin()],
				fieldLabel : tr.type,
				name : "type",
				triggerAction : "all",
				valueField : "name",
				displayField : "value",
				allowBlank : false,
				editable: false,
				cmImmutable: true,
				queryMode: "local",
				store : this.attributeTypeStore
			});

			this.stringLength = new Ext.form.NumberField({
				fieldLabel : tr.length,
				minValue : 1,
				maxValue : Math.pow(2, 31) - 1,
				name : "len",
				allowBlank : false
			});

			this.decimalPrecision = new Ext.form.NumberField({
				fieldLabel : tr.precision,
				minValue : 1,
				maxValue : 20,
				name : "precision",
				allowBlank : false
			});

			this.fieldFilter = new Ext.form.TextArea( {
				fieldLabel : tr.referencequery,
				name : "fieldFilter",
				allowBlank : true,
				vtype : "cmdbcommentrelaxed",
				invalidText : tr.pipeNotAllowed,
				editableOnInherited : true
			});

			this.addMetadataBtn = new Ext.Button( {
				text : tr.meta.title,
				scope : this,
				iconCls : "modify",
				handler : function() {
					new CMDBuild.Administration.MetadataWindow({
						meta : this.record.data.meta,
						ns : "system.template.",
						owner : this
					}).show();
				}
			});

			this.decimalScale = new Ext.form.NumberField( {
				fieldLabel : tr.scale,
				minValue : 1,
				maxValue : 20,
				name : "scale",
				allowBlank : false
			});

			this.lookupTypes = new Ext.form.ComboBox({
				plugins: [new CMDBuild.SetValueOnLoadPlugin()],
				fieldLabel : tr.lookup,
				name : "lookup",
				valueField : "type",
				displayField : "type",
				allowBlank : false,
				cmImmutable : true,
				store : _CMCache.getLookupTypeLeavesAsStore(),
				queryMode : "local"
			});

			this.domainStore = new Ext.data.Store({
				autoLoad: false,
				model : "CMDomainModelForGrid",
				proxy: {
					type: 'ajax',
					url : "services/json/schema/modclass/getreferenceabledomainlist",
					reader: {
						type: 'json',
						root : "rows"
					}
				},
				sorters: {
					property: 'description',
					direction: 'ASC'
				}
			});

			this.referenceDomains = new Ext.form.ComboBox({
				plugins: [new CMDBuild.SetValueOnLoadPlugin()],
				fieldLabel : tr.domain,
				name : "idDomain",
				valueField : "idDomain",
				displayField : "description",
				allowBlank : false,
				cmImmutable : true,
				store: this.domainStore,
				queryMode : "local"
			});

			this.foreignKeyDest = new Ext.form.ComboBox( {
				plugins: [new CMDBuild.SetValueOnLoadPlugin()],
				fieldLabel : tr.destination,
				name : "fkDestination",
				hiddenName : "fkDestination",
				valueField : "id",
				displayField : "description",
				editable : false,
				allowBlank : false,
				cmImmutable : true,
				queryMode : "local",
				//TODO 3 to 4 add the Process
				store : _CMCache.getClassesStore()
			});

			this.contextualFields = {
				STRING : [ this.stringLength ],
				DECIMAL : [ this.decimalPrecision,this.decimalScale ],
				LOOKUP : [ this.lookupTypes ],
				FOREIGNKEY : [ this.foreignKeyDest ],
				REFERENCE : [ this.referenceDomains, this.fieldFilter, this.addMetadataBtn ]
			};

			this.buildBasePropertiesPanel();

			this.specificProperties = new Ext.form.FieldSet({
				margin: "0 0 5 5",
				title : tr.typeProperties,
				autoScroll : true,
				defaultType : "textfield",
				flex: 1,
				items : [
					this.comboType,
					this.stringLength,
					this.decimalPrecision,
					this.decimalScale,
					this.referenceDomains,
					this.foreignKeyDest,
					this.lookupTypes,
					this.fieldFilter,
					this.addMetadataBtn
				]
			});
			this.plugins = [new CMDBuild.FormPlugin()];
			this.callParent(arguments);
		},

		initComponent: function() {
			Ext.apply(this, {
				frame: true,
				border: false,
				buttons: this.cmButtons,
				layout: "border",
				tbar: this.cmTBar,
				items: [{
					xtype: "panel",
					region: "center",
					layout: {
						type: 'hbox',
						align: 'stretch'
					},
					frame: true,
					items: [this.baseProperties, this.specificProperties]
				}]
			});

			this.callParent(arguments);

			this.comboType.on("select", onSelectComboType, this);
			this.attributeGroup.on("enable", fillAttributeGroupsStore, this);

			this.attributeName.on("change", function(fieldname, newValue, oldValue) {
				this.autoComplete(this.attributeDescription, newValue, oldValue);
			}, this);
		},

		onClassSelected: function(idClass) {
			this.idClass = idClass;

			var classObj = _CMCache.getClassById(idClass);
			if (classObj) {

				this.domainStore.load({
					params : {
						idClass : idClass
					}
				});

				this.attributeTypeStore.load({
					params : {
						tableType : tableTypeMap[classObj.get("tableType")]
					}
				});

				this.hideContextualFields();
		}

//	  var isSuperClass = this.isSuperclass();
//	  this.attributeUnique.initialConfig.cmImmutable = isSuperClass;
//	  this.attributeNotNull.initialConfig.cmImmutable = isSuperClass;
//	  this.formPanel.initForm();
		},

		onAttributeSelected : function(attribute) {
			this.reset();
			
			if (attribute) {
				this.getForm().setValues(attribute.raw);
				this.disableModify(enableCMTbar = true);
				this.deleteButton.setDisabled(attribute.get("inherited"));
				this.hideContextualFields();
				this.showContextualFieldsByType(attribute.get("type"));
	
				// I want send these value only after a modify
				// in the meta data window
				this.getForm().findField("meta").setValue("");
			}
		},

		iterateOverContextualFields: function(type, fn) {
			var typeFields = this.contextualFields[type];
			if (typeFields) {
				for (var i=0, len=typeFields.length; i<len; i++) {
					fn(typeFields[i]);
				}
			}
		},

		showContextualFieldsByType: function(type) {
			this.iterateOverContextualFields(type, function(field) {
				field.show();
			});
		},

		showAndEnableContextualFieldsByType: function(type) {
			this.iterateOverContextualFields(type, function(field) {
				field.show();
				field.enable();
			});
		},

		hideContextualFields: function() {
			for (var type in this.contextualFields) {
				this.iterateOverContextualFields(type, function(field) {
					field.hide();
					field.disable();
				});
			}
		},

		onAddAttributeClick : function(params) {
			this.reset();
			
//			if (this.isSuperclass()) {
//				this.attributeUnique.disable();
//				this.attributeNotNull.disable();
//			}
			
			this.setDefaultValues();
			this.hideContextualFields();
			this.enableModify(all = true);
		},

		setDefaultValues: function() {
			this.isActive.setValue(true);
			this.isBasedsp.setValue(true);
			this.fieldMode.setValue("write");
		},

		buildBasePropertiesPanel: function() {
			this.baseProperties = new Ext.form.FieldSet({
				title : tr.baseProperties,
				autoScroll : true,
				defaultType : "textfield",
				flex: 1,
				items : [
					this.attributeName,
					this.attributeDescription,
					this.attributeGroup,
					this.isBasedsp,
					this.attributeUnique,
					this.attributeNotNull,
					this.isActive,
					{
						xtype: "hidden",
						name: "meta"
					},
					this.fieldMode
				]
			});
		},

		isSuperclass : function() {
			if (this.cachedTable) {
				return this.cachedTable.superclass;
			} else {
				return false;
			}
		}

	});
	
	function onSelectComboType (combo, record, index) {
		var type = record[0].data.value;
		this.hideContextualFields();
		this.showAndEnableContextualFieldsByType(type);
	}
	
	function fillAttributeGroupsStore(combo) {
			var idClass = this.idClass;
			var store = combo.store;

			var cb = function(attributes) {
				store.removeAll();
				var addtributesGroup = {};
				for (var i=0, len=attributes.length; i<len; ++i) {
					var  attribute = attributes[i];
					if (attribute.group) {
						addtributesGroup[attribute.group] = true;
					};
				}
				var groups = [];
				for (var g in addtributesGroup) {
					groups.push([g]);
				}
				store.loadData(groups);
			};

			_CMCache.getAttributeList(idClass, cb);
		}
})();