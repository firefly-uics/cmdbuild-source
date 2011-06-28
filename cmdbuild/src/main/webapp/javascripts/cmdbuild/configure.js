(function() {

Ext.define("CMDBuild.app.Configure", {
	statics: {
		init: function() {
			Ext.QuickTips.init();//for the error tips
			
			this.step1 = new CMDBuild.configure.Step1();
			this.step2 = new CMDBuild.configure.Step2();
			this.step3 = new CMDBuild.configure.Step3();

			this.nextButton = new Ext.button.Button({
				text: "@@ Next"
			});
			
			this.prevButton = new Ext.button.Button({
				text: "@@ Prew",
				disabled: true
			});

			this.finishButton = new Ext.button.Button({
				text: "@@ finish",
				hidden: true
			});

			this.cardPanel = new Ext.panel.Panel({
				layout: "card",
				region: "center",
				border: false,
				frame: true,
				activeItem: 0,
				items: [this.step1, this.step2, this.step3],
				buttons: [this.prevButton, this.nextButton, this.finishButton]
			});

			this.bringToFront = function(panel) {
				this.cardPanel.layout.setActiveItem(panel.id);
			}
			
			this.showNextButton = function(show) {
				this.finishButton.setVisible(!show);
				this.nextButton.setVisible(show);
			}
			
			new Ext.Viewport({
				layout:'border',
				frame: false,
				border: false	,
				items:[this.cardPanel, {
					frame: false,
					border: false,
					region:'north',
					id: 'header_panel',
					contentEl: 'header',
					height: 45
				}, {
					frame: false,
					border: false,
					region : "south",
					id: 'footer_panel',
					contentEl: "footer",
					border: false,
					height: 20
				}]
			});
			new CMDBuild.configure.CMConfigureController(this);
		}
	}
});

})();
// 	var tr = CMDBuild.Translation.configure;
// 
// 	var redirectToApp = function(){
// 		window.location = 'management.jsp';
// 	};
// 	
// 	if (CMDBuild.Config.cmdbuild.jdbcDriverVersion == "undefined") {
// 		CMDBuild.Msg.warn(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.no_driver, true);
// 	} else {
// 		this.sptep1 = new CMDBuild.Configure.Step1({id: 'card-1'});
// 		
// 		function displayViewport() {
// 		    var viewport = new Ext.Viewport({
// 		        id: 'configure-viewport',
// 		        layout:'border',
// 				frame: false,
// 				border: false,
// 		       	items:[{
// 			          	region: 'center',
// 		           	    layout: 'fit',
// 					    border: false,
// 					    frame: false,
// 					    cls: 'configure_wizard',
// 						items: [{
// 							frame: true, 
// 							title: tr.title,
// 						    layout:'card',
// 							activeItem: 0,
// 						    id: 'configure_wizard_panel',
// 							items: [
// 								this.step1
// 							,{
// 								id: 'card-2',
// 								xtype: 'configureStep2'
// 						    },{
// 								id: 'card-3',
// 								xtype: 'configureStep3'
// 						    }]
// 						}]
// 		            },{
// 						frame: false,
// 						border: false,
// 			        	region:'north',
// 			        	id: 'header_panel',
// 			            contentEl: 'header',
// 			            height: 45
// 			        },{
// 			        	frame: false,
// 						border: false,
// 				        region : "south",
// 				        id: 'footer_panel',
// 				        contentEl: "footer",
// 				        border: false,
// 						height: 20
// 			        }]
// 		    });
// 		}
// 		displayViewport();
// 	}
// });