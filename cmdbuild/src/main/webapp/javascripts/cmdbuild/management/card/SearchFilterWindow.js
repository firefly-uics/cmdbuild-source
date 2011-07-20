Ext.define("CMDBuild.Management.SearchFilterWindow", {
	extend: "CMDBuild.PopupWindow",
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
        
        this.url = this.attributes.url;
        
        //this.relations = new CMDBuild.Management.Relations({attributeList: this.attributeList, idClass: this.IdClass});

        Ext.apply(this, {
        	title: CMDBuild.Translation.management.findfilter.window_title + " - " + this.className,
        	layout: "accordion",
            items: [
				this.attributes
//            	    ,this.relations
			],
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

    	this.callParent(arguments);
	},

	sendForm: function() {
		var params = this.setParams();
		CMDBuild.Ajax.request({
			  url : this.url,
			  method: 'POST',
			  params: params,
			  waitTitle : CMDBuild.Translation.common.wait_title,
			  waitMsg : CMDBuild.Translation.common.wait_msg,
			  scope: this,			  
			  success: function(response) {
				this.destroy();
				this.grid.clearFilterButton.enable();
				if (this.grid.pagingBar) {
					this.grid.store.loadPage(1) ;
				} else {
					this.grid.reload();
				}
			}
		});
    },
   
    setParams: function() {
    	var params = {};
    	var formParams = this.attributes.getForm().getValues();
    	//TODO 3 to 4
    	//relations.updateNotInRelation();
    	//formParams["checkedRecords"] = Ext.util.JSON.encode(this.relations.getCardStatesToSend());
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