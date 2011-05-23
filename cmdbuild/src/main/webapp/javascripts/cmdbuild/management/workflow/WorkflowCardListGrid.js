CMDBuild.Management.WorkflowCardListGrid = Ext.extend(CMDBuild.Management.CardListGrid,{
	initComponent: function() {
		CMDBuild.Management.WorkflowCardListGrid.superclass.initComponent.apply(this, arguments);
	},
	
	deselect: function() {
		this.getSelectionModel().clearSelections();
	},
	
	reloadCard: function(params) {
		if(params && params.Id) {
			this.loadPageForCardId(params.Id, params.notChangeStatusAfterSave);
		} else {
			this.reloadCurrentCard(params);
		}
	},
		    
	reloadCurrentCard : function(params) {
		var st = this.wfmodule.getSelectedState();
		this.getStore().baseParams['state'] = st;
		if(params && params["ProcessInstanceId"] && params["Id"]) {
			this.getStore().reload({
				scope: this,
				callback: function() {			    	
			        var tot = this.getStore().getCount();
			        for(var i=0; i<tot; i++) {
			            var rec = this.getStore().getAt(i);
			            if(rec.json["ProcessInstanceId"] == params["ProcessInstanceId"] &&
			            rec.json["Id"] == params["Id"]) {			            	
			                this.getSelectionModel().selectRow(i);
			                this.getView().focusRow(i);
			                this.selectedRow = i;
			                break;
			            }
			        }
			    }		    
			});
		} else {
			this.loadProcess(1);
		}
	},
	    
	loadProcess : function(position) {
		var st = this.wfmodule.getSelectedState();
		this.getStore().baseParams['state'] = st;
		this.loadCards(position);
	},

	loadPageForCardId : function(cardId, dontChangeStatusAfterSave) {
		if (cardId) {
			var params = this.defineParamsToLoadPageForCardId(cardId);
			params['withflowstatus'] = !this.displayingAllStatuses();
			params['FilterCategory'] = this.eventmastertype;
		    CMDBuild.Ajax.request({		    	
		    	url: 'services/json/management/modcard/getcardposition',
		        params: params,
		        scope: this,
		        success:function(response, options, resText) {
		    		var position = resText.position;
		    		if (!this.isCurrentStatus(resText.flowstatus) &&
		    				!this.displayingAllStatuses()) {
		    			if (dontChangeStatusAfterSave) {
		    				position = 0;
		    			} else {
		    				this.wfmodule.setSelectedState(resText.flowstatus, false);
		    			}
		    		}
		            this.loadProcess(position);
		        },
		        failure: function() {
		            this.loadProcess(1);
		            this.publish('cmdb-disable-modify');
		        }
		    });
		} else {
		    this.loadProcess(1);
		}
	},

	displayingAllStatuses: function() {
		return this.wfmodule.getSelectedState() === "all";
	},

	isCurrentStatus: function(status) {
		return status === this.wfmodule.getSelectedState();
	},

	publishLoadEvent: function(record) {
		var activity = this.addActivityDescription(record);
		this.fireEvent("load_activity", {record: new Ext.data.Record(activity)});
		
		CMDBuild.Management.WorkflowCardListGrid.superclass.publishLoadEvent.call(this, record);
	},
	
	addActivityDescription: function(record) {
		var selectedRow = this.getSelectionModel().getSelected();
		if (selectedRow) {
			record.ActivityDescription = selectedRow.data.ActivityDescription;
		}
		return record;
	}
});

