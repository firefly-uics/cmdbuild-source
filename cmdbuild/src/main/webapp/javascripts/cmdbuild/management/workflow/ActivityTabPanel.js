CMDBuild.Management.ActivityTabPanel = Ext.extend(Ext.Panel, {
    initComponent : function() {
    	var w = Ext.getBody().getViewSize().width;
    	w = w/(4.5);
    	
    	this.docPanel = new Ext.Panel({
    	  autoScroll: true,
    	  width: w,
    	  hideMode: 'offsets',
          region: 'east',
          id: 'activitydocpanel',
          frame: true,
          collapsible: true,
          collapsed: true,
          split: true,
          margins: '0 5 5 0',
          cmargins: '0 5 5 5',
          title: CMDBuild.Translation.management.modworkflow.activitydocumentation,
          html: '',          
          updateBody: function(text) {
	  		if (this.body) {
				this.body.update(text);
			}
    	  }
        });
    	
        this.TabPanel = new CMDBuild.TabPanel({
           region: 'center',
           activeTab: this.activeTab,
           items: this.tabitems,
           border: false,
           split: true,
           style: {'border-top': '1px #99BBE8 solid', 'border-right': '1px #99BBE8 solid', padding:'1px 0 0 0'}
        });
        
		Ext.apply(this,{
		  layout: 'border',
		  items : [this.TabPanel,this.docPanel]
		});

		CMDBuild.Management.ActivityTabPanel.superclass.initComponent.apply(this, arguments);
		
		this.subscribe('cmdb-load-activity', this.onLoadActivity, this);
		this.subscribe('cmdb-select-processclass', this.onSelectClass, this);
		this.subscribe('cmdb-reload-process', this.resetDocPanel, this);
		this.subscribe('cmdb-wf-layouthack',this.layoutHack,this);
	},
	
	onSelectClass: function(eventParams) {
		if (!eventParams) {
			return;
		}		
		if (eventParams.tabToOpen) {
			this.TabPanel.activateTabByAttr('cmdbName', eventParams.tabToOpen);
		}
		this.resetDocPanel();
	},
	
	onLoadActivity: function(params) {
		if (params.record.data.Id == -1)
			this.disableAllTabs();
		this.updateActivityDoc(params);
	},
	
	layoutHack: function(){
		this.docPanel.toggleCollapse(false);
		this.docPanel.toggleCollapse(false);
	},
	
	//so you can search and set the active tab even if this is not really a TabPanel
	getComponent: function(name) {
		return this.TabPanel.getComponent(name);
	},

	resetDocPanel : function() {
		this.docPanel.collapse();
		this.docPanel.updateBody('');
	},
	
	updateActivityDoc : function(evtParams) {
		var activityDoc = evtParams.record.data.ActivityDescription;
		if (activityDoc == undefined || activityDoc == '') {
            activityDoc = '';
        }
        var divDoc = '<div style="padding: 5px">' + activityDoc + '</div>';
        this.docPanel.updateBody(divDoc);
	},
	
	disableAllTabs: function() {
		var items = this.TabPanel.items.items
		for (var i = 1, len = items.length; i<len; i++) {
			items[i].disable();
		}
	},
	
	enableAllTabs: function() {
		var items = this.TabPanel.items.items
		for (var i = 1, len = items.length; i<len; i++) {
			items[i].enable();
		}
	},
	
	setActiveTab: function(tabId) {
		this.TabPanel.setActiveTab(tabId);
	}
});
Ext.reg('activitytabpanel', CMDBuild.Management.ActivityTabPanel);
