/**
 * This is the Filter window that contains the card filter
 * 
 * @class CMDBuild.Management.SearchFilterWindow
 * @extends Ext.Window
 */

CMDBuild.Management.SearchFilterWindow = Ext.extend(CMDBuild.PopupWindow, {
    attributeList: {},
    IdClass: 0,
    className:'',
	//custom
	initComponent: function() {
		this.attributes = new CMDBuild.Management.Attributes({
        	attributeList: this.attributeList, 
        	IdClass: this.IdClass, 
        	windowSize: this.windowSize
        });
        this.relations = new CMDBuild.Management.Relations({attributeList: this.attributeList, idClass: this.IdClass});

        Ext.apply(this, {
        	title: CMDBuild.Translation.management.findfilter.window_title + " - " + this.className,
            items : [{
            	xtype: 'panel',
            	layout: 'accordion',
            	border: false,
            	items: [
            	    this.attributes,
            	    this.relations
            	    /*
            	    ,new CMDBuild.Management.Attachments(),
            	    new CMDBuild.Management.SaveFilter()
            	    */
            	]
            }],
            buttonAlign : 'center',
            buttons: [{
                text: CMDBuild.Translation.common.btns.confirm,
                handler: this.sendForm,
                scope: this
            },{
            	text: CMDBuild.Translation.common.btns.abort,
                scope: this,
                handler: this.destroy
            }]
        });       
        CMDBuild.Management.SearchFilterWindow.superclass.initComponent.apply(this);
	},
	
	sendForm: function() {
		var params = this.setParams();
		CMDBuild.Ajax.request({
			  url : this.attributes.url,
			  method: 'POST',
			  params: params,
			  waitTitle : CMDBuild.Translation.common.wait_title,
			  waitMsg : CMDBuild.Translation.common.wait_msg,
			  scope: this,			  
			  success: function(response) {
				this.destroy();
				this.grid.clearFilterBtn.setDisabled(false);
				this.grid.reload();
				if (this.grid.pagingBar) {
					this.grid.pagingBar.changePage(1) ;
				}
			}
		});
    },
   
    setParams: function() {
    	var params = {};
    	var formParams = this.attributes.getForm().getValues();
    	//relations.updateNotInRelation();
    	formParams["checkedRecords"] = Ext.util.JSON.encode(this.relations.getCardStatesToSend());
    	for (key in formParams) {
    		params[key] = formParams[key];
    	}
    	params.FilterCategory = this.filterCategory;
    	if(this.filterSubcategory) {
    		params.FilterSubcategory = this.filterSubcategory;
    	}
    	return params;
    }
});
