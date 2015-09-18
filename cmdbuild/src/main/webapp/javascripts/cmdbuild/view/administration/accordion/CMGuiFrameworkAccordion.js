(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMGuiFrameworkAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: "GuiFramework",

		hideMode: "offsets",

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
		},

		updateStore: function() {
			var root = this.store.getRootNode();
			root.removeAll();
			var children = [];
			CMDBuild.Ajax.request({           
			    url: CMDBuild.core.proxy.CMProxyUrlIndex.customPages.readForCurrentUser,            
			    success: function (response, options, decodedResponse) {
					var pages = decodedResponse.response;
			        for (var i = 0; i < pages.length; i++) {
			        	children.push({
			        		text: pages[i].description,
			        		cmName: "custompage",
			        		leaf: "true",
			        	});
			        }
					root.appendChild(children);
			    }
			});
		}
	});

})();