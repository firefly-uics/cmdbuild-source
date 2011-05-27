CMDBuild.Management.CardNotesTab = Ext.extend(Ext.Panel, {
	translation : CMDBuild.Translation.management.modcard,
    eventtype: 'card',
    eventmastertype: 'class',
    withButtons: true, // used in the windows to have specific buttons
    
	initComponent : function() {
    	this.modifyNoteAction = new Ext.Action({
      		iconCls : 'modify',
      		text : this.translation.modify_note,
			handler : this.enableModify,
      		scope: this
    	});

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
			handler : this.disableModify,
			scope : this
		});
		
		//used if the event type is activity
		this.backToActivityButton = new Ext.Button({
            text: CMDBuild.Translation.common.buttons.workflow.back,
            handler: function() {
                this.findParentByType('activitytabpanel').setActiveTab('activity_tab');
                this.backToActivityButton.disable();
                this.backToActivityButton.hide();
            },
            scope: this,
            disabled: true
		});
		
		this.actualForm = new Ext.form.FormPanel({
			layout: 'fit',
			border: false,
			frame: false,			
			hideMode: 'offsets',
			bodyCssClass: "cmdbuild_border_bottom_blue",
			items: [{ // MUST BE BEFORE THE HIDDEN FIELDS (EXT JS BUG)
				name : 'Notes',
				xtype : 'htmleditor',
				border: false,
				frame: false,
				hideLabel: true,
				enableLinks: false,
				enableSourceEdit: false,
				enableFont: false
			},{
				name: 'IdClass',
				xtype: 'hidden'
			},{
				name: 'Id',
				xtype: 'hidden'
			}]
		});

		this.displayPanel = new Ext.form.FormPanel({
			layout: 'fit',
			hideMode: 'offsets',
			autoScroll: true,
			border: false,			
			frame: false,
			bodyCssClass: "cmdbuild_background_blue " +
					"cmdbuild_body_padding " +
					"cmdbuild_border_bottom_blue " +
					"x-form-item",
			items: [{
				name : 'Notes',
				xtype : 'displayfield'
			}]
		});
		
		if (this.withButtons) {
			this.buttons = [this.saveButton,this.cancelButton];
			if (this.eventtype == 'activity') {
				this.buttons.push(this.backToActivityButton);
			}
		}
		
		Ext.apply(this, {
			frame: false,
			border: false,			
			layout: 'card',
			tbar: [
				this.modifyNoteAction,
				new CMDBuild.Management.GraphActionHandler().getAction()
			],
			items: [this.actualForm, this.displayPanel],
			buttonAlign: 'center',
			style: {background: CMDBuild.Constants.colors.blue.background}
        });
		CMDBuild.Management.CardNotesTab.superclass.initComponent.apply(this, arguments);
		this.subscribe('cmdb-init-' + this.eventmastertype, this.initForClass, this);
		this.subscribe('cmdb-new-' + this.eventtype, this.newCard, this);
		this.subscribe('cmdb-load-' + this.eventtype, this.loadCard, this);
		this.fixIE8RenderBug();		
	},
	
	destroy: function() {
		this.unsubscribe('cmdb-init-' + this.eventmastertype, this.initForClass, this);
		this.unsubscribe('cmdb-new-' + this.eventtype, this.newCard, this);
		this.unsubscribe('cmdb-load-' + this.eventtype, this.loadCard, this);
		
		CMDBuild.Management.CardNotesTab.superclass.destroy.call(this);
	},
	
	//private
	fixIE8RenderBug: function() {
		if (Ext.isIE) {
			this.on('activate', function() {
				this.setDisabled(this.disabled);
			}, this.actualForm);
		}
	},
	
	initForClass: function(eventParams) {
		this.disable();
	},

	loadCard: function(eventParams) {
		var idClass = eventParams.record.data.IdClass;
		if (CMDBuild.Utils.isSimpleTable(idClass)) {
			this.disable();
			return;
		}
		
		this.disableModify();
		this.currentCardId = eventParams.record.data.Id;
		this.currentCardPrivileges = {
			create: eventParams.record.data.priv_create,
			write: eventParams.record.data.priv_write
		};
		var form = this.actualForm.getForm();
		var displayform = this.displayPanel.getForm();
		form.reset();
		displayform.reset();
		form.loadRecord(eventParams.record);
		displayform.loadRecord(eventParams.record);
		this.modifyNoteAction.setDisabled(!this.currentCardPrivileges.write);
		this.enable();
	},

	reloadCard: function(eventParams) {
		this.enable();
	},

	newCard: function(eventParams) {
		this.disable();
	},

	disableModify: function() {
		this.modifyNoteAction.enable();
		this.saveButton.disable();
		this.cancelButton.disable();		
		this.getLayout().setActiveItem(this.displayPanel.id);		
	},

	enableModify: function() {
		this.modifyNoteAction.disable();
		this.saveButton.enable();
		this.cancelButton.enable();
		this.getLayout().setActiveItem(this.actualForm.id);
	},

	saveNote: function() {
		var form = this.actualForm.getForm();
		if (form.isValid()) {
			form.submit({
				method : 'POST',
				url : 'services/json/management/modcard/updatecard',
				waitTitle : CMDBuild.Translation.common.wait_title,
				waitMsg : CMDBuild.Translation.common.wait_msg,
				scope: this,
				success : function() {
					this.disableModify();
					this.publish('cmdb-reload-' + this.eventtype, {cardId: this.currentCardId});
					this.fireEvent("saved"); // To allow the migration to controllers architecture
				}
			});
		}
    }
});
Ext.reg('cardnotestab', CMDBuild.Management.CardNotesTab);