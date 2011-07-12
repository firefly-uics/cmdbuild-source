(function() {

Ext.define("CMDBuild.view.management.classes.CMCardNotesPanel", {
	extend: "Ext.panel.Panel",
	translation : CMDBuild.Translation.management.modcard,
	eventtype: 'card',
	eventmastertype: 'class',
	withButtons: true, // used in the windows to have specific buttons

	initComponent : function() {
		this.modifyNoteButton = new Ext.button.Button( {
			iconCls : 'modify',
			text : this.translation.modify_note,
			handler : this.enableModify,
			scope : this
		});

		this.saveButton = new Ext.button.Button({
			text : CMDBuild.Translation.common.buttons.save,
			name: 'saveButton',
			formBind : true
		});

		this.cancelButton = new Ext.button.Button({
			text : CMDBuild.Translation.common.buttons.abort,
			name: 'cancelButton',
			handler : this.disableModify,
			scope : this
		});

		this.actualForm = new Ext.form.Panel({
			hideMode: "offsets",
			layout: 'fit',
			border: false,
			frame: false,
			bodyCls: "x-panel-body-default-framed",
			hideMode: 'offsets',
			items: [{
				name : 'Notes',
				xtype : 'htmleditor',
				border: false,
				frame: false,
				hideLabel: true,
				enableLinks: false,
				enableSourceEdit: false,
				enableFont: false
			}]
		});

		this.displayPanel = new Ext.form.Panel({
			hideMode: "offsets",
			autoScroll: true,
			hideMode: "offsets",
			frame: false,
			bodyCls: "x-panel-body-default-framed",
			items: [{
				padding: "0 0 5px 5px",
				name : 'Notes',
				xtype : 'displayfield',
				anchor: '95%'
			}]
		});

		if (this.withButtons) {
			this.buttons = [this.saveButton,this.cancelButton];
			//TODO subclassing. Is used if the event type is activity
			if (this.eventtype == 'activity') {
				this.backToActivityButton = new Ext.Button({
					text : CMDBuild.Translation.common.buttons.workflow.back,
					handler : function() {
						this.findParentByType('activitytabpanel').setActiveTab('activity_tab');
						this.backToActivityButton.disable();
						this.backToActivityButton.hide();
					},
					scope : this,
					disabled : true
				});

				this.buttons.push(this.backToActivityButton);
			}
		}

		Ext.apply(this, {
			hideMode: "offsets",
			frame: false,
			border: false,
			cls: "x-panel-body-default-framed",
			layout: 'card',
			tbar: [this.modifyNoteButton],
			items: [
				this.displayPanel
				,this.actualForm
			],
			buttonAlign: 'center'
		});
		this.callParent(arguments);
	},
	onClassSelected: function() {
		this.disableModify();
		this.disable();
	},

	onCardSelected: function(card) {
		var idClass = card.raw.IdClass;
		if (CMDBuild.Utils.isSimpleTable(idClass)) {
			this.disable();
			return;
		} else {
			this.enable();
		}
		this.disableModify();
		this.currentCardId = card.get("Id");
		this.currentCardPrivileges = {
			create: card.raw.priv_create,
			write: card.raw.priv_write
		};
		var form = this.actualForm.getForm();
		var displayform = this.displayPanel.getForm();
		form.reset();
		displayform.reset();
		form.loadRecord(card);
		displayform.loadRecord(card);
		this.modifyNoteButton.setDisabled(!this.currentCardPrivileges.write);
		this.enable();
	},

	reloadCard: function(eventParams) {
		this.enable();
	},

	onAddCardButtonClick: function() {
		this.disable();
	},

	disableModify: function() {
		this.modifyNoteButton.enable();
		this.saveButton.disable();
		this.cancelButton.disable();
		if (this.rendered) {
			this.getLayout().setActiveItem(this.displayPanel.id);
		} else {
			this.on("render", function() {
				this.getLayout().setActiveItem(this.displayPanel.id);
			}, this, {single: true});
		}
	},

	enableModify: function() {
		this.modifyNoteButton.disable();
		this.saveButton.enable();
		this.cancelButton.enable();
		this.getLayout().setActiveItem(this.actualForm.id);
	}
});

})();