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
			Ext.Ajax.request({           
			    url: 'GuiFramework.xml',            
			    success: function (response, options) {
			        var object = response.responseXML;
			        var pages = Ext.DomQuery.select('page', object);
			        for (var i = 0; i < pages.length; i++) {
			        	children.push({
			        		src: Ext.DomQuery.selectValue('src', pages[i]),
			        		text: Ext.DomQuery.selectValue('title', pages[i]),
			        		cmName: Ext.DomQuery.selectValue('cmName', pages[i]),
			        		leaf: Ext.DomQuery.selectValue('leaf', pages[i]),
			        	});
			        }
					root.appendChild(children);
			    }
			});
		}
	});

})();