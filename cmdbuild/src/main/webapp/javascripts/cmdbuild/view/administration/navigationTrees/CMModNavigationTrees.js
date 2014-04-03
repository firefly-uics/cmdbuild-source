(function() {
	var translationModClass = CMDBuild.Translation.administration.modClass;
	
	var baseTitle = "@@ Navigation tree"; //translationModClass.tabs.domains;
	
	Ext.define("CMDBuild.view.administration.navigationTrees.CMModNavigationTrees", {
		extend: "Ext.panel.Panel",
		
		cmName:'navigationTrees',	
		translation: CMDBuild.Translation.administration.modClass,
		
		NAME: "CMModNavigationTrees",

		constructor: function() {
			this.addButton = new Ext.Button({
				iconCls: 'add',
				text: "@@ Add navigation tree",
				scope: this,
				handler: function() {
					this.delegate.cmOn("onAddButtonClick");
				}
			});

			this.navigationTreesForm = new CMDBuild.view.administration.navigationTrees.CMNavigationTreesForm({
				title: translationModClass.tabs.properties
			});

			this.navigationTreesTree = new CMDBuild.view.administration.navigationTrees.CMNavigationTreesTree({
				title: "@@ Tree"
			});

			this.tabPanel = new Ext.TabPanel({
				region: "center",
				frame: false,
				border: false,
				items: [this.navigationTreesForm, this.navigationTreesTree],
				activeTab: 0
			});
			
			this.callParent(arguments);
		},

		initComponent : function() {
			this.layout = "border";
			this.title = baseTitle;
			this.tbar = [this.addButton];
			this.items = [this.tabPanel];
			this.frame = false;
			this.border = true;
			this.callParent(arguments);
		},

		selectPropertiesTab: function() {
			this.tabPanel.setActiveTab(this.navigationTreesForm);
		},

		setTitleSuffix: function(navigationTreeDescription) {
			if (typeof navigationTreeDescription != "undefined") {
				this.setTitle(baseTitle + " - " + navigationTreeDescription);
			} else {
				this.setTitle(baseTitle);
			}
		} 
	});

})();