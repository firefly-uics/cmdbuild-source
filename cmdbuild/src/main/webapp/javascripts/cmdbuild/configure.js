Ext.onReady(function(){

	Ext.QuickTips.init();//for the error tips
	var tr = CMDBuild.Translation.configure;

	var redirectToApp = function(){
		window.location = 'management.jsp';
	};
	
	if (CMDBuild.Config.cmdbuild.jdbcDriverVersion == "undefined") {
		CMDBuild.Msg.warn(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.no_driver, true);
	} else {
		function displayViewport() {
		    var viewport = new Ext.Viewport({
		        id: 'configure-viewport',
		        layout:'border',
				frame: false,
				border: false,
		       	items:[{
			          	region: 'center',
		           	    layout: 'fit',
					    border: false,
					    frame: false,
					    cls: 'configure_wizard',
						items: [{
							frame: true, 
							title: tr.title,
						    layout:'card',
							activeItem: 0,
						    id: 'configure_wizard_panel',
							items: [{
								id: 'card-1',
								xtype: 'configureStep1'
						    },{
								id: 'card-2',
								xtype: 'configureStep2'
						    },{
								id: 'card-3',
								xtype: 'configureStep3'
						    }]
						}]
		            },{
						frame: false,
						border: false,
			        	region:'north',
			        	id: 'header_panel',
			            contentEl: 'header',
			            height: 45
			        },{
			        	frame: false,
						border: false,
				        region : "south",
				        id: 'footer_panel',
				        contentEl: "footer",
				        border: false,
						height: 20
			        }]
		    });
		}
		displayViewport();
	}
});