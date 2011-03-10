(function() {

	CMDBuild.Management.ActivityOptionTab = Ext.extend(Ext.Panel, {
    translation : CMDBuild.Translation.management.modworkflow,
    activityTab: undefined, // passed in instantiation
    
    initComponent: function() {

    	Ext.apply(this,{
    		layout: 'card'
    	});
    	
    	CMDBuild.Management.ActivityOptionTab.superclass.initComponent.apply(this, arguments);
    	this.subscribe('cmdb-extattr-instanced', this.extAttrInstanced, this);
    },

    reset: function() {
    	this.removeAll(true);
    },

    extAttrInstanced: function(evtParams) {
    	if (evtParams.bottomButtons) {
    		Ext.each(evtParams.bottomButtons, function(btn) {
    		    this.addButton(btn);
                btn.hide();
    		}, this);
    	}
    },
    
    loadActivity: function(eventParams) {
    	this.reset();
    	var activity = eventParams.record.data;
    	this.cmdbExtAttrDefs = activity.CmdbuildExtendedAttributes;
    	if (!this.cmdbExtAttrDefs) {
    		return;
    	}
    	Ext.each(this.cmdbExtAttrDefs, function(item) {
            this.add({
    	       xtype: item.extattrtype,
    	       id: item.identifier,
    	       extAttrDef: item,
    	       activity: activity,
    	       processInstanceId: eventParams.record.data.ProcessInstanceId,
    	       workItemId: eventParams.record.data.WorkItemId,
    	       clientForm: this.activityTab.getForm()
    	   });
    	}, this);
    	this.doLayout(true);
    	this.fireEvent("CMActivityLoaded");
    },
    
    onActivityStartEdit: function() {
    	this.items.each(function(item) {
    		var controller = item.getController();
    		if (controller) {
    			controller.onActivityStartEdit();
    		}
    	});
    },
    
    areThereBusyWidget: function() {
    	var widgets = this.items.map;
		for (var key in widgets) {
			var widget = widgets[key];
			_debug("Link card " + key + " is busy: ", widget.isBusy());
			if (widget.isBusy()) {
				return true;
			} else {
				continue;
			}
		}
		return false;
    },
    
    getWidgetsForWhichTheSaveWentWrong: function() {
    	var wrongWidgets = [];
    	this.items.each(function(widget) {
    		if (widget.savingWentWrong) {
    			wrongWidgets.push(widget.extAttrDef.ButtonLabel || widget.id);
    		}    	
    	});
    	if (wrongWidgets.length == 0) {
    		return null;
    	} else {
    		return wrongWidgets;    		
    	}
    },
    
    waitForBusyWidgets: function(cb, cbScope) {    	
    	var pf = new PollingFunction({
    		success: cb,
    		failure: function failure() {
        		CMDBuild.Msg.error(null,CMDBuild.Translation.errors.busy_wf_widgets, false);
        	},
    		checkFn: function checkNotBusy() {
        		return !this.areThereBusyWidget();
        	},
    		cbScope: cbScope,
    		checkFnScope: this
    	}).run();
    },
    
    getInvalidWidgets: function() {
    	var invalid = [];
    	if (this.items.length > 0) {
			this.items.each(function(extAttr) {
				if (!extAttr.isValid()) {
					invalid.push(extAttr.extAttrDef.ButtonLabel || extAttr.extAttrDef.ClassName);
				}
			});
		}
    	return invalid;
    },
    
    getInvalidWidgetsAsHTML: function() {
		var attrs = this.getInvalidWidgets();
		if (attrs.length == 0) {
			return null;
		} else {
			var out = "<ul>";
			for (var i=0, l=attrs.length; i<l; ++i) {
				out += "<li>" + attrs[i] + "</li>";
			}
			
			return out+"</ul>";
		}
	},
	
    /**
	 * @params:
	 * 
	 * process: { 
	 * 	isAdvance: boolean -> say if the activity have to advance,
	 * 	processInstanceId: integer -> the id of the process,
	 * 	workItemId: integer -> the id of the work item 
	 * },
	 * callback: function to call after the save
	 */
    saveWFWidgets : function(process, callback, scope) {
    	if (this.items.length == 0) {
    		callback.call(scope);
		} else {
			this.items.each(function(extAttr) {
				extAttr.setup(process.ProcessInstanceId, process.WorkItemId);
				extAttr.save();
			});
			
			this.waitForBusyWidgets(function() {
				var wrongWidgets = this.getWidgetsForWhichTheSaveWentWrong();
				if (wrongWidgets == null) {
					callback.call(scope);
				} else {
					this.fireEvent("wrong_wf_widgets", wrongWidgets);
				}
			}, this);
		}
	}
});

	var PollingFunction = function(conf) {
		var DEFAULT_DELAY = 500; 
		var DEFAULT_MAX_TIMES = 60;
		
		this.success =  conf.success || Ext.emptyFn;
		this.failure = conf.failure || Ext.emptyFn;
		this.checkFn = conf.checkFn || function() { return true;};
		this.cbScope = conf.cbScope || this;
		this.delay = conf.delay || DEFAULT_DELAY;
		this.maxTimes = conf.maxTimes || DEFAULT_MAX_TIMES;
		this.checkFnScope = conf.checkFnScope || this.cbScope;
		
		this.run = function() {
			if (this.maxTimes == DEFAULT_MAX_TIMES) {
				CMDBuild.LoadMask.get().show();
			}
			if (this.maxTimes > 0) {
				if (this.checkFn.call(this.checkFnScope)) {
					_debug("End polling with success");
					CMDBuild.LoadMask.get().hide();
					this.success.call(this.cbScope);
				} else {
					this.maxTimes--;
					this.run.defer(this.delay, this);
				}
			} else {
				_debug("End polling with failure");
				CMDBuild.LoadMask.get().hide();
				this.failure.call();
			}
		};
	};
	
	Ext.reg('activityoptiontab', CMDBuild.Management.ActivityOptionTab);
})();