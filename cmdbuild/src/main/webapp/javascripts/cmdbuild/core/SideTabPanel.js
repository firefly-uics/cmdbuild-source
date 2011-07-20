(function() {
	
	Ext.define("CMDBuild.SideTabPanel", {
		extend: "Ext.panel.Panel",
		pressedTabCls: "cmdb-pressed-tab",
		tabCls: "cmdb-tab",

		initComponent: function() {
			var tabCls = this.tabCls;
			var pressedTabCls = this.pressedTabCls;

			var tabs = new CMDBuild.Tabs({
				tabCls: tabCls,
				pressedTabCls: pressedTabCls,
				region: "east"
			});

			var centralPanel = new CMDBuild.TabbedPanel({
				tabCls: tabCls,
				pressedTabCls: pressedTabCls,
				region: "center"
			});

			Ext.apply(this, {
				layout: "border",
				items: [centralPanel, tabs]
			});
			
			this.callParent(arguments);
			
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
		},
		
		editMode: function() {
			this.getCentralPanel().items.each(function(i) {
				i.editMode();
			});
		},
		
		displayMode: function() {
			this.getCentralPanel().items.each(function(i) {
				i.displayMode();
			});
		}
	});
	
	Ext.define("CMDBuild.Tabs", {
		extend: "Ext.panel.Panel",
		frame: false,
		border: false,
		pressedTabCls: "cmdb-pressed-tab",
		tabCls: "cmdb-tab",
		bodyCls: "x-panel-body-default-framed",
		layout: {
			type:'vbox',
			align:'stretchmax'
		},
		defaults:{margins:'2 4 0 0'},
		initComponent: function() {
			if (fixRendereingIssueForIE7()) {
				this.maxTabWidth = 0;
			} else {
				this.autoWidth = true;
			}
			this.callParent(arguments);
		},
		addTabFor: function(panel, additionalCls) {
			var tabCls = this.tabCls;
			var pressedTabCls = this.pressedTabCls;
			var t = new Ext.Panel({
				text: panel.title,
				frame: false,
				border: false,
				cls: tabCls,
				html: (function(panel, additionalCls) {
					var tmpl;
					if (additionalCls) {
						var tmpl = "<div class=\"cmdb-tab-icon {1}\"></div><p>{0}</p>";
						return Ext.String.format(tmpl, panel.tabLabel, additionalCls);
					} else {
						tmpl = "<p>{0}</p>";
						return Ext.String.format(tmpl, panel.tabLabel);
					}
				})(panel, additionalCls),
				targetPanel: panel,
				listeners: {
					render: function(p) {
						p.getEl().on('click', Ext.Function.bind(p.fireEvent,p, ["click", p]));
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
		activeItem: 0,
		border: true,
		setActiveItem: function(p) {
			this.layout.setActiveItem(p.id);
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
			this.pressedTab.removeCls(this.pressedTabCls);
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