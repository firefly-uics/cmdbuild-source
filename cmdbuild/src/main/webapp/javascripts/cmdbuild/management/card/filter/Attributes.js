/**
 * This is the Attribute Panel that contains the part of the filter that allow you
 * to choose the attribute to filter
 * 
 * @class CMDBuild.Management.Attributes
 * @extends Ext.form.FormPanel
 */
CMDBuild.Management.Attributes = Ext.extend(Ext.form.FormPanel, {
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
			tbar: tbar,
			frame: false,
			bodyStyle: {background: CMDBuild.Constants.colors.blue.background, border: '1px '+CMDBuild.Constants.colors.blue.border+' solid'},
			border: true,
       		layout: 'form',
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
			frame: true,
			items: [this.fieldsPanel]
        });
        
        CMDBuild.Management.Attributes.superclass.initComponent.apply(this, arguments);
    },
    
    addAttributeToFilter: function(index) {
		var attribute  = this.attributeList[index];
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
        for (var i=0; i < this.attributeList.length; i++) {
			var attributeName = this.attributeList[i].description;
			if (this.attributeList[i].name != "Notes") {
				var _this = this;
				this.menu.add({ 
					text: attributeName,
					index: i,
					handler: function() {
						_this.addAttributeToFilter(this.index);
					}
				});
			}
		};		
    },
    
    addField: function(field, attribute){
    	//hide the or of the current field
		field.getOrPanel().hide();
    	if (typeof this.fieldsetCategory[attribute.description] == "undefined" ){					
    		this.fieldsetCategory[attribute.description] = new Ext.form.FieldSet({
				title: attribute.description,
	       		border: true,
	       		style: {padding: '5px', margin: '5px'},
	       		bodyStyle: {padding: '0', margin: '0'}
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
