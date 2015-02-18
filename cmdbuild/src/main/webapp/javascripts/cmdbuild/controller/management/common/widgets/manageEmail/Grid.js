(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.Grid', {

		requires: [
			'CMDBuild.controller.management.common.widgets.manageEmail.Main',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.ManageEmail'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow}
		 */
		controllerEmailWindow: undefined,

		/**
		 * @cfg {Array}
		 */
		deletedEmails: [],

		/**
		 * All email types this widget manages
		 *
		 * @cfg {Object}
		 */
		emailTypes: {
			draft: 'draft',
			outgoing: 'outgoing',
			received: 'received',
			sent: 'sent'
		},

		/**
		 * @property {Mixed}
		 */
		emailWindow: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.widgets.manageEmail.Grid}
		 */
		view: undefined,

		/**
		 * @property {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.management.common.widgets.manageEmail.Main} configObject.parentDelegate
		 * @param {CMDBuild.controller.management.common.widgets.manageEmail.Grid} configObject.view
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view.delegate = this;
			this.widgetConf = this.parentDelegate.widgetConf;
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onEmailAddButtonClick':
					return this.onEmailAddButtonClick(param);

				case 'onEmailDeleteButtonClick':
					return this.onEmailDeleteButtonClick(param);

				case 'onEmailEditButtonClick':
					return this.onEmailEditButtonClick(param);

				case 'onEmailRegenerationButtonClick':
					return this.onEmailRegenerationButtonClick(param);

				case 'onEmailReplyButtonClick':
					return this.onEmailReplyButtonClick(param);

				case 'onEmailViewButtonClick':
					return this.onEmailViewButtonClick(param);

				case 'onItemDoubleClick':
					return this.onItemDoubleClick(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 * @param {Boolean} enableStoreLoad
		 */
		addRecord: function(record, disableStoreLoad) {
			disableStoreLoad = disableStoreLoad || false;
_debug('addRecord record', record);
			this.parentDelegate.view.setLoading(true);
			CMDBuild.core.proxy.widgets.ManageEmail.create({
				params: record.getAsParams(),
				scope: this,
				failure: function(response, options, decodedResponse) {
					CMDBuild.Msg.error(CMDBuild.Translation.common.failure, '@@ ManageEmail grid controller error: email create call failure', false);
				},
				success: function(response, options, decodedResponse) {
					if (!disableStoreLoad)
						this.storeLoad();
				},
				callback: function(options, success, response) {
					this.parentDelegate.view.setLoading(false);
				}
			});
		},

		/**
		 * @param {Object} values
		 */
		addTemplate: function(values) {
			var record = this.createRecord(values);

			this.addRecord(record);
		},

		/**
		 * Creates email model with default attributes setup
		 *
		 * @param {Object} recordValues
		 *
		 * @return {CMDBuild.model.widget.ManageEmail.email}
		 */
		createRecord: function(recordValues) {
			recordValues = recordValues || {};
			recordValues[CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID] = recordValues[CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID] || this.parentDelegate.getActivityId();
			recordValues[CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION] = false;
			recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] = (recordValues.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX)) ? recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] : this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX];
			recordValues[CMDBuild.core.proxy.CMProxyConstants.STATUS] = recordValues[CMDBuild.core.proxy.CMProxyConstants.STATUS] || this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.DRAFT];

			return Ext.create('CMDBuild.model.widget.ManageEmail.email', recordValues);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 * @param {Boolean} enableStoreLoad
		 */
		editRecord: function(record, disableStoreLoad) {
			disableStoreLoad = disableStoreLoad || false;
_debug('editRecord record', record);
			this.parentDelegate.view.setLoading(true);
			CMDBuild.core.proxy.widgets.ManageEmail.update({
				params: record.getAsParams(),
				scope: this,
				failure: function(response, options, decodedResponse) {
					CMDBuild.Msg.error(CMDBuild.Translation.common.failure, '@@ ManageEmail grid controller error: email update call failure', false);
				},
				success: function(response, options, decodedResponse) {
					if (!disableStoreLoad)
						this.storeLoad();
				},
				callback: function(options, success, response) {
					this.parentDelegate.view.setLoading(false);
				}
			});
		},

		/**
		 * @return {Array}
		 */
		getDeletedEmails: function () {
			return this.deletedEmails;
		},

		/**
		 * @return {Array}
		 */
		getDraftEmails: function() {
			return this.getEmailsByGroup(this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.DRAFT]);
		},

		/**
		 * @param {String} group
		 *
		 * @return {Array}
		 */
		getEmailsByGroup: function(group) {
			var out = this.view.getStore().getGroups(group);

			if (out)
				out = out.children; // ExtJS mystic output { name: group, children: [...] }

			return out || [];
		},

		/**
		 * @param {Boolean} modifiedOnly
		 *
		 * @return {Array} outgoingEmails
		 */
		getOutgoingEmails: function(modifiedOnly) {
			var allOutgoing = modifiedOnly ? false : true;
			var outgoingEmails = [];
			var emails = this.view.getStore().getRange();

			for (var i = 0; i < emails.length; ++i) {
				var currentEmail = emails[i];

				if (allOutgoing || currentEmail.dirty)
					outgoingEmails.push(currentEmail.data);
			}

			return outgoingEmails;
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 *
		 * @return {Boolean}
		 */
		isRegenerable: function(record) {
			return !Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)) && record.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION);
		},

		onEmailAddButtonClick: function() {
			this.controllerEmailWindow = Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
				parentDelegate: this,
				record: this.createRecord(),
				widgetConf: this.widgetConf,
				widgetController: this.parentDelegate
			});

			this.emailWindow = this.controllerEmailWindow.getView();
			this.emailWindow.show();
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onEmailDeleteButtonClick: function(record) {
			Ext.Msg.confirm(
				CMDBuild.Translation.common.confirmpopup.title,
				CMDBuild.Translation.common.confirmpopup.areyousure,

				function(btn) {
					if (btn == 'yes')
						this.removeRecord(record);
				},
				this
			);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onEmailEditButtonClick: function(record) {
			this.controllerEmailWindow = Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
				parentDelegate: this,
				record: record,
				widgetConf: this.widgetConf,
				widgetController: this.parentDelegate,
				windowMode: 'edit'
			});

			this.emailWindow = this.controllerEmailWindow.getView();
			this.emailWindow.show();
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onEmailRegenerationButtonClick: function(record) {
			if (!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE))) {
				var emptyEmail = Ext.create('CMDBuild.model.widget.ManageEmail.email');
				emptyEmail.set(CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID, this.parentDelegate.getActivityId());
				emptyEmail.set(CMDBuild.core.proxy.CMProxyConstants.ID, record.get(CMDBuild.core.proxy.CMProxyConstants.ID));
				emptyEmail.set(CMDBuild.core.proxy.CMProxyConstants.STATUS, this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.DRAFT]);
				emptyEmail.set(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE));

				this.parentDelegate.regenerateEmail(emptyEmail);
				this.storeLoad();
			}
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onEmailReplyButtonClick: function(record) {
			var content = '<p>'
					+ CMDBuild.Translation.onDay + ' ' + record.get(CMDBuild.core.proxy.CMProxyConstants.DATE)
					+ ', <' + record.get(CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS) + '> ' + CMDBuild.Translation.hasWrote
				+ ':</p>'
				+ '<blockquote>' + record.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT) + '</blockquote>';

			var replyRecordData = {};
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS] = null;
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES] = record.get(CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.CONTENT] = content;
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.DATE] = null;
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS] = null;
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH] = null;
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] = true;
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.STATUS] = this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.DRAFT];
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] = 'RE: ' + record.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES] = record.get(CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS) || record.get(CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES);

			this.controllerEmailWindow = Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
				parentDelegate: this,
				record: Ext.create('CMDBuild.model.widget.ManageEmail.email', replyRecordData),
				widgetConf: this.widgetConf,
				widgetController: this.parentDelegate,
				windowMode: 'reply'
			});

			this.emailWindow = this.controllerEmailWindow.getView();
			this.emailWindow.show();
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onEmailViewButtonClick: function(record) {
			this.controllerEmailWindow = Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
				parentDelegate: this,
				record: record,
				widgetConf: this.widgetConf,
				widgetController: this.parentDelegate,
				windowMode: 'view'
			});

			this.emailWindow = this.controllerEmailWindow.getView();
			this.emailWindow.show();
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onItemDoubleClick: function(record) {
			if (this.recordIsEditable(record)) {
				this.onEmailEditButtonClick(record);
			} else {
				this.onEmailViewButtonClick(record);
			}
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 *
		 * @return {Boolean}
		 */
		recordIsEditable: function(record) {
			return record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS) == this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.DRAFT];
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 *
		 * @return {Boolean}
		 */
		recordIsReceived: function(record) {
			return (record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS) == this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.RECEIVED]);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 * @param {Boolean} enableStoreLoad
		 */
		removeRecord: function(record, disableStoreLoad) {
			disableStoreLoad = disableStoreLoad || false;
_debug('removeRecord record', record);
			this.parentDelegate.view.setLoading(true);
			CMDBuild.core.proxy.widgets.ManageEmail.remove({
				params: record.getAsParams([CMDBuild.core.proxy.CMProxyConstants.ID, CMDBuild.core.proxy.CMProxyConstants.TEMPORARY]),
				scope: this,
				failure: function(response, options, decodedResponse) {
					CMDBuild.Msg.error(CMDBuild.Translation.common.failure, '@@ ManageEmail grid controller error: email remove call failure', false);
				},
				success: function(response, options, decodedResponse) {
					if (!disableStoreLoad)
						this.storeLoad();
				},
				callback: function(options, success, response) {
					this.parentDelegate.view.setLoading(false);
				}
			});
		},

		/**
		 * Loads grid store with activityId parameter
		 *
		 * @param {Boolean} regenerateAllEmails
		 * @param {Boolean} forceRegeneration
		 */
		storeLoad: function(regenerateAllEmails, forceRegeneration) {
			regenerateAllEmails = regenerateAllEmails || false;
			forceRegeneration = forceRegeneration || false;

			this.parentDelegate.view.setLoading(true);
			this.view.getStore().load({
				params: {
					activityId: this.parentDelegate.getActivityId()
				},
				scope: this,
				callback: function(records, operation, success) {
					this.parentDelegate.view.setLoading(false);

					this.parentDelegate.getAllTemplatesData(regenerateAllEmails, forceRegeneration);
				}
			});
		}
	});

})();