(function() {
	var tableTypeMap = {
		simpletable: "SIMPLECLASS",
		standard: "CLASS"
	},
	
	TEXT_EDITOR_TYPE = {
		plain: "PLAIN",
		html: "HTML"
	};

	function getTableType(classObj) {
		return tableTypeMap[classObj.get("tableType")];
	}

	function cannotHaveUniqueAttributes(classObj) {
		return isSuperClass(classObj) || isSimpleClass(classObj);
	}

	function cannotHaveNotNullAttributes(classObj) {
		return isSuperClass(classObj);
	}

	function isSuperClass(classObj) {
		return classObj && classObj.get("superclass");
	}

	function isSimpleClass(classObj) {
		return getTableType(classObj) == tableTypeMap.simpletable;
	}

	var tr =  CMDBuild.Translation.administration.modClass.attributeProperties;
	
	// FIXME this take a store given from the cache. The model set the valueField as a
	// string and it works for the other combo. For this the data have the valueField as
	// integer, so the set value does not work (the values are taken from the attributes grid)
	Ext.define("CMDBuild.FkCombo", {
		extend: "Ext.form.ComboBox",
		setValue: function(v) {
			if (typeof v == "number") {
				v = ""+v;
			}

			this.callParent([v]);
		}
	});

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
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.MIDDLE_FIELD_WIDTH,
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
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.MIDDLE_FIELD_WIDTH,
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
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : "name",
				allowBlank : false,
				vtype : "alphanum",
				cmImmutable : true
			});

			this.attributeDescription = new Ext.form.TextField({
				fieldLabel : tr.description,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : "description",
				allowBlank : false,
				vtype : "cmdbcomment"
			});

			this.attributeNotNull = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.isnotnull,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : "isnotnull"
			});

			this.attributeUnique = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.isunique,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : "isunique"
			});

			this.isBasedsp = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.isbasedsp,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : "isbasedsp"
			});

			this.isActive = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.isactive,
				labelWidth: CMDBuild.LABEL_WIDTH,
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
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : "type",
				triggerAction : "all",
				valueField : "name",
				displayField : "value",
				allowBlank : false,
				editable: false,
				cmImmutable: true,
				queryMode: "local",
				store : this.attributeTypeStore,
				listConfig: {
					loadMask: false
				}
			});

			this.stringLength = new Ext.form.NumberField({
				fieldLabel : tr.length,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_SMALL_FIELD_WIDTH,
				minValue : 1,
				maxValue : Math.pow(2, 31) - 1,
				name : "len",
				allowBlank : false
			});

			this.decimalPrecision = new Ext.form.NumberField({
				fieldLabel : tr.precision,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_SMALL_FIELD_WIDTH,
				minValue : 1,
				maxValue : 20,
				name : "precision",
				allowBlank : false
			});

			this.fieldFilter = new Ext.form.TextArea( {
				fieldLabel : tr.referencequery,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : "fieldFilter",
				allowBlank : true,
				vtype : "cmdbcommentrelaxed",
				invalidText : tr.pipeNotAllowed,
				editableOnInherited : true
			});

			this.referenceFilterMetadata = {};
			this.referenceFilterMetadataDirty = false;

			this.addMetadataBtn = new Ext.Button( {
				text : tr.meta.title,
				scope : this,
				iconCls : "modify",
				margin: "0 0 0 155",
				handler : function() {
					var w = new CMDBuild.view.administration.classes.CMMetadataWindow({
						data : this.referenceFilterMetadata,
						dirtyFlag: this.referenceFilterMetadataDirty,
						ns : "system.template."
					});

					this.mon(w.saveBtn, "click", function() {
						this.referenceFilterMetadata = w.getMetaAsMap();
						this.referenceFilterMetadataDirty = true;

						w.destroy();
					}, this);

					w.show();
				}
			});

			this.decimalScale = new Ext.form.NumberField( {
				fieldLabel : tr.scale,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_SMALL_FIELD_WIDTH,
				minValue : 1,
				maxValue : 20,
				name : "scale",
				allowBlank : false
			});

			this.lookupTypes = new Ext.form.ComboBox({
				plugins: [new CMDBuild.SetValueOnLoadPlugin()],
				fieldLabel : tr.lookup,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
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
				model : "CMDomainModelForCombo",
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
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : "idDomain",
				valueField : "idDomain",
				displayField : "description",
				allowBlank : false,
				cmImmutable : true,
				store: this.domainStore,
				queryMode : "local",
				listConfig: {
					loadMask: false
				}
			});

			this.foreignKeyDest = new CMDBuild.FkCombo( {
				plugins: [new CMDBuild.SetValueOnLoadPlugin()],
				fieldLabel : tr.destination,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : "fkDestination",
				hiddenName : "fkDestination",
				valueField : "id",
				displayField : "description",
				editable : false,
				allowBlank : false,
				cmImmutable : true,
				queryMode : "local",
				store : _CMCache.getClassesAndProcessesStore()
			});

			this.textAttributeWidget = new Ext.form.ComboBox({
				name: "editorType",
				fieldLabel: CMDBuild.Translation.administration.modClass.attributeProperties.editorType.label,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.MIDDLE_FIELD_WIDTH,
				valueField: "value",
				displayField: "name",
				queryMode: "local",
				editable: false,
				allowBlank: false,
				store: new Ext.data.SimpleStore({
					fields: ["value","name"],
					data : [
						[TEXT_EDITOR_TYPE.plain, CMDBuild.Translation.administration.modClass.attributeProperties.editorType.plain],
						[TEXT_EDITOR_TYPE.html, CMDBuild.Translation.administration.modClass.attributeProperties.editorType.html]
					]
				})
			});

			this.contextualFields = {
				STRING : [ this.stringLength ],
				DECIMAL : [ this.decimalPrecision,this.decimalScale ],
				LOOKUP : [ this.lookupTypes ],
				FOREIGNKEY : [ this.foreignKeyDest ],
				REFERENCE : [ this.referenceDomains, this.fieldFilter, this.addMetadataBtn ],
				TEXT: [this.textAttributeWidget]
			};

			this.buildBasePropertiesPanel();

			this.specificProperties = new Ext.form.FieldSet({
				margin: "0 0 5 5",
				padding: "5 5 20 5",
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
					this.addMetadataBtn,
					this.textAttributeWidget
				]
			});

			this.plugins = [new CMDBuild.FormPlugin()];
			this.callParent(arguments);
		},

		initComponent: function() {
			Ext.apply(this, {
				frame: false,
				border: false,
				cls: "x-panel-body-default-framed cmbordertop",
				bodyCls: 'cmgraypanel',
				buttonAlign: "center",
				buttons: this.cmButtons,
				tbar: this.cmTBar,
				layout: {
					type: 'hbox',
					align:'stretch'
				},
				items: [this.baseProperties, this.specificProperties]
			});

			this.callParent(arguments);

			this.comboType.on("select", onSelectComboType, this);

			this.attributeName.on("change", function(fieldname, newValue, oldValue) {
				this.autoComplete(this.attributeDescription, newValue, oldValue);
			}, this);
		},

		onClassSelected: function(idClass) {
			this.idClass = idClass;
			this.classObj = this.takeDataFromCache(idClass);

			if (this.classObj) {
				this.domainStore.load({
					params : {
						idClass : idClass
					}
				});

				this.attributeTypeStore.load({
					params : {
						tableType : getTableType(this.classObj)
					}
				});

				this.hideContextualFields();
				this.attributeUnique.cmImmutable = cannotHaveUniqueAttributes(this.classObj);
				this.attributeNotNull.cmImmutable = cannotHaveNotNullAttributes(this.classObj);
			}
		},

		// private and overridden in subclasses
		takeDataFromCache: function(idClass) {
			return _CMCache.getClassById(idClass);
		},

		onAttributeSelected : function(attribute) {
			this.reset();
			
			if (attribute) {
				this.getForm().setValues(attribute.raw);
				this.disableModify(enableCMTbar = true);
				this.deleteButton.setDisabled(attribute.get("inherited"));
				this.hideContextualFields();
				this.showContextualFieldsByType(attribute.get("type"));
	
				this.referenceFilterMetadata = attribute.raw.meta || {};
				this.referenceFilterMetadataDirty = false;
			}
		},

		// override
		reset: function() {
			this.mixins.cmFormFunctions.reset.call(this);
			this.referenceFilterMetadata = {};
			this.referenceFilterMetadataDirty = false;
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

		onAddAttributeClick : function(params, enableAll) {
			this.reset();
			this.setDefaultValues();
			this.hideContextualFields();
			this.enableModify(all = true);
			if (!enableAll) {
				this.attributeUnique.setDisabled(cannotHaveUniqueAttributes(this.classObj));
				this.attributeNotNull.setDisabled(cannotHaveNotNullAttributes(this.classObj));
			}
		},

		setDefaultValues: function() {
			this.isActive.setValue(true);
			this.isBasedsp.setValue(false);
			this.fieldMode.setValue("write");
		},

		buildBasePropertiesPanel: function() {
			this.baseProperties = new Ext.form.FieldSet({
				title : tr.baseProperties,
				padding: "5 5 20 5",
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
					this.fieldMode
				]
			});
		},

		fillAttributeGroupsStore: function(attributes) {
			var store = this.attributeGroup.store,
				addtributesGroup = {},
				groups = [],
				attribute;

			store.removeAll();

			// build a map before to deny duplications
			for (var i=0, len=attributes.length; i<len; ++i) {
				attribute = attributes[i];
				if (attribute.data.group) {
					addtributesGroup[attribute.data.group] = true;
				};
			}

			for (var g in addtributesGroup) {
				groups.push([g]);
			}

			store.loadData(groups);
		},

		// override
		enableModify: function(all) {
			this.mixins.cmFormFunctions.enableModify.call(this, all);
			this.addMetadataBtn.enable();
		},

		// override
		disableModify: function(enableCMTBar) {
			this.mixins.cmFormFunctions.disableModify.call(this, enableCMTBar);
			this.addMetadataBtn.disable();
		}
	});

	function onSelectComboType (combo, record, index) {
		var type = record[0].data.value;
		this.hideContextualFields();
		this.showAndEnableContextualFieldsByType(type);
	}

})();