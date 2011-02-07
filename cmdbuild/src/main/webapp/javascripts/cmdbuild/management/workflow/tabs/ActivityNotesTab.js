CMDBuild.Management.ActivityNotesTab = Ext.extend(Ext.Panel, {
	translation : CMDBuild.Translation.management.modcard,
    eventtype: 'activity',
    eventmastertype: 'processclass',
    currentCardId: -1,
    currentProcessInstanceId: -1,
    loaded: false,

	initComponent : function() {
		this.saveButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.save,
			name: 'saveButton',
			formBind : true,
			handler : this.saveNote,
			scope : this
		});

		this.cancelButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.abort,
			name: 'cancelButton',
			handler : this.onAbort,
			scope : this
		});

		this.htmlEditor = new Ext.form.HtmlEditor({
			name : 'Notes',
            hideLabel: true,
            enableLinks: false,
            enableSourceEdit: false,
            enableFont: false
		});
		
		this.form = new Ext.form.FormPanel({
			frame: false,
			border: false,
			style: {
				"border-bottom": "1px "+CMDBuild.Constants.colors.blue.border+" solid"
			},
			layout: 'fit',
            id: 'activityNotesForm',
            items: [this.htmlEditor, {
                name: 'IdClass',
                xtype: 'hidden'
            },{
                name: 'Id',
                xtype: 'hidden'
            }]
        });
		
		Ext.apply(this, {
			frame: false,
			style: {background: CMDBuild.Constants.colors.blue.background},
			layout: 'fit',
			tbar: [
				new CMDBuild.Management.GraphActionHandler().getAction()
			],
			items: [this.form],
			buttonAlign: 'center',
			buttons: [this.saveButton, this.cancelButton]
        });

		CMDBuild.Management.CardNotesTab.superclass.initComponent.apply(this, arguments);
		
		this.on('show', this.actualLoad, this);
		this.on('hide', this.disablePanelIfNotCompleted, this);
	},

	initForClass: function(eventParams) {
		this.disable();
		if (eventParams) {
			this.currentClassPrivileges = Ext.apply({
				create: false,
				write: false
			}, eventParams.privileges);
		}
	},

	loadActivity: function(activity) {
		// this.wfmodule is inserted by ActivityTabPanel in the object params
		this.activityStatusCode = this.wfmodule.getFlowStatusCodeById(activity.record.data.FlowStatus);
		if (this.activityStatusCode != 'closed.completed') {
			this.disable();
		} else {
			this.enable();
			this.disableModify();
		}
		this.loaded = false;
		this.currentCardPrivileges = {
			write: activity.record.data.editableByCurrentUser
		};
		this.record = activity.record;
		
		this.currentCardId = activity.record.data.Id;
		this.currentProcessId = activity.record.data.ProcessInstanceId;
		if(this.isVisible()) {
			this.actualLoad();
		}
	},

	disablePanelIfNotCompleted: function() {
		if (this.activityStatusCode != 'closed.completed') {
			this.disable();
		}
	},

	actualLoad: function() {
		if(this.loaded == true) {
			CMDBuild.log.info('info already loaded');
			return;
		}
		CMDBuild.log.info('actual load the process notes info');
        var form = this.form.getForm();
        
        if(form.items.getCount() == 0) {
        	CMDBuild.log.info('form does not have fields! postponing!');
        	var t = this;
        	window.setTimeout(function(){t.actualLoad();},200);
        } else {
        	CMDBuild.log.info('loading notes...');
            form.reset();
            form.loadRecord(this.record);
            this.loaded = true;
        }
    },

	reloadCard: function(eventParams) {
		this.enable();
	},
	
	onAbort: function() {
		this.loaded = false;
		this.actualLoad();
	},
	
	disableModify: function() {
		this.saveButton.disable();
		this.cancelButton.disable();		
		this.form.disable();
	},

	enableModify: function() {
		this.saveButton.enable();
		this.cancelButton.enable(); 
		this.form.enable();
	},

	saveNote: function() {
		var form = this.form.getForm();
		var t = this;
		
		CMDBuild.LoadMask.get().show();
		if (form.isValid()) {
			form.submit({
				method : 'POST',
				url : 'services/json/management/modcard/updatecard',				
				scope: t,
				success : function() {
					CMDBuild.LoadMask.get().hide();
					this.disableModify();
					var actGrid = Ext.getCmp('activitylist_grid');
					var idx = actGrid.store.find('Id',t.currentCardId);
					var record = actGrid.store.getAt(idx);
					var value = this.htmlEditor.getValue();
					record.json.Notes = value;
					Ext.getCmp('activity_tab').show();
					CMDBuild.log.info('notes', value);
				},
				failure: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
    }
});
Ext.reg('activitynotestab', CMDBuild.Management.ActivityNotesTab);
