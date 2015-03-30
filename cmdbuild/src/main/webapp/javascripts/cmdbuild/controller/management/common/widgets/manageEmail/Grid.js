(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.Grid', {

		requires: [
			'CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.manageEmail.ManageEmail'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow}
		 */
		controllerEmailWindow: undefined,

		/**
		 * @property {Mixed}
		 */
		emailWindow: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.widgets.manageEmail.Grid}
		 */
		view: undefined,

		/**
		 * @cfg {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail} configObject.parentDelegate
		 * @param {CMDBuild.controller.management.common.widgets.manageEmail.Grid} configObject.view
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view.delegate = this;
			this.widgetConf = this.cmOn('getWidgetConf');
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
				case 'onGridAddEmailButtonClick':
					return this.onGridAddEmailButtonClick(param);

				case 'onGridDeleteEmailButtonClick':
					return this.onGridDeleteEmailButtonClick(param);

				case 'onGridEditEmailButtonClick':
					return this.onGridEditEmailButtonClick(param);

				case 'onGridItemDoubleClick':
					return this.onGridItemDoubleClick(param);

				case 'onGridRegenerationEmailButtonClick':
					return this.onGridRegenerationEmailButtonClick(param);

				case 'onGridReplyEmailButtonClick':
					return this.onGridReplyEmailButtonClick(param);

				case 'onGridSendEmailButtonClick':
					return this.onGridSendEmailButtonClick(param);

				case 'onGridViewEmailButtonClick':
					return this.onGridViewEmailButtonClick(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 * @param {Array} regenerationTrafficLightArray
		 * @param {Function} success
		 */
		addRecord: function(record, regenerationTrafficLightArray, success) {
			if (!Ext.Object.isEmpty(record)) {
				CMDBuild.core.proxy.widgets.manageEmail.ManageEmail.create({
					params: record.getAsParams(),
					scope: this,
					loadMask: this.cmOn('getGlobalLoadMask'),
					failure: function(response, options, decodedResponse) {
						CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.emailCreate, false);
					},
					success: success || function(response, options, decodedResponse) {
						if (CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail.trafficLightArrayCheck(record, regenerationTrafficLightArray) || Ext.isEmpty(regenerationTrafficLightArray))
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
		 * @return {CMDBuild.model.widget.ManageEmail.email}
		 */
		createRecord: function(recordValues) {
			recordValues = recordValues || {};
			recordValues[CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID] = recordValues[CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID] || this.parentDelegate.getActivityId();
			recordValues[CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION] = false;
			recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] = recordValues.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX) ? recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] : this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX];

			return Ext.create('CMDBuild.model.widget.ManageEmail.email', recordValues);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 * @param {Array} regenerationTrafficLightArray
		 */
		editRecord: function(record, regenerationTrafficLightArray) {
			if (!Ext.Object.isEmpty(record)) {
				CMDBuild.core.proxy.widgets.manageEmail.ManageEmail.update({
					params: record.getAsParams(),
					scope: this,
					loadMask: this.cmOn('getGlobalLoadMask'),
					failure: function(response, options, decodedResponse) {
						CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.emailUpdate, false);
					},
					success: function(response, options, decodedResponse) {
						if (CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail.trafficLightArrayCheck(record, regenerationTrafficLightArray) || Ext.isEmpty(regenerationTrafficLightArray))
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
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
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

					Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
						parentDelegate: me,
						record: record
					});

					this.storeLoad();
				}
			);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
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
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
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
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onGridEditEmailButtonClick: function(record) {
			Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
				parentDelegate: this,
				record: record,
				windowMode: 'edit'
			});
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onGridItemDoubleClick: function(record) {
			if (this.recordIsEditable(record)) {
				this.onGridEditEmailButtonClick(record);
			} else {
				this.onGridViewEmailButtonClick(record);
			}
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onGridRegenerationEmailButtonClick: function(record) {
			if (!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)))
				this.parentDelegate.regenerateSelectedEmails([record]);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onGridReplyEmailButtonClick: function(record) {
			var content = '<p>'
					+ CMDBuild.Translation.onDay + ' ' + record.get(CMDBuild.core.proxy.CMProxyConstants.DATE)
					+ ', <' + record.get(CMDBuild.core.proxy.CMProxyConstants.FROM) + '> ' + CMDBuild.Translation.hasWrote
				+ ':</p>'
				+ '<blockquote>' + record.get(CMDBuild.core.proxy.CMProxyConstants.BODY) + '</blockquote>';

			var replyRecordData = {};
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.ACCOUNT] = record.get(CMDBuild.core.proxy.CMProxyConstants.ACCOUNT);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID] = record.get(CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID) || this.parentDelegate.getActivityId();
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.BCC] = record.get(CMDBuild.core.proxy.CMProxyConstants.BCC);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.BODY] = content;
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.CC] = record.get(CMDBuild.core.proxy.CMProxyConstants.CC);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION] = false;
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH] = record.get(CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] = record.get(CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] = 'RE: ' + record.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.TO] = record.get(CMDBuild.core.proxy.CMProxyConstants.FROM) || record.get(CMDBuild.core.proxy.CMProxyConstants.TO);

			Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
				parentDelegate: this,
				record: Ext.create('CMDBuild.model.widget.ManageEmail.email', replyRecordData),
				windowMode: 'reply'
			});
		},
		/**
		 * Updates selected record with Outgoing status
		 *
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onGridSendEmailButtonClick: function(record) {
			if (!Ext.isEmpty(record)) {
				record.set(CMDBuild.core.proxy.CMProxyConstants.STATUS, CMDBuild.core.proxy.CMProxyConstants.OUTGOING);

				this.editRecord(record);
			}
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 */
		onGridViewEmailButtonClick: function(record) {
			Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
				parentDelegate: this,
				record: record,
				windowMode: 'view'
			});
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 *
		 * @return {Boolean}
		 */
		recordIsEditable: function(record) {
			return record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS) == CMDBuild.core.proxy.CMProxyConstants.DRAFT;
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 *
		 * @return {Boolean}
		 */
		recordIsReceived: function(record) {
			return (record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS) == CMDBuild.core.proxy.CMProxyConstants.RECEIVED);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 * @param {Array} regenerationTrafficLightArray
		 */
		removeRecord: function(record, regenerationTrafficLightArray) {
			if (!Ext.Object.isEmpty(record)) {
				CMDBuild.core.proxy.widgets.manageEmail.ManageEmail.remove({
					params: record.getAsParams([CMDBuild.core.proxy.CMProxyConstants.ID, CMDBuild.core.proxy.CMProxyConstants.TEMPORARY]),
					scope: this,
					loadMask: this.cmOn('getGlobalLoadMask'),
					failure: function(response, options, decodedResponse) {
						CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.emailRemove, false);
					},
					success: function(response, options, decodedResponse) {
						if (CMDBuild.controller.management.common.widgets.manageEmail.ManageEmail.trafficLightArrayCheck(record, regenerationTrafficLightArray) || Ext.isEmpty(regenerationTrafficLightArray))
							this.storeLoad();
					}
				});
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
						activityId: this.parentDelegate.getActivityId()
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