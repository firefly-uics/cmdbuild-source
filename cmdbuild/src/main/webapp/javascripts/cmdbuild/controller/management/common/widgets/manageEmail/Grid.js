(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.Grid', {

		requires: [
			'CMDBuild.controller.management.common.widgets.manageEmail.Main',
			'CMDBuild.core.proxy.CMProxyConstants'
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
			draft: 'Draft',
			'new': 'New',
			outgoing: 'Outgoing',
			received: 'Received',
			sent: 'Sent'
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

				case 'onEmailDelete':
					return this.onEmailDelete(param);

				case 'onEmailEdit':
					return this.onEmailEdit(param);

				case 'onEmailRegeneration':
					return this.onEmailRegeneration(param);

				case 'onEmailReply':
					return this.onEmailReply(param);

				case 'onEmailView':
					return this.onEmailView(param);

				case 'onGlobalRegenerationButtonClick':
					return this.onGlobalRegenerationButtonClick();

				case 'onItemDoubleClick':
					return this.onItemDoubleClick(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param {Object} values
		 */
		addTemplateToStore: function(values) {
			var record = this.createRecord(values);

			this.addToStoreIfNotInIt(record);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		addToStoreIfNotInIt: function(record) {
			var store = this.view.getStore();

			if (store.findBy(function(item) {
					return item.get(CMDBuild.core.proxy.CMProxyConstants.ID) == record.get(CMDBuild.core.proxy.CMProxyConstants.ID);
				}) == -1
			) {
				CMDBuild.controller.management.common.widgets.manageEmail.Main.generateTemporaryId(record);

				// Use loadRecords because store.add does not update the grouping so the grid goes broken
				store.loadRecords([record], { addRecords: true });
			}
		},

		/**
		 * @param {Object} recordValues
		 *
		 * @return {CMDBuild.model.widget.ManageEmail.email}
		 */
		createRecord: function(recordValues) {
			recordValues = recordValues || {};
			recordValues[CMDBuild.core.proxy.CMProxyConstants.STATUS] = recordValues[CMDBuild.core.proxy.CMProxyConstants.STATUS] || this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.NEW];
			recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] = (recordValues.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX)) ? recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] : this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX];

			return Ext.create('CMDBuild.model.widget.ManageEmail.email', recordValues);
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
		 * @return {Array}
		 */
		getNewEmails: function() {
			return this.getEmailsByGroup(this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.NEW]);
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

				// Avoid to send temporary Ids to server
				if (currentEmail.get(CMDBuild.core.proxy.CMProxyConstants.IS_ID_TEMPORARY))
					delete currentEmail.data[CMDBuild.core.proxy.CMProxyConstants.ID];

				if (allOutgoing || currentEmail.dirty)
					outgoingEmails.push(currentEmail.data);
			}

			return outgoingEmails;
		},

		/**
		 * @return {Boolean}
		 */
		hasDraftEmails: function() {
			return this.getDraftEmails().length > 0;
		},

		/**
		 * @WIP TODO
		 *
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 *
		 * @return {Boolean}
		 */
		isRegenerable: function(record) {
			return true;
		},

		/**
		 * @return {Boolean}
		 */
		isStoreLoaded: function() {
			return this.view.storeLoaded;
		},

		onEmailAddButtonClick: function() {
			this.controllerEmailWindow = Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
				parentDelegate: this,
				record: this.createRecord()
			});

			this.emailWindow = this.controllerEmailWindow.getView();
			this.emailWindow.show();
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onEmailDelete: function(record) {
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
		onEmailEdit: function(record) {
			this.controllerEmailWindow = Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
				parentDelegate: this,
				record: record,
				windowMode: 'edit'
			});

			this.emailWindow = this.controllerEmailWindow.getView();
			this.emailWindow.show();
		},

		/**
		 * @WIP TODO
		 *
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onEmailRegeneration: function(record) {
			this.controllerEmailWindow = Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
				parentDelegate: this,
				windowMode: 'confirm'
			});

			this.emailWindow = this.controllerEmailWindow.getView();
			this.emailWindow.show();
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onEmailReply: function(record) {
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
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.STATUS] = this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.NEW];
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] = 'RE: ' + record.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES] = record.get(CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS) || record.get(CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES);

			this.controllerEmailWindow = Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
				parentDelegate: this,
				record: Ext.create('CMDBuild.model.widget.ManageEmail.email', replyRecordData),
				windowMode: 'reply'
			});

			this.emailWindow = this.controllerEmailWindow.getView();
			this.emailWindow.show();
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onEmailView: function(record) {
			this.controllerEmailWindow = Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
				parentDelegate: this,
				record: record,
				windowMode: 'view'
			});

			this.emailWindow = this.controllerEmailWindow.getView();
			this.emailWindow.show();
		},

		onGlobalRegenerationButtonClick: function() {
			this.parentDelegate.checkToRegenerateAllEmails(true);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onItemDoubleClick: function(record) {
			if (this.recordIsEditable(record)) {
				this.onEmailEdit(record);
			} else {
				this.onEmailView(record);
			}
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 *
		 * @return {Boolean}
		 */
		recordIsEditable: function(record) {
			var status = record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS);

			return status == this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.DRAFT] || status == this.emailTypes[CMDBuild.core.proxy.CMProxyConstants.NEW];
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
		 * Check if temporaryId is false, email is known from server so put id in deletedEmails array
		 *
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		removeRecord: function(record) {
			var id = record.getId();

			if (id > 0 && !record.get(CMDBuild.core.proxy.CMProxyConstants.IS_ID_TEMPORARY))
				this.deletedEmails.push(id);

			this.view.getStore().remove(record);
		}
	});

})();