CMDBuild.LocaleSearchField = Ext.extend(Ext.form.TwinTriggerField, {
	
    initComponent : function(){
		CMDBuild.LocaleSearchField.superclass.initComponent.call(this);
        this.triggerConfig = {
            tag:'span', cls:'x-form-twin-triggers', cn:[
            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger1Class},
            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger2Class}
        ]};
        
        this.on('specialkey', function(f, e){
            if(e.getKey() == e.ENTER){
                this.onTrigger1Click();
            }
        }, this);   
        
        this.on('render', function(field){
        	//TODO a generic way to define the width of the field
        	//realy, but realy realy, ugly solution for a render issue
        	var wrap = this.container.dom.childNodes[0]
        	wrap.style.width = "300px";      	 
        }, this);
    },

    validationEvent:false,
    validateOnBlur:false,
    
    trigger1Class:'x-form-search-trigger',
    trigger2Class:'x-form-clear-trigger',
   
    hideTrigger1 :false,
	hideTrigger2 :false,
	
    onTrigger1Click : function(){
        var query = this.getRawValue().toUpperCase();
        this.grid.getStore().filterBy(function(record,id){
        	for (var attr in record.data) {
        		var attribute = (record.data[attr]+"").toUpperCase();
        		var searchIndex = attribute.search(query);
        		if (searchIndex != -1)
        			return true
        	}
        	return false
        });
    },
    
    onTrigger2Click: function(e){
		if (! this.disabled)
			this.setValue("");
			this.onTrigger1Click();
	}
});
Ext.reg('localesearchfield', CMDBuild.LocaleSearchField);