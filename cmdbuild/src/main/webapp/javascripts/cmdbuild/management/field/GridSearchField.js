/*
 * Ext JS Library 2.2.1
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
 
Ext.app.GridSearchField = Ext.extend(Ext.form.TwinTriggerField, {
	
    initComponent : function(){
    	Ext.app.GridSearchField.superclass.initComponent.call(this);
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
        	var wrap = this.container.dom.childNodes[0];
        	wrap.style.width = "160px";      	 
        }, this);
    },

    afterRender : function(){
        Ext.form.TriggerField.superclass.afterRender.call(this);
        var y;
        if (Ext.isIE && !this.hideTrigger && this.el.getY() != (y = this.trigger.getY())) {
            this.el.position();                        
            this.el.setY(y - ( y - this.el.getY() - 2));
        }
    },
    
    validationEvent:false,
    validateOnBlur:false,
    
    trigger1Class:'x-form-search-trigger',
    trigger2Class:'x-form-clear-trigger',
   
    hideTrigger1 :false,
	hideTrigger2 :false,
	
    onTrigger1Click : function(){
    	var value = this.getRawValue();
    	CMDBuild.log.info('Full text query for - ' + value);
        this.grid.getStore().baseParams["query"] = value;
        this.grid.pagingBar.changePage(1); //with this is not needed the reload
    },
    
    onTrigger2Click: function(e){
		if (! this.disabled)
			this.setValue("");
			this.onTrigger1Click();
	}
});
Ext.reg('gridsearchfield', Ext.app.GridSearchField);