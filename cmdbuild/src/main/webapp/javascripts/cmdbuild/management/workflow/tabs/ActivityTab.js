CMDBuild.Management.ActivityTab = Ext.extend(Ext.Panel, {
	translation : CMDBuild.Translation.management.modworkflow,
	extAttrIds: [],	
	currentState: {state: "open.running"},
	autoEditMode: false,	//flag to set the form in modify-mode after advance process
	isOldModeClose: false,	//flag to detect the mode of the previous card
	editable: false,
	hideMode: 'offsets',
	
	initComponent : function() {
		
		this.terminateProcessAction = new Ext.Action({
      		iconCls : 'delete',
      		text : this.translation.delete_card,
      		handler : this.onTerminateProcess,
      		scope : this
    	});
		
    	this.modifyCardAction = new Ext.Action({
      		iconCls : 'modify',
      		text : this.translation.modify_card,
			handler : this.enableModify,
      		scope: this
    	});

		this.saveButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.workflow.save,
			name: 'saveButton',
			handler : this.saveCard,
			scope : this
		});
		
		this.advanceButton = new Ext.Button({
		  text: CMDBuild.Translation.common.buttons.workflow.advance,
		  handler: this.advanceCard,
		  scope: this
		});

		this.cancelButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.abort,
			name: 'cancelButton',
			handler : this.onCancelButton,
			scope : this
		});
		
		this.actualForm = new Ext.form.FormPanel({
			plugins: new CMDBuild.FieldSetAddPlugin(),
			hideMode: 'offsets',
			autoHeight: true,
			autoWidth: false,
			region: 'center',
			monitorValid: true,
            labelAlign: "right", 
            labelWidth: 200,
			defaults: { 
				labelWidth: 200
			},
			frame: false,
			border: false,			
			items: [{
				xtype: 'hidden',
				name: 'IdClass'
			},{
				xtype: 'hidden',
				name: 'Id'
			},{
				xtype: 'hidden',
				name: 'ProcessInstanceId'
			},{
				xtype: 'hidden',
				name: 'WorkItemId'
			}],
// FIXME copied from EditablePanel: use that instead
/* BEGIN */
			switchFieldsToEdit: function() {
				var fields = this.items.items;
		    	for (var i=0;  i<fields.length; ++i) {
		    		var field = fields[i];
		    		if (field.resolveTemplate) {
		    			field.resolveTemplate();
		    		}
		    	}
		    }
/* END */
		});
		
		this.displayPanel = new Ext.form.FormPanel({
			plugins: [new CMDBuild.FormPlugin()],
			hideMode: 'offsets',
			region: 'center',
			autoHeight: true,
			autoWidth: true,
			trackResetOnLoad: true,
			labelAlign: "right",
			labelWidth: 200,
			defaults: { 
				labelWidth: 200
			},
			frame: false,
			border: false,			
			items:[{
				xtype: 'hidden',
				name: 'IdClass'
			}]
		});
		
		this.formContainer = new Ext.Panel({
			region: 'center',
			hideMode: 'offsets',
			layout: 'border',
			autoScroll: true,
			frame: true,
			border: true,
			style: {padding: '5px 5px 0 5px'},
			items: [this.displayPanel, this.actualForm]
		});
		
		this.customButtons = new Ext.Panel({
			region: 'east',
			hideMode: 'offsets',
			autoScroll: true,
			frame: false,
			border: false,
			style: {padding: '5px 5px 0 0'},
			bodyStyle: {background: CMDBuild.Constants.colors.blue.background},
			items: []
		});
		
		this.processStepName = new Ext.form.DisplayField();
		this.processStepCode = new Ext.form.DisplayField();
		
		Ext.apply(this, {
			frame: false,
			border:false,
			hideMode: 'offsets',
			style: {background: CMDBuild.Constants.colors.blue.background},
			tbar: [
				this.modifyCardAction,
				this.terminateProcessAction,
				'->','-',
				this.processStepName,'-',
				this.processStepCode, ' '
			],
			layout: 'border',
			items: [this.formContainer,this.customButtons],
			buttonAlign: 'center',
			buttons: [
				this.saveButton,
				this.advanceButton,
				this.cancelButton
			]
        });
		this.formFields = [];
		this.formDisplayFields = [];
		
		CMDBuild.Management.ActivityTab.superclass.initComponent.apply(this, arguments);

		this.subscribe('cmdb-init-processclass', this.initForClass, this);
		this.subscribe('cmdb-load-activity', this.onLoadActivity, this);
		this.subscribe('cmdb-new-process', this.newCard, this);
		this.subscribe('cmdb-enablemodify-activity', this.onEnableModifyCard, this);
		this.subscribe('cmdb-select-stateprocess', this.onSelectStateProcess, this);
		this.subscribe('cmdb-disable-modify', this.disableModify(), this);
		this.subscribe('cmdb-empty-activity', this.onEmptyActivity, this),
		
		this.actualForm.on('clientvalidation', this.onClientValidation, this);
		
		this.on('activate', function(p){
			Ext.ComponentMgr.get('activityopts_tab').disable();
		}, this);
		
		this.actualForm.on('show', this.syncSizeCombos, this);
	},

	onClientValidation: function(panel, valid) {
		var validActivity = valid && this.checkExtAttrValidation();
		this.advanceButton.setDisabled(!validActivity);
	},
	
	checkExtAttrValidation: function() {
		for (var i = 0, l=this.extAttrIds.length; i<l; i++) {
			var extAttr = Ext.getCmp(this.extAttrIds[i]);
			if (extAttr && !extAttr.isValid()) {
				return false;
			}
		};
		return true;
	},
	
	initForClass: function(eventParams) {
		if (!eventParams) {
			return;
		}
		this.currentClassId = eventParams.classId;
		this.currentCardId = -1;
		this.currentProcessInstanceId = '';

		this.customButtons.hide();
		
        this.removeExtendedAttributeButtons();
		this.disableAll();
		
		this.doLayout();
	},	
	
	onLoadActivity: function(eventParams) {
		var callback = this.loadActivity.createDelegate(this, [eventParams], true);		
		var eventIdClass = eventParams.record.data.IdClass;	
		if (this.idClassOfCurrentRecord != eventIdClass) {
			this.idClassOfCurrentRecord = eventIdClass;
			CMDBuild.Management.FieldManager.loadAttributes(this.idClassOfCurrentRecord, callback, true);
		} else {
			this.loadActivity(undefined, eventParams);
		}
	},
	
	loadActivity: function(attributeList, eventParams) {
    	this.displayPanel.show();
    	this.actualForm.hide();

		if (attributeList) {
			this.attributeList = attributeList;
		}

		if (eventParams.isnew) {
			//force the status to open.running for the new activity
			activityStatus = 'open.running';
		} else {
			var activityStatus = this.wfmodule.getFlowStatusCodeById(eventParams.record.data.FlowStatus);
			this.onSelectStateProcess({state:activityStatus});
		}		

		if( activityStatus == 'open.running' ) {
			this.loadOpenActivity(eventParams);
		} else {
			this.loadClosedActivity(eventParams);
		}
		
		this.updateTabBarInfo(eventParams);
		this.autoEditMode = false;
		this.hideTerminateProcessActionIfNotStoppable();
		this.publish('cmdb-wf-layouthack');
		this.doLayout();
	},
	
	loadOpenActivity: function(eventParams) {
		this.isOldModeClose = false;
		this.currentRecord = eventParams.record;
		this.currentCardId = eventParams.record.data.Id;
		this.currentProcessInstanceId = eventParams.record.data.ProcessInstanceId;
		this.currentWorkItemId = eventParams.record.data.WorkItemId;
		this.activityDescription = eventParams.record.data.ActivityDescription;
		this.isStoppable = eventParams.record.data.stoppable;
		this.deletePreviousFields();
		
		this.formFields = [];
		this.formDisplayFields = [];
		
		for(var i in this.attributeList) {
			var attribute = this.attributeList[i];
			var index = eventParams.record.data[attribute.name + "_index"];
			if(index != undefined) {
				if(index > -1) {
					var mode = eventParams.record.data[ attribute.name + "_type" ];
					//list of possible _type
					var modeConvertionMatrix = {
						VIEW: "read",
	                    UPDATE: "write",
	                    REQUIRED: "required"
					};
					attribute.fieldmode = modeConvertionMatrix[mode];
					this.formFields[index] = CMDBuild.Management.FieldManager.getFieldForAttr(attribute);
					this.formDisplayFields[index] = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, true); //true to have a displayField
				}
			}
		}
		for(var i=0;i<this.formFields.length;i++) {
			if(this.formFields[i]) {
                this.actualForm.add(this.formFields[i]);
                this.displayPanel.add(this.formDisplayFields[i]);
			}
		}
		
		this.displayPanel.getForm().loadRecord(eventParams.record);
		this.manageEditability(eventParams);		
	},	
	
	manageEditability: function(eventParams) {
		if (eventParams.record.data.editableByCurrentUser) {			
			this.editable = true;
            this.registerExtAttributes( eventParams.record.data.CmdbuildExtendedAttributes );
            if (eventParams.edit || this.autoEditMode) {            	
                this.enableModify();
            } else {
                this.onCancelButton(this.currentState);
            }
		} else {
			this.editable = false;
			this.disableAll();
		}		
	},
	
	loadClosedActivity: function(eventParams) {
		this.editable = false;
		if(this.isOldModeClose == false) {
			this.loadOldMode();
		}
		//load the fields in eventParams
		this.currentRecord = eventParams.record;
		this.currentCardId = eventParams.record.data.Id;
        this.currentProcessInstanceId = '';
        this.currentWorkItemId = '';
        this.actualForm.getForm().reset();
        this.actualForm.getForm().loadRecord(eventParams.record);
        this.displayPanel.getForm().reset();
        this.displayPanel.getForm().loadRecord(eventParams.record);
        this.disableAll();
	},

	loadOldMode: function() {
		// Remove ext attrs
		this.customButtons.hide();
        this.removeExtendedAttributeButtons();
        this.findParentByType('activitytabpanel').getComponent('activityopts_tab').disable();
		this.deletePreviousFields();
		this.createNewFieldsAndAddToActualForm();
		this.isOldModeClose = true;
		this.doLayout();
	},
	
	createNewFieldsAndAddToActualForm: function() {
		this.formFields = [];
		this.formDisplayFields = [];
        for (var i=0; i<this.attributeList.length; i++) {
            var attribute = this.attributeList[i];
            if (attribute.name != "Notes") {
                var displayField = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, true);
                if (displayField) {
                	this.formDisplayFields.push(displayField);
                	this.displayPanel.add(displayField);
                }
                var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute);
                if (field){
                	this.formFields.push(field);
                    this.actualForm.add(field);
                }
            }
        }
	},

	newCard: function(eventParams) {
		this.currentCardId = -1;
		Ext.Ajax.request({
			url: 'services/json/management/modworkflow/getstartactivitytemplate',
			method : 'POST',
			params : {
				idClass : eventParams.classId,
				id : -1
			},
			scope : this,
			success : function(response) {
				this.publish('cmdb-load-activity',{edit:true,isnew:true,record: Ext.util.JSON.decode(response.responseText)});
			},
			failure : function(response, options) {
				CMDBuild.Msg.error(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.generic_error, true);
			}
		});
	},

	deletePreviousFields: function() {
		for (var i=0; i<this.formFields.length; i++) {
			var field = this.formFields[i];
			this.actualForm.remove(field);
		}
		for (var i=0; i<this.formDisplayFields.length; i++) {
			var displayForms = this.formDisplayFields[i];
			this.displayPanel.remove(displayForms);
		}
	},
	
	createNewFields: function() {
		this.formFields = [];
		this.formDisplayFields = [];
		
		for (var i=0; i<this.attributeList.length; i++) {
			var attribute = this.attributeList[i];
			if (attribute.name != "Notes") {
				var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute);
				var displayField = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, true);
				this.formFields.push(field);
				this.formDisplayFields.push(displayField);
			}
		};
	},
		
	setOptionsDisabled: function(disabled) {
		if (this.customButtons.items) {
            this.customButtons.items.each(function(b) {
                b.setDisabled(disabled);
            }, this.customButtons);
        }
	},

	disableModify: function() {        
		this.disableAll();
		if (this.currentCardId != -1) {
            this.modifyCardAction.enable();
		}
    	this.actualForm.hide();
    	this.displayPanel.show();
    },

    updateTabBarInfo : function(eventParams) {
    	var process = eventParams.record.data;    	
		var tbar = this.getTopToolbar();
		var name = "";
		var code = "";
		if (this.flowStatusIsOpen()) {
			if(process.activityPerformerName)           
	            name = eventParams.record.data.activityPerformerName;
	        if(process.Code)
	        	code = eventParams.record.data.Code;
		}
		this.processStepName.setValue(name);
		this.processStepCode.setValue(code);
	},
	
	clearTabBarInfo: function() {
		this.processStepName.setValue("");
		this.processStepCode.setValue("");
	},
    
	flowStatusIsOpen: function() {
		var firstPart = this.currentState.state.split(".", 1);	
		return firstPart == "open";
	}, 
	
    enableModify: function() {
		this.displayPanel.hide();
		this.actualForm.show();
    	this.enableAll();
    	this.disableActions();
    	this.actualForm.getForm().loadRecord(this.currentRecord);
    	this.actualForm.switchFieldsToEdit();
    },
    
    hideTerminateProcessActionIfNotStoppable: function() {
    	if (this.isStoppable)
    		this.terminateProcessAction.show();
    	else 
    		this.terminateProcessAction.hide();
    },
    
    onTerminateProcess: function() {
    	var title = this.translation.abort_card;
		var msg = this.translation.abort_card_confirm;
		var url = 'services/json/management/modworkflow/abortprocess';
		var params = {
                WorkItemId: this.currentWorkItemId,
                ProcessInstanceId: this.currentProcessInstanceId
		};
		this.sendRequest(title, msg, url, params);
    },

    sendRequest: function(title, msg, url, params) {
		var tthis = this;		
		Ext.Msg.confirm(
            title,
            msg,
            function(btn) {
                if (btn != 'yes')
                    return;
                CMDBuild.LoadMask.get().show();
        		Ext.Ajax.request({
        			url : url,
        			params : params,
        			method : 'POST',
        			scope : this,
        			success : function(response) {
        				CMDBuild.LoadMask.get().hide();
        				var ret = Ext.util.JSON.decode(response.responseText);
        				if(ret.success){
        					tthis.publish('cmdb-reload-activity');
                            tthis.actualForm.getForm().reset();
                            tthis.displayPanel.getForm().reset();
        				}
        			},
        			failure : function(response, options) {
        				CMDBuild.LoadMask.get().hide();
        				CMDBuild.Msg.error(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.generic_error, true);
        			}
          	 	});
            }
	   );
	},

	advanceCard: function() {		
        this.disableButtons();
        if(this.currentProcessInstanceId == 'tostart') {
			this.startSaveAdvance(true);
		} else {
            this.saveAdvanceCard(true);
		}
    },
    
    saveCard: function() {    	
    	this.disableButtons();
    	if(this.currentProcessInstanceId == 'tostart') {
            this.startSaveAdvance(false);
        } else {
            this.saveAdvanceCard(false);
        }
    },
       
    //called when the process is new and needs to be created first
    startSaveAdvance: function(isAdvance) {
    	//start the process
    	this.currentCardId = -1;
    	CMDBuild.LoadMask.get().show();
        Ext.Ajax.request({
            url : 'services/json/management/modworkflow/startprocess',
            method : 'POST',
            params : {
                idClass : this.idClassOfCurrentRecord,
                id : -1
            },
            scope : this,
            success : function(response) {
            	CMDBuild.LoadMask.get().hide();
            	var record = Ext.util.JSON.decode(response.responseText);
            	this.currentCardId = record.data.Id;
                this.currentProcessInstanceId = record.data.ProcessInstanceId;
                this.currentWorkItemId = record.data.WorkItemId;
                
                var theForm = this.actualForm.getForm();
                theForm.findField("Id").setValue(this.currentCardId);
                theForm.findField("ProcessInstanceId").setValue(this.currentProcessInstanceId);
                theForm.findField("WorkItemId").setValue(this.currentWorkItemId);
                this.saveAdvanceCard(isAdvance);
            },
            failure : function(response, options) {
            	CMDBuild.LoadMask.get().hide();
            	CMDBuild.Msg.error(CMDBuild.Translation.errors.error_message, CMDBuild.Translation.errors.generic_error, true);
            }
        });
    },

	// save variables in activity or save them and advance the process to the
	// next activity
	saveAdvanceCard : function(isAdvance) {
		var theUrl = isAdvance
				? 'services/json/management/modworkflow/advanceprocess'
				: 'services/json/management/modworkflow/updateactivity';
		var form = this.actualForm.getForm();
			// build a boolean array with extattr ids
			var waitMap = this.buildExtAttrsWaitMap();
			// call this function with the id of an extattr
			var onExtAttrsSaved = function(id, success) {
			if (id) { // called withoud an id if there are no extended attributes
				this.updateWaitMap(waitMap, id, success);
			}
			if (this.extAttrDone(waitMap)) {
				if (this.extAttrSucceded(waitMap)) {
					CMDBuild.LoadMask.get().show();
					form.submit( {
						method : 'POST',
						url : theUrl,
						timeout: 90,
						scope : this,
						clientValidation: isAdvance, //to force the save request
						success : function() {
							CMDBuild.LoadMask.get().hide();
							this.autoEditMode = true; //used by loadActivity, that sets it back to false
							this.actualForm.getForm().reset();
							this.publish('cmdb-reload-activity', {
								"Id" : this.currentCardId,
								"notChangeStatusAfterSave": true
							});
						},
						failure : function(response, options) {
							CMDBuild.LoadMask.get().hide();
							this.enableButtons();
						}
					});
				} else {
					this.enableButtons();
					CMDBuild.Msg.error(null, CMDBuild.Translation.errors.reasons.WF_CANNOT_COMPLETE_WORKITEM, true);
				}
			}
		};
		this.saveAllExtAttrs(onExtAttrsSaved.createDelegate(this), isAdvance);
	},

	// build a boolean array with extattr ids
	// values: true = waiting, false = failed, undefined = succeded
	buildExtAttrsWaitMap: function() {
		var waitMap = {};
		Ext.each(this.extAttrIds, function(id) {
			waitMap[id] = true;
		});
		return waitMap;
	},

	updateWaitMap: function(waitMap, id, success) {
		if (success) {
			CMDBuild.log.info("Extended attribute " + id + " succeded");
			waitMap[id] = undefined;
		} else {
			CMDBuild.log.info("Extended attribute " + id + " failed");
			waitMap[id] = false;
		}
	},

	// returns if every extattr has been processed
	extAttrDone: function(waitMap) {
		for (var i in waitMap) {
			if (waitMap[i]) {
				CMDBuild.log.debug("** Still waiting for some extended attributes");
				return false;
			}
		}
		CMDBuild.log.debug("** All extended attributes saved");
		return true;
	},

	extAttrSucceded: function(waitMap) {
		for (var i in waitMap) {
			if (waitMap[i] === false) {
				CMDBuild.log.debug("** Some extended attributes failed");
				return false;
			}
		}
		CMDBuild.log.debug("** All extended attributes succeded");
		return true;
	},

	saveAllExtAttrs : function(fn, isAdvance) {
		// if no extattr is defined, simply call the fn function
		if (this.extAttrIds.length == 0) {
			fn();
		} else {
			Ext.each(this.extAttrIds, function(id) {
				this.saveExtAttr(id, this.actualForm, fn, isAdvance);
			}, this);
		}
	},
	
	saveExtAttr : function(identifier, frm, fn, isAdvance) {
		var extAttr = Ext.getCmp(identifier);
		if (extAttr) {
			extAttr.setup(this.currentProcessInstanceId, this.currentWorkItemId);
			extAttr.save(frm, fn, isAdvance);
		}
	},

    registerExtAttributes: function(extAttrDefs) {
    	this.customButtons.hide();
    	this.formContainer.doLayout();
    	Ext.getCmp('activityopts_tab').disable();
    	this.removeExtendedAttributeButtons();
    	
    	if(!extAttrDefs || extAttrDefs.length == 0){ return; }
        Ext.each(extAttrDefs, function(item) {
    	   this.customButtons.add(new Ext.Button({
    	   	   text: item.btnLabel || CMDBuild.Translation.management.modworkflow[item.labelId],
    	   	   style: 'padding: 2px 3px',
    	       scope: this,
    	       disabled: true,
    	       handler: function() {
    		   		var extAttr = Ext.getCmp(item.identifier);
    		   		extAttr.fireEvent('activate-extattr'); //BaseExtendedAttribute activates the options tab
    	       }
    	   }));
    	   this.extAttrIds.push(item.identifier);
    	}, this);
        
        this.customButtons.show();
        this.customButtons.doLayout();
        this.manageExtAttrButtonsWidth(this.customButtons);
        this.doLayout();
    },

    removeExtendedAttributeButtons: function() { 
    	this.customButtons.removeAll(true);
    	this.extAttrIds = [];
    },

    manageExtAttrButtonsWidth: function(btns) {
    	var maxW = 0;
    	btns.items.each(function(item){
    		var w = item.getEl().getComputedWidth() + item.getEl().getFrameWidth('lr');
    	   if(w > maxW) {
    	   	   maxW = w;
    	   }
    	});
    	btns.items.each(function(item){ 
    	   item.getEl().setWidth(maxW);
        });
    	this.customButtons.setWidth(maxW); //to fix the width of the panel, auto width does not work with IE7
    },
    
    onEnableModifyCard: function() {
    	if(this.editable)
           this.enableModify();
    },
    
    onSelectStateProcess: function(stateProcess) {
    	this.currentState = stateProcess;
    	this.disableAll();
    	this.enableActionsByCurrentState();
    },
    
    onEmptyActivity: function() {
    	this.deletePreviousFields();
    	this.disableActions();
    	this.clearTabBarInfo();
    	this.removeExtendedAttributeButtons();
    	this.autoEditMode = false;
    },
    
    onCancelButton: function() {    	
    	this.disableModify();
    	if (this.currentCardId != -1) {
    		this.enableActionsByCurrentState();
    	} else {
    		this.deletePreviousFields();
    		this.clearTabBarInfo();
    	}
    },    
    
    enableActionsByCurrentState: function() {
    	if (this.currentState.state == "open.running") {
    		this.enableActions();
    	} else if(this.currentState.state == "open.not_running.suspended"){
    		this.terminateProcessAction.enable();
    	}
    },
    
    disableAll :function() {
    	this.disableActions();
        this.disableButtons();
        this.actualForm.setFieldsDisabled();
        this.setOptionsDisabled(true);
    },
    
    enableAll: function() {
    	this.enableActions();
    	this.enableButtons();
        this.actualForm.setFieldsEnabled();
        this.setOptionsDisabled(false);
    },
    
    enableActions: function() {
        this.modifyCardAction.enable();
        if (this.isStoppable) {
    		this.terminateProcessAction.enable();
        }
    },
    
    disableActions: function() {
    	this.modifyCardAction.disable();
    	this.terminateProcessAction.disable();
    },
    
    enableButtons: function() {
    	this.actualForm.monitorValid = true;
    	this.actualForm.on('clientvalidation', this.onClientValidation, this);
        this.saveButton.enable();
        this.advanceButton.enable();
        this.cancelButton.enable();        
    },
    
    disableButtons: function() {
    	this.actualForm.monitorValid = false;
    	this.actualForm.un('clientvalidation', this.onClientValidation, this);
    	this.saveButton.disable();
        this.advanceButton.disable();
        this.cancelButton.disable();
    },
    
    syncSizeCombos: function() {
    	for (var i = this.formFields.length; i-- > 0;) {
    		var field = this.formFields[i];
    		if (field && field.grow) {
    			if (field.syncSize) {
    				field.syncSize();
	    		} 
	     		if(field.growSizeFix){
	     			field.growSizeFix();
	    		}
    		}
    	}
    }
});
Ext.reg('activitytab', CMDBuild.Management.ActivityTab);