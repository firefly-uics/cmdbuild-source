CMDBuild.IconsCombo = Ext.extend(CMDBuild.CMDBuildCombo, {
	initComponent : function(){
		
        this.triggerConfig = {
            tag:'span', cls:'x-form-twin-triggers', cn:[
            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger1Class},
            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger2Class}
        ]};
        
        Ext.apply(this, {
        	width: this.width - 17,
        	editable: false,
        	store: new Ext.data.JsonStore( {
	            url: "services/json/schema/modgis/geticonslist",
	            root: "rows",
	            fields: [ 'name', 'description', 'path' ],
	            autoLoad: true
	        }),
        	tpl: new Ext.XTemplate(
	        		'<tpl for=".">',
		        		'<div class="icon-item">',
			        		'<div class="icon-item-image-wrap"><img src="{path}" alt="{description}" class="icon-item-image"/></div>',
			        		'<div class="icon-item-label">{description}</div>',
		        		'</div>',
	        		'</tpl>'
	        ),
	        itemSelector: ".icon-item"	        
        });
        CMDBuild.IconsCombo.superclass.initComponent.call(this);
    },
    
	getTrigger: Ext.form.TwinTriggerField.prototype.getTrigger,
	initTrigger: Ext.form.TwinTriggerField.prototype.initTrigger,
	trigger1Class: Ext.ux.form.XComboBox.prototype.triggerClass,
	trigger2Class: 'x-form-clear-trigger',
	onTrigger1Click: Ext.ux.form.XComboBox.prototype.onTriggerClick, 
	onTrigger2Click: function(e){
		if (!this.disabled) {
			this.clearValue();
			this.fireEvent('clear', this);
		}
	}
});