CMDBuild.Management.GraphWindow = Ext.extend(CMDBuild.PopupWindow, {
	resizable: true,

	initComponent: function() {
		var graphParams = {};
		Ext.apply(graphParams, CMDBuild.Config.graph, {
				classid: this.classId,
				objid: this.cardId
			});
		Ext.apply(this, {
			title: CMDBuild.Translation.management.graph.title,
			items: {
				xtype: 'flashpanel',
				mediaCfg : {
					mediaType: 'SWF',
					url: 'flash/graph.swf',
					start: true,
					loop: false,
					controls: false,
					params: {
						flashVars: graphParams
					}
				}
			}
		});
		CMDBuild.Management.GraphWindow.superclass.initComponent.apply(this);
	}
});

CMDBuild.Management.showGraphWindow = function(classId, cardId) {
	var graphWindow = new CMDBuild.Management.GraphWindow({
			classId: classId,
			cardId: cardId
		});
	graphWindow.show();
};

CMDBuild.Management.GraphActionHandler = function() {
    this.action = new Ext.Action({
  		iconCls : 'graph',
  		text : CMDBuild.Translation.management.graph.action,
		handler : this.onShowGraph,
  		scope: this,
  		disabled: true
	});
	this.subscribe('cmdb-init-class', this.initForClass, this);
	this.subscribe('cmdb-load-card', this.loadCard, this);
	
	this.subscribe('cmdb-load-activity', this.loadActivity, this);
 };
 
 Ext.extend(CMDBuild.Management.GraphActionHandler, Ext.util.Observable, {
	getAction: function() {
		return this.action;
	},

	initForClass: function(eventParams) {
		this.action.disable();
	},

	isASimpleTable: function() {
		if (this.currentTable) {
			return this.currentTable.tableType == "simpletable";
		} else {
			return false;
		}
	},
	
	loadCard: function(eventParams) {
		this.currentCardId = eventParams.record.data.Id;
		this.currentClassId = eventParams.record.data.IdClass;
		this.currentTable = CMDBuild.Cache.getTableById(this.currentClassId);
		// the simple table has not relation graph
		this.action.setDisabled(this.isASimpleTable());
	},

	loadActivity: function(eventParams) {
		this.currentActivityId = eventParams.record.data.Id;
		this.currentProcessId = eventParams.record.data.IdClass;
		this.currentActivityTable = CMDBuild.Cache.getTableById(this.currentClassId);
		// the simple table has not relation graph
		this.action.setDisabled(this.isASimpleTable());
	},
	
	onShowGraph: function() {
		var expandedAccordion = _MainViewportController.getExpandedAccordion();
		if (_MainViewportController.getExpandedAccordion().cmType == "card") {
			CMDBuild.Management.showGraphWindow(this.currentClassId, this.currentCardId);
		} else {
			CMDBuild.Management.showGraphWindow(this.currentProcessId, this.currentActivityId);
		}
	}
});
