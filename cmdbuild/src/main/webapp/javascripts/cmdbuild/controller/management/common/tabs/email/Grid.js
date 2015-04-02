(function () {

	Ext.define('CMDBuild.controller.management.common.tabs.email.Grid', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.controller.management.common.tabs.email.Email',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.tabs.email.Email'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Email}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGridAddEmailButtonClick',
			'onGridDeleteEmailButtonClick',
			'onGridEditEmailButtonClick',
			'onGridItemDoubleClick',
			'onGridRegenerationEmailButtonClick',
			'onGridReplyEmailButtonClick',
			'onGridSendEmailButtonClick',
			'onGridViewEmailButtonClick'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.EmailWindow}
		 */
		controllerEmailWindow: undefined,

		/**
		 * @property {Mixed}
		 */
		emailWindow: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.tabs.email.Grid}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.management.common.tabs.email.Email} configObject.parentDelegate
		 * @param {CMDBuild.controller.management.common.tabs.email.Grid} configObject.view
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.management.common.tabs.email.GridPanel', {
				delegate: this
			});
		},

		/**
		 * @param {CMDBuild.model.tabs.Email.email} record
		 * @param {Array} regenerationTrafficLightArray
		 * @param {Function} success
		 */
		addRecord: function(record, regenerationTrafficLightArray, success) {
			if (!Ext.Object.isEmpty(record)) {
				CMDBuild.core.proxy.tabs.email.Email.create({
					params: record.getAsParams(),
					scope: this,
					loadMask: this.cmfg('getGlobalLoadMask'),
					failure: function(response, options, decodedResponse) {
						CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.emailCreate, false);
					},
					success: success || function(response, options, decodedResponse) {
						if (CMDBuild.controller.management.common.tabs.email.Email.trafficLightArrayCheck(record, regenerationTrafficLightArray) || Ext.isEmpty(regenerationTrafficLightArray))
							this.storeLoad();
					}
				});
			}
		},

		/**
		 * Creates email model with default attributes setup
		 *
		 * @param {Object} recordValues
		 *
		 * @return {CMDBuild.model.tabs.Email.email}
		 */
		createRecord: function(recordValues) {
			recordValues = recordValues || {};
			recordValues[CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID] = recordValues[CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID] || this.parentDelegate.getSelectedEntityId();
			recordValues[CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION] = false;
			recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] = recordValues.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX) ? recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] : this.cmfg('getConfiguration')[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX];

			return Ext.create('CMDBuild.model.tabs.Email.email', recordValues);
		},

		/**
		 * @param {CMDBuild.model.tabs.Email.email} record
		 * @param {Array} regenerationTrafficLightArray
		 */
		editRecord: function(record, regenerationTrafficLightArray) {
			if (!Ext.Object.isEmpty(record)) {
				CMDBuild.core.proxy.tabs.email.Email.update({
					params: record.getAsParams(),
					scope: this,
					loadMask: this.cmfg('getGlobalLoadMask'),
					failure: function(response, options, decodedResponse) {
						CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.emailUpdate, false);
					},
					success: function(response, options, decodedResponse) {
						if (CMDBuild.controller.management.common.tabs.email.Email.trafficLightArrayCheck(record, regenerationTrafficLightArray) || Ext.isEmpty(regenerationTrafficLightArray))
							this.storeLoad();
					}
				});
			}
		},

		/**
		 * @return {Array}
		 */
		getDraftEmails: function() {
			return this.getEmailsByGroup(CMDBuild.core.proxy.CMProxyConstants.DRAFT);
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
		 * @param {CMDBuild.model.tabs.Email.email} record
		 *
		 * @return {Boolean}
		 */
		isRegenerable: function(record) {
			return !Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE));
		},

		onGridAddEmailButtonClick: function() {
			var me = this;
			var record = this.createRecord();

			this.addRecord( // To generate an emailId
				record,
				null,
				function(response, options, decodedResponse) { // Success function override
					record.set(CMDBuild.core.proxy.CMProxyConstants.ID, decodedResponse.response);

					Ext.create('CMDBuild.controller.management.common.tabs.email.EmailWindow', {
						parentDelegate: me,
						record: record
					});

					this.storeLoad();
				}
			);
		},

		/**
		 * @param {CMDBuild.model.tabs.Email.email} record
		 */
		onGridDeleteEmailButtonClick: function(record) {
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
		 * @param {CMDBuild.model.tabs.Email.email} record
		 */
		onGridEditEmailButtonClick: function(record) {
			Ext.create('CMDBuild.controller.management.common.tabs.email.EmailWindow', {
				parentDelegate: this,
				record: record,
				windowMode: 'edit'
			});
		},

		/**
		 * @param {CMDBuild.model.tabs.Email.email} record
		 */
		onGridItemDoubleClick: function(record) {
			if (!this.cmfg('getConfiguration')[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY] && this.recordIsEditable(record)) {
				this.onGridEditEmailButtonClick(record);
			} else {
				this.onGridViewEmailButtonClick(record);
			}
		},

		/**
		 * @param {CMDBuild.model.tabs.Email.email} record
		 */
		onGridRegenerationEmailButtonClick: function(record) {
			if (!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)))
				this.parentDelegate.regenerateSelectedEmails([record]);
		},

		/**
		 * @param {CMDBuild.model.tabs.Email.email} record
		 */
		onGridReplyEmailButtonClick: function(record) {
			var content = '<p>'
					+ CMDBuild.Translation.onDay + ' ' + record.get(CMDBuild.core.proxy.CMProxyConstants.DATE)
					+ ', <' + record.get(CMDBuild.core.proxy.CMProxyConstants.FROM) + '> ' + CMDBuild.Translation.hasWrote
				+ ':</p>'
				+ '<blockquote>' + record.get(CMDBuild.core.proxy.CMProxyConstants.BODY) + '</blockquote>';

			var replyRecordData = {};
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.ACCOUNT] = record.get(CMDBuild.core.proxy.CMProxyConstants.ACCOUNT);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID] = record.get(CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID) || this.parentDelegate.getSelectedEntityId();
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.BCC] = record.get(CMDBuild.core.proxy.CMProxyConstants.BCC);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.BODY] = content;
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.CC] = record.get(CMDBuild.core.proxy.CMProxyConstants.CC);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION] = false;
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH] = record.get(CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] = record.get(CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] = 'RE: ' + record.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.TO] = record.get(CMDBuild.core.proxy.CMProxyConstants.FROM) || record.get(CMDBuild.core.proxy.CMProxyConstants.TO);

			Ext.create('CMDBuild.controller.management.common.tabs.email.EmailWindow', {
				parentDelegate: this,
				record: Ext.create('CMDBuild.model.tabs.Email.email', replyRecordData),
				windowMode: 'reply'
			});
		},

		/**
		 * @param {CMDBuild.model.tabs.Email.email} record
		 */
		onGridSendEmailButtonClick: function(record) {
			this.sendRecord(record);
		},

		/**
		 * @param {CMDBuild.model.tabs.Email.email} record
		 */
		onGridViewEmailButtonClick: function(record) {
			Ext.create('CMDBuild.controller.management.common.tabs.email.EmailWindow', {
				parentDelegate: this,
				record: record,
				windowMode: 'view'
			});
		},

		/**
		 * @param {CMDBuild.model.tabs.Email.email} record
		 *
		 * @return {Boolean}
		 */
		recordIsEditable: function(record) {
			return record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS) == CMDBuild.core.proxy.CMProxyConstants.DRAFT;
		},

		/**
		 * @param {CMDBuild.model.tabs.Email.email} record
		 *
		 * @return {Boolean}
		 */
		recordIsSendable: function(record) {
			return (
				!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.TO))
				&& !Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT))
				&& record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS) != CMDBuild.core.proxy.CMProxyConstants.OUTGOING
			);
		},

		/**
		 * @param {CMDBuild.model.tabs.Email.email} record
		 * @param {Array} regenerationTrafficLightArray
		 */
		removeRecord: function(record, regenerationTrafficLightArray) {
			if (!Ext.Object.isEmpty(record)) {
				CMDBuild.core.proxy.tabs.email.Email.remove({
					params: record.getAsParams([CMDBuild.core.proxy.CMProxyConstants.ID, CMDBuild.core.proxy.CMProxyConstants.TEMPORARY]),
					scope: this,
					loadMask: this.cmfg('getGlobalLoadMask'),
					failure: function(response, options, decodedResponse) {
						CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.emailRemove, false);
					},
					success: function(response, options, decodedResponse) {
						if (CMDBuild.controller.management.common.tabs.email.Email.trafficLightArrayCheck(record, regenerationTrafficLightArray) || Ext.isEmpty(regenerationTrafficLightArray))
							this.storeLoad();
					}
				});
			}
		},

		/**
		 * Send all draft email records
		 */
		sendAll: function() {
			var updateTrafficLightArray = [];

			Ext.Array.forEach(this.getDraftEmails(), function(email, i, allEmails) {
				this.sendRecord(email, updateTrafficLightArray);
			}, this);
		},

		/**
		 * Updates selected record with Outgoing status
		 *
		 * @param {CMDBuild.model.tabs.Email.email} record
		 * @param {Array} trafficLightArray
		 */
		sendRecord: function(record, trafficLightArray) {
			trafficLightArray = trafficLightArray || [];

			if (!Ext.isEmpty(record)) {
				record.set(CMDBuild.core.proxy.CMProxyConstants.STATUS, CMDBuild.core.proxy.CMProxyConstants.OUTGOING);

				this.editRecord(record, trafficLightArray);
			}
		},

		/**
		 * Loads grid store with activityId parameter
		 *
		 * @param {Mixed} regenerateAllEmails
		 * @param {Boolean} forceRegeneration
		 */
		storeLoad: function(regenerateAllEmails, forceRegeneration) {
			if (!this.view.getStore().isLoading()) {
				regenerateAllEmails = regenerateAllEmails || false;
				forceRegeneration = forceRegeneration || false;

				this.parentDelegate.isWidgetBusy = true; // Setup widget busy state and the begin of store load

				this.view.getStore().load({
					params: {
						activityId: this.parentDelegate.getSelectedEntityId()
					},
					scope: this,
					callback: function(records, operation, success) {
						this.parentDelegate.isWidgetBusy = false;

						this.parentDelegate.getAllTemplatesData(regenerateAllEmails, forceRegeneration);
					}
				});
			}
		}
	});

})();