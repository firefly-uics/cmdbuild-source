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

    //custom attributes
	translation: CMDBuild.Translation.management.findfilter,
	attributeList: {},
	IdClass: 0,
    autoScroll: false,

	initComponent:function() {
	    this.fieldsetCategory = {};
	    this.menu = new Ext.menu.Menu();
	    this.fillMenu();
	    
	    var tbar = [{text: this.translation.title, iconCls: 'add', menu: this.menu}];

		if (this.filterButton) {
        	tbar.push('->');
        	tbar.push(this.filterButton);
    	}
		
		this.fieldsPanel = new Ext.Panel({
			frame: true,
			border: true,
       		autoScroll: true,
       		region: 'center'
       	});
		
		this.fieldsPanel.add({
        	xtype: 'hidden',
        	name: 'IdClass',
        	value: this.IdClass
    	});
		
		Ext.apply(this, {
			layout: 'border',
			tbar: tbar,
			items: [this.fieldsPanel]
        });
        
        this.callParent(arguments);
    },

    addAttributeToFilter: function(attribute) {	
		var field = CMDBuild.Management.FieldManager.getFieldSetForFilter(attribute);
		var attributeField = field.getAttributeField();
		
		if (attributeField.isFormField) {
			this.addField(field, attribute);
		} else {
			this.addFieldSet(field, attribute);
		}			
	},
    
    //private
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
    
    addField: function(field, attribute){
    	//hide the or of the current field
		field.getOrPanel().hide();
    	if (typeof this.fieldsetCategory[attribute.description] == "undefined" ) {
    		this.fieldsetCategory[attribute.description] = new Ext.form.FieldSet({
				title: attribute.description,
	       		border: false
	       	});

	       	this.fieldsPanel.add(this.fieldsetCategory[attribute.description]);	       	
		} else {
			//show the or of the last field
			var indexLastField = (this.fieldsetCategory[attribute.description].items.items.length) - 1;
			var lastField = this.fieldsetCategory[attribute.description].items.items[indexLastField];
			lastField.getOrPanel().show();
		}
		
		field.removeButton.on('click', function(){
			this.removeFieldset(field.fieldsetCategory, field);
		}, this),
		
		this.fieldsetCategory[attribute.description].add(field);
		this.fieldsPanel.doLayout();
	},
    
    addFieldSet: function(field, attribute){
    	var lookUp = field.getAttributeField();
    	for (var i = 0, items = lookUp.items.items, len = items.length; i<len; i++){
    		var item = items[i];
    		item.hideLabel = true;
    		item.growMax = 180;
    	}
    	this.addField(field, attribute);
    },
    
    removeFieldset: function(category, fieldset){
   		this.fieldsetCategory[category].remove(fieldset);
   		if (this.fieldsetCategory[category].items.length == 0){
   			this.fieldsPanel.remove(this.fieldsetCategory[category]);
   			this.fieldsetCategory[category] = undefined;
   			this.fieldsPanel.doLayout;
   		} else {
   			var indexLastField = (this.fieldsetCategory[category].items.items.length) - 1;
			var lastField = this.fieldsetCategory[category].items.items[indexLastField];
			lastField.getOrPanel().hide();
   		}
    },
    
    cleanFildsetCategory: function() {
    	this.fieldsetCategory = {};
    }
});