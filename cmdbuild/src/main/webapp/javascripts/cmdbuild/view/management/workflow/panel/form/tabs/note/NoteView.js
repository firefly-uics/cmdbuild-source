(function () {

	/**
	 * @link CMDBuild.view.management.common.widgets.CMOpenNotes
	 * @link CMDBuild.view.management.classes.CMCardNotesPanel
	 *
	 * @legacy
	 */
	Ext.define('CMDBuild.view.management.workflow.panel.form.tabs.note.NoteView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.workflow.panel.form.tabs.Note}
		 */
		delegate: undefined,

		border: false,
		frame: false,
		title: CMDBuild.Translation.management.modworkflow.tabs.notes,

		withButtons: true, // used in the windows to have specific buttons
		withTbar: true, // used in the windows to have specific buttons

		initComponent: function() {
			this._editMode = false;

			this.CMEVENTS = {
				saveNoteButtonClick: 'cm-save-clicked',
				cancelNoteButtonClick: 'cm-cancel-clicked',
				modifyNoteButtonClick: 'cm-modify.clicked'
			};

			var me = this;

			this.modifyNoteButton = Ext.create('CMDBuild.core.buttons.iconized.Modify', {
				text: CMDBuild.Translation.management.modcard.modify_note,
				scope: this,

				handler: function() {
					this.delegate.onModifyNoteClick();
				}
			});

			var htmlField = Ext.create('CMDBuild.view.common.field.HtmlEditor', {
				name: 'Notes',
				hideLabel: true
			});

			this.actualForm = new Ext.form.Panel({
				hideMode: 'offsets',
				layout: 'fit',
				border: false,
				frame: false,
				bodyCls: 'x-panel-body-default-framed',
				hideMode: 'offsets',
				items: [htmlField],

				setValue: function(v) {
					htmlField.setValue(v || '');
				},

				getValue: function() {
					return htmlField.getValue();
				}
			});

			var displayField = Ext.create('Ext.form.field.Display', {
				padding: '0 0 5px 5px',
				name : 'Notes',
				anchor: '95%'
			});

			this.displayPanel = Ext.create('Ext.form.Panel', {
				hideMode: 'offsets',
				autoScroll: true,
				frame: false,
				bodyCls: 'x-panel-body-default-framed',
				items: [displayField],

				setValue: function(v) {
					displayField.setValue(v);
				},

				getValue: function() {
					return displayField.getValue();
				}
			});

			this.buildButtons();

			if (this.withTbar)
				this.tbar = [this.modifyNoteButton];

			Ext.apply(this, {
				hideMode: 'offsets',
				frame: false,
				border: false,
				cls: 'x-panel-body-default-framed',
				layout: 'card',
				items: [this.displayPanel, this.actualForm],
				buttonAlign: 'center'
			});

			this.callParent(arguments);

			this.backToActivityButton.hide();
		},

		listeners: {
			show: function(panel, eOpts) {
				// History record save
				if (!Ext.isEmpty(_CMWFState.getProcessClassRef()) && !Ext.isEmpty( _CMWFState.getProcessInstance()))
					CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
						moduleId: 'workflow',
						entryType: {
							description: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.TEXT),
							id: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.ID),
							object: _CMWFState.getProcessClassRef()
						},
						item: {
							description: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.TEXT),
							id: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.ID),
							object: _CMWFState.getProcessInstance()
						},
						section: {
							description: this.title,
							object: this
						}
					});
			}
		},

		buildButtons: function() {
			if (this.withButtons) {
				var me = this;

				this.buttons = [
					this.saveButton = Ext.create('CMDBuild.core.buttons.text.Save', {
						name: 'saveButton',
						formBind: true,
						scope: this,

						handler: function (button, e) {
							this.delegate.onSaveNoteClick();
						}
					}),

					this.cancelButton = Ext.create('CMDBuild.core.buttons.text.Abort', {
						name: 'cancelButton',
						scope: this,

						handler: function (button, e) {
							this.delegate.onCancelNoteClick();
						}
					})
				];
			}

			this.buttons = this.buttons || [];

			this.backToActivityButton = Ext.create('CMDBuild.core.buttons.text.Back', { hidden: true });
			this.buttons.push(this.backToActivityButton);
		},

		configure: Ext.emptyFn,

		cmActivate: function() {
			this.enable();
			this.backToActivityButton.show();
			this.ownerCt.setActiveTab(this);
			this.enableModify();
		},

		hideBackButton: function() {
			this.backToActivityButton.hide();
		},

		reset: function() {
			this.actualForm.getForm().reset();
			this.displayPanel.getForm().reset();
		},

		loadCard: function(card) {
			this.actualForm.getForm().loadRecord(card);
			this.displayPanel.getForm().loadRecord(card);
		},

		getForm: function() {
			return this.actualForm.getForm();
		},

		syncForms: function() {
			var v = this.actualForm.getValue();

			this.displayPanel.setValue(v);

			return v;
		},

		disableModify: function() {
			if (this.privWrite) {
				this.modifyNoteButton.enable();
			} else {
				this.modifyNoteButton.disable();
			}

			if (this.withButtons) {
				this.saveButton.disable();
				this.cancelButton.disable();
			}

			this.getLayout().setActiveItem(this.displayPanel);
			this._editMode = false;
		},

		enableModify: function() {
			this.modifyNoteButton.disable();

			if (this.withButtons) {
				this.saveButton.enable();
				this.cancelButton.enable();
			}

			this.getLayout().setActiveItem(this.actualForm);
			this.actualForm.setValue(this.displayPanel.getValue());
			this._editMode = true;
		},

		updateWritePrivileges: function(privWrite) {
			this.privWrite = privWrite;
		},

		isInEditing: function() {
			return this._editMode;
		},

		/**
		 * @deprecated
		 */
		reloadCard: function(eventParams) {
			_deprecated('reloadCard', this);

			this.enable();
		},

		/**
		 * @deprecated
		 */
		onClassSelected: function() {
			_deprecated('onClassSelected', this);
		},

		/**
		 * @deprecated
		 */
		onCardSelected: function(card) {
			_deprecated('onCardSelected', this);

			var idClass = card.raw.IdClass;

			if (CMDBuild.Utils.isSimpleTable(idClass)) {
				this.disable();

				return;
			} else {
				this.enable();
			}

			this.currentCardId = card.get('Id');
			this.currentCardPrivileges = {
				create: card.raw.priv_create,
				write: card.raw.priv_write
			};
			this.reset();
			this.loadCard(card);
			this.disableModify();
		}
	});

})();