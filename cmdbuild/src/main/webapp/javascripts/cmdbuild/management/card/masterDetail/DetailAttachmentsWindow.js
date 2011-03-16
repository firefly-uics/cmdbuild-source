CMDBuild.Management.DetailAttachmentsWindow = Ext.extend(CMDBuild.PopupWindow, {
    masterCard: null,
    initComponent: function() {
	    this.title = String.format("{0} - {1}"
	    		, CMDBuild.Translation.management.modcard.tabs.attachments 
	    		, this.masterCard.Description);
	    
	    var attachments = new CMDBuild.Management.CardAttachmentsTab({
	    	border: false
	    });

	    var closeButton = new Ext.Button({
	        text: CMDBuild.Translation.common.buttons.close,
	        name: 'saveButton',
	        formBind: true,
	        handler: function() {
		        this.destroy();
	        },
	        scope: this
	    });

	    this.items = [ attachments ];
	    this.buttons = [ closeButton ];
	    this.buttonAlign = "center";
	    CMDBuild.Management.DetailAttachmentsWindow.superclass.initComponent
	            .call(this);

	    this.on("show", function() {
	    	attachments.loadCard({
	    		record: new Ext.data.Record(this.masterCard)
	    	});
	    }, this);	  
    }
});