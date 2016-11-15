(function () {

	/**
	 * @link CMDBuild.controller.management.workflow.CMActivityAttachmentsController
	 * @link CMDBuild.controller.management.classes.attachments.CMCardAttachmentsController
	 * @link CMDBuild.controller.management.classes.CMModCardSubController
	 *
	 * @legacy
	 */
	Ext.define('CMDBuild.controller.management.workflow.panel.form.tabs.attachments.Attachments', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.LoadMask',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.management.workflow.panel.form.tabs.Attachment'
		],

		mixins: {
			observable: 'Ext.util.Observable',
			attachmentWindowDelegate: "CMDBuild.view.management.CMEditAttachmentWindowDelegate",
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		/**
		 * @cfg {CMDBuild.controller.management.workflow.panel.form.Form}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.management.workflow.panel.form.tabs.attachments.AttachmentsView}
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

			this.mixins.observable.constructor.call(this, arguments);

			this.view = Ext.create('CMDBuild.view.management.workflow.panel.form.tabs.attachments.AttachmentsView', { delegate: this });

			this.card = null;
			this.entryType = null;

			this.callBacks = {
				'action-attachment-delete': this.onDeleteAttachmentClick,
				'action-attachment-edit': this.onEditAttachmentClick,
				'action-attachment-download': this.onDownloadAttachmentClick
			};

			this.confirmStrategy = null;
			this.delegate = null;

			this.mon(this.view.addAttachmentButton, "click", this.onAddAttachmentButtonClick, this);
			this.mon(this.view, 'beforeitemclick', cellclickHandler, this);
			this.mon(this.view, "itemdblclick", onItemDoubleclick, this);
			this.mon(this.view, 'activate', this.view.loadCardAttachments, this.view);

			_CMWFState.addDelegate(this);

			// Build sub-controllers
			this.controllerWindowGraph = Ext.create('CMDBuild.controller.common.panel.gridAndForm.panel.common.graph.Window', { parentDelegate: this });
		},

		getCard: function() {
			return _CMWFState.getProcessInstance() || null;
		},

		getCardId: function() {
			var pi = this.getCard();
			if (pi) {
				return pi.getId();
			}
		},

		getClassId: function() {
			var pi = _CMWFState.getProcessInstance();
			if (pi) {
				return pi.getClassId();
			}
		},

		// wfStateDelegate
		onProcessInstanceChange: function(processInstance) {
			this._loaded = false;
			if (processInstance.isNew() ||
					this.theModuleIsDisabled()) {

				this.view.disable();
			} else {
				this.updateView();
			}
		},

		updateView: function(et) {
			var pi = _CMWFState.getProcessInstance();
			var processClass = _CMCache.getEntryTypeById(pi.getClassId());

			this.updateViewPrivilegesForEntryType(et);
			this.setViewExtraParams();
			this.view.reloadCard();
			this.view.enable();
			this.view.hideBackButton();
		},

		setViewExtraParams: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.getClassId());
			params[CMDBuild.core.constants.Proxy.CARD_ID] = this.getCardId();

			this.view.setExtraParams(params);
		},

		disableTheTabBeforeCardSelection: function(entryType) {
			return (entryType && entryType.get("tableType") == CMDBuild.core.constants.Global.getTableTypeSimpleTable());
		},

		onAddCardButtonClick: function(classIdOfNewCard) {
			this.view.disable();
		},

		// override
		// we want the attachments in readOnly mode, so set the privilege
		// to can only read. Then if there is the OpenAttachement extend attribute
		// it'll enable the editing
		// new business rule: read a configuration parameter to enable the editing
		// of attachments of closed activities (and then without damn openAttachment widget)
		updateViewPrivilegesForEntryType: function(et) {
			var priv = false;
			var pi = _CMWFState.getProcessInstance();

			if (CMDBuild.configuration.workflow.get(CMDBuild.core.constants.Proxy.ENABLE_ADD_ATTACHMENT_ON_CLOSED_ACTIVITIES)
					&& pi
					&& pi.isStateCompleted()) {

				priv = true;
			}

			this.view.updateWritePrivileges(priv);
		},

		updateViewPrivilegesForTypeId: function(entryTypeId) {
			var et = _CMCache.getEntryTypeById(entryTypeId);
			this.updateViewPrivilegesForEntryType(et);
		},

		onDeleteAttachmentClick: function(record) {
			var me = this;

			Ext.Msg.confirm(tr.delete_attachment, tr.delete_attachment_confirm,
				function(btn) {
					if (btn != 'yes') {
						return;
					}
					doDeleteRequst(me, record);
		 		}, this);
		},

		onDownloadAttachmentClick: function (record) {
			var params = {};
			params['Filename'] = record.get('Filename');
			params[CMDBuild.core.constants.Proxy.CARD_ID] = this.getCardId();
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.getClassId());

			CMDBuild.proxy.management.workflow.panel.form.tabs.Attachment.download({ params: params });
		},

		// It is not possible add an attachment at the first step of the process
		onAddAttachmentButtonClick: function() {
			var pi = _CMWFState.getProcessInstance();
			if (pi && pi.isNew()) {
				CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.canNotAddAnAttachmentBeforeSavingTheActivity, false);
			} else {
				var autocompletionRules = findAutocompletionRules(this);
				var serverVars = CMDBuild.controller.management.common.widgets.CMWidgetController.getTemplateResolverServerVars(this.getCard());
				var templateResolverForm = this.superController ? this.superController.getFormForTemplateResolver() : null;

				// without the form, the template resolver is not able to
				// do its work. This happen if open the attachments
				// window from the Detail Tab
				var me = this;
				if (templateResolverForm) {
					var mergedRoules = mergeRulesInASingleMap(autocompletionRules);

					new CMDBuild.Management.TemplateResolver({
						clientForm: templateResolverForm,
						xaVars: mergedRoules,
						serverVars: serverVars
					}).resolveTemplates({
						attributes: Ext.Object.getKeys(mergedRoules),
						callback: function(o) {
							createWindowToAddAttachment(me, groupMergedRules(o));
						}
					});
				} else {
					createWindowToAddAttachment(me, autocompletionRules);
				}
			}
		},

		onEditAttachmentClick: function(record) {
			new CMDBuild.view.management.CMEditAttachmentWindow({
				metadataValues: record.getMetadata(),
				attachmentRecord: record,
				delegate: this
			}).show();

			this.confirmStrategy = Ext.create('CMDBuild.controller.management.workflow.panel.form.tabs.attachments.ModifyAttachmentStrategy', this);
		},

		destroy: function() {
			this.mun(this.view.addAttachmentButton, "click", this.onAddAttachmentButtonClick, this);
			this.mun(this.view, 'beforeitemclick', cellclickHandler, this);
			this.mun(this.view, "itemdblclick", onItemDoubleclick, this);
			this.mun(this.view, 'activate', this.view.loadCardAttachments, this.view);
		},

		theModuleIsDisabled: function() {
			return !CMDBuild.configuration.dms.get(CMDBuild.core.constants.Proxy.ENABLED);
		},

		// as attachment window delegate
		onConfirmButtonClick: function(attachmentWindow) {
			var form = attachmentWindow.form.getForm();

			if (!form.isValid()) {
				return;
			}

			if (this.confirmStrategy) {
				CMDBuild.core.LoadMask.show();
				attachmentWindow.mask();
				this.confirmStrategy.doRequest(attachmentWindow);
			}
		},

		onShowGraphClick: function() {
			this.controllerWindowGraph.cmfg('onPanelGridAndFormGraphWindowConfigureAndShow', {
				classId: this.card.get("IdClass"),
				cardId: this.card.get("Id")
			});
		},

		onCloneCard: function() {
			if (this.view) {
				this.view.disable();
			}
		},

		/**
		 * @returns {Void}
		 *
		 * @legacy
		 */
		reset: function () {
			this.view.clearStore();

			this.view.disable();
		}
	});

	function createWindowToAddAttachment(me, metadataValues) {
		new CMDBuild.view.management.CMEditAttachmentWindow({
			metadataValues: metadataValues,
			delegate: me
		}).show();

		me.confirmStrategy = new CMDBuild.controller.management.classes
		.attachments.AddAttachmentStrategy(me);
	}

	function findAutocompletionRules(me) {
		var classId = me.getClassId();
		var rules = {};

		if (classId) {
			var entryType = _CMCache.getEntryTypeById(classId);
			if (entryType) {
				rules = entryType.getAttachmentAutocompletion();
			}
		}

		return rules;
	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className;

		if (this.callBacks[className]) {
			this.callBacks[className].call(this, model);
		}
	};

	function onItemDoubleclick(grid, model, html, index, e, options) {
		this.onDownloadAttachmentClick(model);
	};

	function doDeleteRequst(me, record) {
		var params = {
			Filename: record.get("Filename")
		};

		params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.getClassId());
		params[CMDBuild.core.constants.Proxy.CARD_ID] = me.getCardId();

		CMDBuild.core.LoadMask.show();
		CMDBuild.proxy.management.workflow.panel.form.tabs.Attachment.remove({
			params: params,
			loadMask: false,
			scope: this,
			success: function() {
				// Defer the call because Alfresco is not responsive
				Ext.Function.createDelayed(function deferredCall() {
					CMDBuild.core.LoadMask.hide();
					me.view.reloadCard();
				}, CMDBuild.configuration.dms.get(CMDBuild.core.constants.Proxy.ALFRESCO_DELAY), me)();
			},
			failure: function() {
				CMDBuild.core.LoadMask.hide();
			}
		});
	}

})();
