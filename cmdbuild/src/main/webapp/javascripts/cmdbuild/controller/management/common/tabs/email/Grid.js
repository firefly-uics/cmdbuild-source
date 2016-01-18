(function () {

	Ext.define('CMDBuild.controller.management.common.tabs.email.Grid', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.controller.management.common.tabs.email.Email',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.common.tabs.email.Email',
			'CMDBuild.core.Message'
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
			'onGridViewEmailButtonClick',
			'sendAll',
			'setUiState',
			'storeLoad'
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
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.tabs.email.Email} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.tabs.email.GridPanel', { delegate: this });
		},

		/**
		 * @param {Mixed} record
		 * @param {Array} regenerationTrafficLightArray
		 * @param {Function} success
		 */
		addRecord: function(record, regenerationTrafficLightArray, success) {
			if (!Ext.Object.isEmpty(record)) {
				CMDBuild.core.proxy.common.tabs.email.Email.create({
					params: record.getAsParams(),
					scope: this,
					loadMask: this.cmfg('getGlobalLoadMask'),
					failure: function(response, options, decodedResponse) {
						CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.emailCreate, false);
					},
					success: success || function(response, options, decodedResponse) {
						if (CMDBuild.controller.management.common.tabs.email.Email.trafficLightArrayCheck(record, regenerationTrafficLightArray))
							this.storeLoad();
					}
				});
			} else {
				_warning('tried to add empty record', this);

				this.storeLoad();
			}
		},

		/**
		 * Creates email model with default attributes setup
		 *
		 * @param {Object} recordValues
		 *
		 * @return {Mixed}
		 */
		createRecord: function(recordValues) {
			recordValues = recordValues || {};
			recordValues[CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION] = false;
			recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] = recordValues.hasOwnProperty(CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX) ? recordValues[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] : this.cmfg('configurationGet')[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX];
			recordValues[CMDBuild.core.proxy.CMProxyConstants.REFERENCE] = this.cmfg('selectedEntityIdGet');
			recordValues[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = this.cmfg('selectedEntityIdGet') < 0; // Setup temporary parameter

			return Ext.create('CMDBuild.model.common.tabs.email.Email', recordValues);
		},

		/**
		 * @param {Mixed} record
		 * @param {Array} regenerationTrafficLightArray
		 */
		editRecord: function(record, regenerationTrafficLightArray) {
			if (!Ext.Object.isEmpty(record)) {
				CMDBuild.core.proxy.common.tabs.email.Email.update({
					params: record.getAsParams(),
					scope: this,
					loadMask: this.cmfg('getGlobalLoadMask'),
					failure: function(response, options, decodedResponse) {
						CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.emailUpdate, false);
					},
					success: function(response, options, decodedResponse) {
						if (CMDBuild.controller.management.common.tabs.email.Email.trafficLightArrayCheck(record, regenerationTrafficLightArray))
							this.storeLoad();
					}
				});
			} else {
				_warning('tried to edit empty record', this);

				this.storeLoad();
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
		 * @param {Mixed} record
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
		 * @param {Mixed} record
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
		 * @param {Mixed} record
		 */
		onGridEditEmailButtonClick: function(record) {
			Ext.create('CMDBuild.controller.management.common.tabs.email.EmailWindow', {
				parentDelegate: this,
				record: record,
				windowMode: 'edit'
			});
		},

		/**
		 * @param {Mixed} record
		 */
		onGridItemDoubleClick: function(record) {
			if (
				!this.cmfg('configurationGet')[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY]
				&& this.cmfg('editModeGet')
				&& this.recordIsEditable(record)
			) {
				this.onGridEditEmailButtonClick(record);
			} else {
				this.onGridViewEmailButtonClick(record);
			}
		},

		/**
		 * @param {Mixed} record
		 */
		onGridRegenerationEmailButtonClick: function(record) {
			if (!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)))
				this.cmfg('regenerateSelectedEmails', [record]);
		},

		/**
		 * @param {Mixed} record
		 */
		onGridReplyEmailButtonClick: function(record) {
			var content = '<p>'
					+ CMDBuild.Translation.onDay + ' ' + record.get(CMDBuild.core.proxy.CMProxyConstants.DATE)
					+ ', <' + record.get(CMDBuild.core.proxy.CMProxyConstants.FROM) + '> ' + CMDBuild.Translation.hasWrote
				+ ':</p>'
				+ '<blockquote>' + record.get(CMDBuild.core.proxy.CMProxyConstants.BODY) + '</blockquote>';

			var replyRecordData = {};
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.ACCOUNT] = record.get(CMDBuild.core.proxy.CMProxyConstants.ACCOUNT);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.BCC] = record.get(CMDBuild.core.proxy.CMProxyConstants.BCC);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.BODY] = content;
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.CC] = record.get(CMDBuild.core.proxy.CMProxyConstants.CC);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION] = false;
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH] = record.get(CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] = record.get(CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.REFERENCE] = this.cmfg('selectedEntityIdGet');
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] = 'RE: ' + record.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT);
			replyRecordData[CMDBuild.core.proxy.CMProxyConstants.TO] = record.get(CMDBuild.core.proxy.CMProxyConstants.FROM) || record.get(CMDBuild.core.proxy.CMProxyConstants.TO);

			Ext.create('CMDBuild.controller.management.common.tabs.email.EmailWindow', {
				parentDelegate: this,
				record: Ext.create('CMDBuild.model.common.tabs.email.Email', replyRecordData),
				windowMode: 'reply'
			});
		},

		/**
		 * @param {Mixed} record
		 */
		onGridSendEmailButtonClick: function(record) {
			this.sendRecord(record);
		},

		/**
		 * @param {Mixed} record
		 */
		onGridViewEmailButtonClick: function(record) {
			Ext.create('CMDBuild.controller.management.common.tabs.email.EmailWindow', {
				parentDelegate: this,
				record: record,
				windowMode: 'view'
			});
		},

		/**
		 * @param {Mixed} record
		 *
		 * @return {Boolean}
		 */
		recordIsEditable: function(record) {
			return record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS) == CMDBuild.core.proxy.CMProxyConstants.DRAFT;
		},

		/**
		 * @param {Mixed} record
		 *
		 * @return {Boolean}
		 */
		recordIsSendable: function(record) {
			return (
				!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.TO))
				&& !Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT))
				&& record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS) != CMDBuild.core.proxy.CMProxyConstants.OUTGOING
				&& record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS) != CMDBuild.core.proxy.CMProxyConstants.SENT
			);
		},

		/**
		 * @param {Mixed} record
		 * @param {Array} regenerationTrafficLightArray
		 */
		removeRecord: function(record, regenerationTrafficLightArray) {
			if (!Ext.Object.isEmpty(record)) {
				CMDBuild.core.proxy.common.tabs.email.Email.remove({
					params: record.getAsParams([CMDBuild.core.proxy.CMProxyConstants.ID, CMDBuild.core.proxy.CMProxyConstants.TEMPORARY]),
					scope: this,
					loadMask: this.cmfg('getGlobalLoadMask'),
					failure: function(response, options, decodedResponse) {
						CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.emailRemove, false);
					},
					success: function(response, options, decodedResponse) {
						if (CMDBuild.controller.management.common.tabs.email.Email.trafficLightArrayCheck(record, regenerationTrafficLightArray))
							this.storeLoad();
					}
				});
			} else {
				_warning('tried to remove empty record', this);

				this.storeLoad();
			}
		},

		/**
		 * Disable topToolbar evaluating readOnly and edit mode (disable only when readOnly = false and editMode = true)
		 */
		setUiState: function() {
			this.view.setDisabledTopBar(
				!(
					!this.cmfg('configurationGet')[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY]
					&& this.cmfg('editModeGet')
				)
			);
		},

		/**
		 * Send all draft email records
		 */
		sendAll: function() {
			if (!Ext.isEmpty(this.getDraftEmails())) {
				var updateTrafficLightArray = [];

				Ext.Array.forEach(this.getDraftEmails(), function(email, i, allEmails) {
					this.sendRecord(email, updateTrafficLightArray);
				}, this);
			}
		},

		/**
		 * Updates selected record with Outgoing status
		 *
		 * @param {Mixed} record
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
		 */
		storeLoad: function() {
			this.cmfg('busyStateSet', true); // Setup widget busy state and the begin of store load

			this.view.getStore().removeAll(); // Clear store before load new one

			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.REFERENCE] = this.cmfg('selectedEntityIdGet');

			this.view.getStore().load({
				params: params,
				scope: this,
				callback: function(records, operation, success) {
					if (success) {
						this.cmfg('getAllTemplatesData');
					} else {
						CMDBuild.core.Message.error(null, {
							text: CMDBuild.Translation.errors.unknown_error,
							detail: operation.error
						});
					}
				}
			});
		}
	});

})();