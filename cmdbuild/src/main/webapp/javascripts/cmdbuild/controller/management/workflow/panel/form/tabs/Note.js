(function () {

	/**
	 * @link CMDBuild.controller.management.workflow.CMNoteController
	 * @link CMDBuild.controller.management.classes.CMNoteController
	 *
	 * @legacy
	 */
	Ext.define('CMDBuild.controller.management.workflow.panel.form.tabs.Note', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.Card',
			'CMDBuild.proxy.workflow.management.panel.form.tabs.Note'
		],

		mixins: {
			observable: 'Ext.util.Observable',
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		/**
		 * @cfg {CMDBuild.controller.management.workflow.panel.form.Form}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.management.workflow.panel.form.tabs.note.NoteView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.workflow.panel.form.Form} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.workflow.panel.form.tabs.note.NoteView', { delegate: this });

			this.mixins.observable.constructor.call(this, arguments);

			this.card = null;
			this.entryType = null;

			this.CMEVENTS = {
				noteWasSaved: 'cm-note-saved'
			};

			this.addEvents(this.addEvents.noteWasSaved);

			_CMWFState.addDelegate(this);
		},

		/**
		 * @param {Number} id
		 *
		 * @returns {Void}
		 */
		onAddCardClick: function (id) {
			this.view.disable();
		},

		// override to deny to add a note to a new process
		disableTheTabBeforeCardSelection: function(processInstance) {
			if (!processInstance || processInstance.isNew()) {
				return true;
			} else {
				return CMDBuild.Utils.isSimpleTable(processInstance.getClassId());
			}
		},

		reset: function(card) {
			this.view.reset();
			this.view.disable();
		},

		updateView: function(card) {
			this.updateViewPrivilegesForCard(card);
			this.view.reset();
			this.view.disableModify();
		},

		// override: return always false because we want that
		// in process the user could modify the notes only if
		// there is an openNote extended attribute defined.
		updateViewPrivilegesForCard: function(card) {
			this.view.updateWritePrivileges(false);
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		onSaveNoteClick: function () {
			var form = this.view.getForm();
			var params = this._getSaveParams();

			if (form.isValid() && this.beforeSave(this.card)) {
				CMDBuild.proxy.workflow.management.panel.form.tabs.Note.update({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						this.view.disableModify(enableToolbar = true);

						this.syncSavedNoteWithModel(
							this.card,
							this.view.syncForms(),
							decodedResponse
						);

						this.fireEvent(this.CMEVENTS.noteWasSaved);
					}
				});
			}
		},

		// override
		// use the process instance
		onCancelNoteClick: function() {
			this.onProcessInstanceChange(_CMWFState.getProcessInstance());
		},

		// wfStateDelegate
		onProcessClassRefChange: function() {
			this.view.disable();
		},

		onProcessInstanceChange: function(pi) {
			this.updateView(pi);
			this.view.loadCard(new CMDBuild.DummyModel(pi.getValues()));

			if (this.disableTheTabBeforeCardSelection(pi)) {
				this.view.disable();
			} else {
				this.view.enable();
			}
		},

		onActivityInstanceChange: Ext.emptyFn,

		onModifyNoteClick: function() {
			if (isEditable(this.card)) {
				var me = this;

				this.lockCard(function() {
					me.view.enableModify();
				});
			}
		},

		// is not possible to save the note if the
		// activity is not already saved
		beforeSave: function(card) {
			var isNew = isANewActivity(card);

			if (isNew) {
				CMDBuild.core.Message.error(CMDBuild.Translation.common.failure,
					CMDBuild.Translation.management.modworkflow.extattrs.notes.must_save_to_modify,
					popup = false);
			}

			return !isNew;
		},

		// override to retrieve the activityInstance
		// from the WorkflowState
		_getSaveParams: function() {
			var params = {};
			var form = this.view.getForm();
 			var pi = _CMWFState.getProcessInstance();
			var ai = _CMWFState.getActivityInstance();

			if (pi && ai) {
				params['Notes'] = this.view.getForm().getValues()['Notes'];
				params.classId = pi.getClassId();
				params.cardId = pi.getId();
				params.activityInstanceId = ai.getId();
				params.advance = false;
				params.ww = '{}';
				params.attributes = Ext.encode(form.getValues());
 			}

			return params;
		},

		// override
		// don't use the card passed by superclass success
		// after save request. Use the processInstance instead
		syncSavedNoteWithModel: function(card, noteValue, processData) {
			var pi = _CMWFState.getProcessInstance();

			if (pi) {
				pi.setNotes(noteValue);
				pi.updateBeginDate(processData);
			}
		},

		lockCard: function(success) {
			if (CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ENABLE_CARD_LOCK)) {
				if (this.card) {
					var id = this.card.get('Id');
					CMDBuild.proxy.workflow.management.panel.form.tabs.Note.lockActivity({
						params: {
							id: id
						},
						loadMask: false,
						success: success
					});
				}
			} else {
				success();
			}
		},

		unlockCard: function() {
			if (CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ENABLE_CARD_LOCK)) {
				if (this.card && this.view.isInEditing()) {
					var id = this.card.get('Id');
					CMDBuild.proxy.workflow.management.panel.form.tabs.Note.unlockActivity({
						params: {
							id: id
						},
						loadMask: false
					});
				}
			}
		}
	});


	function isEditable(card) {
		return _CMUtils.getEntryTypePrivilegesByCard(card).write;
	}

})();
