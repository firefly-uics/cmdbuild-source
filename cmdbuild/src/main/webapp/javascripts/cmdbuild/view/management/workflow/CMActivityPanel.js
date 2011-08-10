(function() {
	var tr =  CMDBuild.Translation.management.modworkflow,
		modeConvertionMatrix = {
			VIEW: "read",
			UPDATE: "write",
			REQUIRED: "required"
		};

	Ext.define("CMDBuild.view.management.workflow.CMActivityPanel", {
		extend: "Ext.panel.Panel",

		initComponent: function() {

			this.activityForm = new CMDBuild.view.management.workflow.CMActivityPanel.Form({
				region: "center"
			});

			this.wfWidgetsPanel = new CMDBuild.view.management.workflow.CMActivityPanel.WFWidgetsPanel({
				region: 'east',
				hideMode: 'offsets',
				cls: "cmborderleft",
				autoScroll: true,
				frame: true,
				border: false,
				items: []
			});

			this.wfWidgetsPanel.hide();

			this.layout = "border";
			this.items = [this.activityForm, this.wfWidgetsPanel];
			this.callParent(arguments);

			this.activityForm.on("cmeditmode", function() {
				this.fireEvent("cmeditmode");
			}, this);

			this.activityForm.on("cmdisplaymode", function() {
				this.fireEvent("cmdisplaymode");
			}, this);

		},

		/*
		 o = {
				editMode: editMode,
				cb: cb,
				scope: this
			}
		 */
		updateForActivity: function(activity, o) {
			this.activityForm.activityToLoad = activity;
			this.activityForm.updateInfo(activity);
			this.wfWidgetsPanel.updateWidgets(activity);

			var loadCard = Ext.bind(function() {
				this.activityForm.loadCard(activity);
				if (o.editMode) {
					this.editMode();
				} else {
					this.displayMode(enableCMTbar = true);
				}
				if (o.cb) {
					var scope = o.scope || this;
					o.cb.call(this);
				}
			}, this);

			//we need to reload always the fields
			_CMCache.getAttributeList(
				activity.data.IdClass,
				Ext.bind(function cb(a) {
					this.activityForm.buildActivityFields(a);
					loadCard();
				}, this)
			);
		},

		updateForClosedActivity: function(activity) {
			this.activityForm.activityToLoad = activity;
			this.activityForm.updateInfo(activity);
			this.wfWidgetsPanel.updateWidgets(activity);
			
			_CMCache.getAttributeList(
				activity.data.IdClass,
				Ext.bind(function cb(a) {
					this.activityForm.fillForm(a, editMode = false);
					this.activityForm.loadCard(activity);
				}, this)
			);
		},

		clearForNoActivity: function() {
			this.activityForm.removeAll(destroy = true);
			this.activityForm.updateInfo();
			this.wfWidgetsPanel.removeAll(destroy = true);
			this.wfWidgetsPanel.hide();
			this.displayMode();
		},

		getForm: function() {
			return this.activityForm.getForm();
		},

		getValues: function() {
			return this.activityForm.getValues();
		},

		displayMode: function(enableCMTbar) {
			this.activityForm.displayMode(enableCMTbar);
			this.wfWidgetsPanel.displayMode();
		},

		editMode: function() {
			this.activityForm.editMode();
			this.wfWidgetsPanel.editMode();
		},

		reset: function() {
			this.activityForm.reset();
		},

		getInvalidAttributeAsHTML: function() {
			return this.activityForm.getInvalidAttributeAsHTML();
		}
	});

	Ext.define("CMDBuild.view.management.workflow.CMActivityPanel.WFWidgetsPanel", {
		extend: "Ext.panel.Panel",
		initComponent: function() {
			Ext.apply(this, {
				frame: false,
				border: false,
				layout : 'vbox',
				bodyCls: "x-panel-body-default-framed",
				bodyStyle: {
					padding: "30px 5px 0 5px"
				}
			});
			this.callParent(arguments);
		},

		updateWidgets: function(activity) {
			var data = activity.raw || activity.data,
				widgetsDefinition = data.CmdbuildExtendedAttributes || [],
				me = this;

			this.removeAll();

			if (widgetsDefinition.length > 0) {
				this.show();
				Ext.each(widgetsDefinition, function(item) {
					me.add(new Ext.Button({
						text: item.btnLabel || CMDBuild.Translation.management.modworkflow[item.labelId],
						widgetDefinition: item,
						handler: function() {
							me.fireEvent("cm-wfwidgetbutton-click", item);
						},
						margins:'0 0 5 0'
					}));
				});
				me.updateButtonsWidth();
			} else {
				this.hide();
			}
		},

		updateButtonsWidth: function () {
			var maxW = 0;
			this.items.each(function(item) {
				var w = item.getWidth();
				if (w > maxW) {
					maxW = w;
				}
			});

			this.items.each(function(item) {
				item.setWidth(maxW);
			});
			// to fix the width of the panel, auto width does
			// not work with IE7
			this.setWidth(maxW + 10); // 10 is the pudding
		},

		displayMode: function() {
			this.items.each(function(i) {
				i.disable();
			});
		},

		editMode: function() {
			this.items.each(function(i) {
				i.enable();
			});
		}
	});
	
	Ext.define("CMDBuild.view.management.workflow.CMActivityPanel.Form", {
		extend: "CMDBuild.view.management.classes.CMCardPanel",

		buildTBar: function() {
			this.cmTBar = [
				this.modifyCardButton = new Ext.button.Button({
					iconCls : "modify",
					text : tr.modify_card
				}),
				this.deleteCardButton = new Ext.button.Button({
					iconCls : "delete",
					text : tr.delete_card
				}),
				'->','-',
				this.processStepName = new Ext.button.Button({
					overCls: Ext.button.Button.baseCls,
					pressedCls: Ext.button.Button.baseCls,
					disable: Ext.emptyFn
				}),
				'-',
				this.processStepCode = new Ext.button.Button({
					overCls: Ext.button.Button.baseCls,
					pressedCls: Ext.button.Button.baseCls,
					disable: Ext.emptyFn
				}),
				' '
			];
		},

		buildButtons: function() {
			this.cmButtons = [
				this.saveButton = new Ext.button.Button({
					text: CMDBuild.Translation.common.buttons.save
				}),

				this.advanceButton = new Ext.button.Button({
					text: CMDBuild.Translation.common.buttons.workflow.advance
				}),

				this.cancelButton = new Ext.button.Button( {
					text: this.readOnlyForm ? CMDBuild.Translation.common.btns.close : CMDBuild.Translation.common.btns.abort
				})
			];
		},

		updateInfo : function(activity) {
			activity = activity || {};
			var data = activity.raw || activity.data || {};

			this.processStepName.setText(data.activityPerformerName || "");
			this.processStepCode.setText(data.Code || "");
		},

		buildActivityFields: function(attributes, editMode) {
			var cleanedAttrs = [],
				data = this.activityToLoad.raw || this.activityToLoad.data; // if is a template given from the server, has not the raw data, so use the data

			for (var a in attributes) {
				a = attributes[a];
				var index = data[a.name + "_index"];
				if (index != undefined && index > -1) {
					var mode = data[a.name + "_type"];
					a.fieldmode = modeConvertionMatrix[mode];
					cleanedAttrs[index] = a;
				}
			}

			this.fillForm(cleanedAttrs, editMode);
		}
	});

})();

/*
CMDBuild.Management.ActivityTab = Ext.extend(Ext.Panel, {
	translation: CMDBuild.Translation.management.modworkflow,
	extAttrIds: [],	
	currentState: {state: "open.running"},
	autoEditMode: false,	//flag to set the form in modify-mode after advance process
	isOldModeClose: false,	//flag to detect the mode of the previous card
	editable: false,
	hideMode: 'offsets',
	
	constructor: function() {
		this.terminateProcessAction = new Ext.Action({
      		iconCls: 'delete',
      		text: this.translation.delete_card,
      		handler: function() {
				this.fireEvent("terminate_process", {
		                WorkItemId: this.currentWorkItemId,
		                ProcessInstanceId: this.currentProcessInstanceId
				});
			},
      		scope: this
    	});
		
    	this.modifyCardAction = new Ext.Action({
      		iconCls : 'modify',
      		text : this.translation.modify_card,
			handler : this.enableModify,
      		scope: this
    	});

		this.saveButton = new Ext.Button({
            text: CMDBuild.Translation.common.buttons.workflow.save,
            name: 'saveButton',
            handler: function() {
                this.onSave(isAdvance = false, skipValidation = true);
            },
            scope: this
        });

        this.advanceButton = new Ext.Button({
            text: CMDBuild.Translation.common.buttons.workflow.advance,
            handler: function() {
        		this.autoEditMode = true;
                this.onSave(isAdvance = true);
            },
            scope: this
        });

		this.cancelButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.abort,
			name: 'cancelButton',
			handler : this.onCancelButton,
			scope : this
		});
		
		this.actualForm = new CMDBuild.Management.CardForm({
			plugins: new CMDBuild.FieldSetAddPlugin(),
			hideMode: 'offsets',
			autoHeight: true,
			autoWidth: false,
			region: 'center',
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
			//BEGIN
			switchFieldsToEdit: function() {
				var fields = this.items.items;
		    	for (var i=0;  i<fields.length; ++i) {
		    		var field = fields[i];
		    		if (field.resolveTemplate) {
		    			field.resolveTemplate();
		    		}
		    	}
		    }
			// END
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
		CMDBuild.Management.ActivityTab.superclass.constructor.apply(this, arguments);
	},
	
	initComponent : function() {
		this.formFields = [];
		this.formDisplayFields = [];
		
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

		CMDBuild.Management.ActivityTab.superclass.initComponent.apply(this, arguments);
		this.subscribe('cmdb-disable-modify', this.disableModify(), this);
		this.actualForm.on('show', this.syncSizeCombos, this);
	},
	
	getForm: function() {
		return this.actualForm.getForm();
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
	
	loadActivity: function(eventParams) {
		var callback = this._loadActivity.createDelegate(this, [eventParams], true);
		var eventIdClass = eventParams.record.data.IdClass;
		if (this.idClassOfCurrentRecord != eventIdClass) {
			this.idClassOfCurrentRecord = eventIdClass;
			CMDBuild.Management.FieldManager.loadAttributes(this.idClassOfCurrentRecord, callback, true);
		} else {
			this._loadActivity(undefined, eventParams);
		}
	},
	
	_loadActivity: function(attributeList, eventParams) {
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

		if (activityStatus == 'open.running' ) {
			this.loadOpenActivity(eventParams);
		} else {
			this.loadClosedActivity(eventParams);
		}
		
		this.updateTabBarInfo(eventParams);		
		this.hideTerminateProcessActionIfNotStoppable();
		this.doLayout();
	},
	
	loadOpenActivity: function(eventParams) {
		if (this.currentCardId != eventParams.record.data.Id) {
			// deny the auto edit if the card is selected after the termination
			// of a process
			this.autoEditMode = false;
		}
		
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
		
		for (var i in this.attributeList) {
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
		
		for (var i=0;i<this.formFields.length;i++) {
			if(this.formFields[i]) {
                this.actualForm.add(this.formFields[i]);
                this.displayPanel.add(this.formDisplayFields[i]);
			}
		}
		
		this.displayPanel.getForm().loadRecord(eventParams.record);
		
		// listened by the ActivityTabPanel to now when to call manageEditability
		this.fireEvent("CMActivityLoaded");
	},	
	
	manageEditability: function(eventParams) {
		if (eventParams.record.data.editableByCurrentUser) {			
			this.editable = true;
            this.buildExtAttributeButtons( eventParams.record.data.CmdbuildExtendedAttributes );
            if (eventParams.edit || this.autoEditMode) {          	
                this.enableModify();
            } else {
                this.onCancelButton(this.currentState);
            }
		} else {
			this.editable = false;
			this.disableAll();
		}
		this.autoEditMode = false;
	},
	
	loadClosedActivity: function(eventParams) {
		this.editable = false;
		if (this.isOldModeClose == false) {
			this.loadOldMode();
		}
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
		this.customButtons.hide();
        this.removeExtendedAttributeButtons();        
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
		this.autoEditMode = false;
    	this.actualForm.hide();
    	this.displayPanel.show();
    },
	
	clearTabBarInfo: function() {
		this.processStepName.setValue("");
		this.processStepCode.setValue("");
	},
    
	flowStatusIsOpen: function() {
		var firstPart = this.currentState.state.split(".", 1);	
		return firstPart == "open";
	}, 
	
    hideTerminateProcessActionIfNotStoppable: function() {
    	if (this.isStoppable) {
    		this.terminateProcessAction.show();
    	} else { 
    		this.terminateProcessAction.hide();
    	}
    },
    
    onSave: function(isAdvance, skipValidation) {
		this.fireEvent("save", {
			isAdvance: isAdvance,
			idClass: this.idClassOfCurrentRecord,
			toStart: this.currentProcessInstanceId == 'tostart',
			ProcessInstanceId: this.currentProcessInstanceId,
    		WorkItemId: this.currentWorkItemId,
			skipValidation: skipValidation
		});
    },
    
    processStarted: function(process) {
    	this.currentCardId = process.Id;
        this.currentProcessInstanceId = process.ProcessInstanceId;
        this.currentWorkItemId = process.WorkItemId;
        
        var form = this.actualForm.getForm();
        form.findField("Id").setValue(this.currentCardId);
        form.findField("ProcessInstanceId").setValue(this.currentProcessInstanceId);
        form.findField("WorkItemId").setValue(this.currentWorkItemId);        
    },

    getInvalidAttributeAsHTML: function() {
    	return this.actualForm.getInvalidAttributeAsHTML();
    },
    
	// save variables in activity or save them and advance the process to the next activity
    updateActivity : function(isAdvance) {
		CMDBuild.LoadMask.get().show();
		this.actualForm.getForm().submit({
			method : 'POST',
			url : "services/json/management/modworkflow/updateactivity",
			timeout: 90,
			params: {
				advance: isAdvance
			},
			scope : this,
			clientValidation: isAdvance, //to force the save request
			success : function() {
				CMDBuild.LoadMask.get().hide();
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
    },
	
    buildExtAttributeButtons: function(extAttrDefs) {
    	this.customButtons.hide();
    	this.formContainer.doLayout();    	
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
    		   		extAttr.onActivation(); //BaseExtendedAttribute activates the options tab
    	       }
    	   }));
    	   this.extAttrIds.push(item.identifier);
    	}, this);

        manageExtAttrButtonsWidth.call(this);
        this.doLayout();
        
        function manageExtAttrButtonsWidth() {
            this.customButtons.show();
            this.customButtons.doLayout();
            
        	var maxW = 0;
        	this.customButtons.items.each(function(item){
        		var w = item.getEl().getComputedWidth() + item.getEl().getFrameWidth('lr');
        		if(w > maxW) {
        			maxW = w;
        		}
        	});
        	
        	this.customButtons.items.each(function(item){ 
        	   item.getEl().setWidth(maxW);
            });
        	//to fix the width of the panel, auto width does not work with IE7
        	this.customButtons.setWidth(maxW);
        }
    },

    removeExtendedAttributeButtons: function() { 
    	this.customButtons.removeAll(true);
    	this.extAttrIds = [];
    },
    
    onEnableModifyCard: function() {
    	if (this.editable) {
           this.enableModify();
    	}
    },
    
    onSelectStateProcess: function(stateProcess) {
    	this.currentState = stateProcess;
    	this.disableAll();
    	this.enableActionsByCurrentState();
    },
    
    onEmptyActivityGrid: function() {
    	this.deletePreviousFields();    	
    	this.clearTabBarInfo();
    	this.removeExtendedAttributeButtons();
    	this.disableAll();
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

});
*/