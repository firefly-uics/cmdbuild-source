/**
 * This is the Attribute Panel that contains the part of the filter that allow you
 * to choose the attribute to filter
 * 
 * @class CMDBuild.Management.Attributes
 * @extends Ext.form.FormPanel
 */
 
Ext.define("CMDBuild.Management.Attributes", {
	extend: "Ext.form.Panel",
	url: 'services/json/management/modcard/setcardfilter',
	title: CMDBuild.Translation.management.findfilter.attributes,
	autoScroll: false,

	//custom attributes
	translation: CMDBuild.Translation.management.findfilter,
	attributeList: {},
	IdClass: 0,

	initComponent:function() {
		this.fieldsetCategory = {};
		this.menu = new Ext.menu.Menu();
		this.fillMenu();

		var tbar = [{text: this.translation.title, iconCls: 'add', menu: this.menu}];

		if (this.filterButton) {
			tbar.push('->');
			tbar.push(this.filterButton)
			this.resetFilterButton = new Ext.button.Button({
				text: CMDBuild.Translation.management.findfilter.clear_filter,
				iconCls: "delete"
			});
			tbar.push(this.resetFilterButton);
		}

		// TODO this panel is not needed, remove id
		this.fieldsPanel = new Ext.Panel( {
			frame : false,
			border : false,
			bodyCls: "x-panel-default-framed",
			autoScroll : true,
			region : 'center'
		});

		this.fieldsPanel.add( {
			xtype : 'hidden',
			name : 'IdClass',
			value : this.IdClass
		});

		Ext.apply(this, {
			layout: 'border',
			tbar: tbar,
			items: [this.fieldsPanel],
			listeners: {
				added: function(me) {
					// needed because the zIndexParent is not set
					// for the menu, because when created is not owned
					// in a floating element
					me.menu.registerWithOwnerCt();
				}
			}
		});

		this.callParent(arguments);
	},

	updateMenuForClassId: function(classId) {
		this.currentClassId = classId;
		_CMCache.getAttributeList(classId, Ext.bind(function(attributes) {
			this.attributeList = attributes
			this.fillMenu();
		}, this));
	},

	// private
	addAttributeToFilter: function(attribute) {	
		var field = CMDBuild.Management.FieldManager.getFieldSetForFilter(attribute);
		var attributeField = field.getAttributeField();
		
		if (attributeField.isFormField) {
			this.addField(field, attribute);
		} else {
			this.addFieldSet(field, attribute);
		}
	},

	// private
	addField: function(field, attribute){
		//hide the "or" of the current field
		field.getOrPanel().hide();
		var category = attribute.name;

		if (typeof this.fieldsetCategory[category] == "undefined" ) {
			this.fieldsetCategory[category] = new Ext.form.FieldSet({
				title: attribute.description
			});

			this.fieldsPanel.add(this.fieldsetCategory[category]);
		} else {
			//show the or of the last field
			this.fieldsetCategory[category].items.last().getOrPanel().show();
		}

		field.removeButton.on('click', function(){
			this.removeFieldset(category, field);
		}, this);

		this.fieldsetCategory[category].add(field);
		this.fieldsPanel.doLayout();
	},

	addFieldSet : function(field, attribute) {
		var lookUp = field.getAttributeField();
		for ( var i = 0, items = lookUp.items.items, len = items.length; i < len; i++) {
			var item = items[i];
			item.hideLabel = true;
			item.growMax = 180;
		}

		this.addField(field, attribute);
	},

	removeFieldset : function(category, fieldset) {

		(function notifyBasicFormOfNestedFieldRemoved() {
			var me = this;
			fieldset.cascade(function(item) {
				if (item && (item instanceof Ext.form.Field)) {
					me.fireEvent("remove", me, item);
				}
			});
		}).call(this);

		this.fieldsetCategory[category].remove(fieldset);

		if (this.fieldsetCategory[category].items.length == 0) {
			// the removed field was the last, so the fieldset is now empty
			this.fieldsPanel.remove(this.fieldsetCategory[category]);
			delete this.fieldsetCategory[category];
		} else {
			this.fieldsetCategory[category].items.getLast().getOrPanel().hide();
		}
	},

	removeAllFieldsets: function() {
		this.fieldsPanel.removeAll();
		this.fieldsetCategory = {};
	},

	// private
	fillMenu: function() {
		this.menu.removeAll();
		var groupedAttr = CMDBuild.Utils.groupAttributes(this.attributeList
				,allowNoteFiled = false);
		
		var submenues = buildSubMenues.call(this);
		if (submenues.length == 1) {
			this.menu.add(submenues[0].menu);
		} else {
			this.menu.add(submenues);
		}
		
		function buildSubMenues() {
			var submenues = [];
			var _this = this;
			for (var group in groupedAttr) {
				var items = [];
				var attrs = groupedAttr[group];
				
				for (var i=0, l=attrs.length; i<l; ++i) {
					items.push({ 
						text: attrs[i].description,
						attribute: attrs[i],
						handler: function() {
							_this.addAttributeToFilter(this.attribute);
						}
					});
				}
				
				submenues.push({text: group, menu: items});
			}
			return submenues;
		}
		
	},

	cleanFildsetCategory : function() {
		this.fieldsetCategory = {};
	}

});