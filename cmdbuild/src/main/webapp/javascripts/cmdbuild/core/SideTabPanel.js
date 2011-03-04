(function() {
	
	CMDBuild.SideTabPanel = Ext.extend(Ext.Panel, {
		pressedTabCls: "cmdb-pressed-tab",
		tabCls: "cmdb-tab",
		initComponent: function() {
			var tabCls = this.tabCls;
			var pressedTabCls = this.pressedTabCls;
			
			var tabs = new CMDBuild.Tabs({
				tabCls: tabCls,
				pressedTabCls: pressedTabCls
			});
			var centralPanel = new CMDBuild.TabbedPanel({
				tabCls: tabCls,
				pressedTabCls: pressedTabCls
			});
			
			this.layout = "border";
			this.items = [centralPanel, tabs];
			CMDBuild.SideTabPanel.superclass.initComponent.apply(this, arguments);
			
			this.addPanel = function(panel) {
				centralPanel.add(panel);
				if (centralPanel.itemsLength() == 1) {
					tabs.hide();
				} else {
					tabs.show();
				}
			};
			
			centralPanel.on("addedpanel", function(panel) {
				tabs.addTabFor(panel);
			}, this);
			
			tabs.on("click", function(tab) {
				centralPanel.setActiveItem(tab.targetPanel);
			}, this);
			
			Ext.each(this.tabs, function(panel) {
				this.addPanel(panel);
			}, this);
			
			this.getCentralPanel = function() {
				return centralPanel;
			};
		}
	});
	
	CMDBuild.Tabs = Ext.extend(Ext.Panel, {
		region: "east",
		frame: false,
		border: false,
		margins: "0 0 0 2",
		pressedTabCls: "cmdb-pressed-tab",
		tabCls: "cmdb-tab",
		layout: {
            type:'vbox',
            align:'stretchmax'
        },
        defaults:{margins:'2 4 0 0'},
        bodyStyle: { 
        	background: CMDBuild.Constants.colors.blue.background,
        	padding: "2px 0 0 0",
        	"border-left": "1px " + CMDBuild.Constants.colors.blue.border+" solid"
        },
		initComponent: function() {
			if (fixRendereingIssueForIE7()) {
				this.maxTabWidth = 0;
			} else {
				this.autoWidth = true;
			}
        	CMDBuild.Tabs.superclass.initComponent.apply(this, arguments);
        },
		addTabFor: function(panel, additionalCls) {
        	var tabCls = this.tabCls;
			var pressedTabCls = this.pressedTabCls;
        	var t = new Ext.Panel({
				text: panel.title,
				bodyStyle: { 
		        	background: CMDBuild.Constants.colors.blue.background
        		},
				frame: false,
				border: false,
				cls: tabCls,
				html: (function(panel, additionalCls) {
					var tmpl;
					if (additionalCls) {
						var tmpl = "<div class=\"cmdb-tab-icon {1}\"></div><p>{0}</p>";
						return String.format(tmpl, panel.tabLabel, additionalCls);
					} else {
						tmpl = "<p>{0}</p>";
						return String.format(tmpl, panel.tabLabel);
					}
				})(panel, additionalCls),
				targetPanel: panel,
				listeners: {
			        render: function(p) {
						p.getEl().on('click', p.fireEvent.createDelegate(p, ["click", p]));
			        }
			    }
			});
        	
        	t.on("click", onTabClick, this);
        	
        	if (fixRendereingIssueForIE7()) {
	        	t.on("afterlayout", function(p) {
	        		var tabWidth = p.getWidth();
	        		if (this.maxTabWidth < tabWidth) {
	        			this.setWidth(tabWidth + 22);
	        		}
	        	}, this, {single: true});
        	}
        	
			panel.on("activate", function() {
				manageToggleTab.call(this, t); // to manage the first activation without a real click
				if (fixRendereingIssueForIE7()) {
					panel.doLayout();
				}
			}, this);
			this.add(t);
        },
        activateFirst: function() {
        	var f = this.items.first();
        	if (f) {
        		onTabClick.call(this, f);
        	}
        }
	});
	
	
	CMDBuild.TabbedPanel = Ext.extend(Ext.Panel, {
		layout: "card",			
		region: "center",
		activeItem: 0,
		border: false,
		setActiveItem: function(panel) {
			this.getLayout().setActiveItem(panel.id);
		},
		onAdd: function(p) {
			this.fireEvent("addedpanel", p);
		},
		itemsLength: function() {
			return this.items.items.length;
		}
	});
	
	function manageToggleTab(tab) {
		if (this.pressedTab) {
			this.pressedTab.removeClass(this.pressedTabCls);
		}
		tab.addClass(this.pressedTabCls);
		this.pressedTab = tab;
	};
	
	function fixRendereingIssueForIE7() {
		return Ext.isIE7;
	}
	
	function onTabClick(tab) {
		manageToggleTab.call(this, tab);
		this.fireEvent("click", tab);
	}
})();