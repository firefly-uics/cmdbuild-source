(function () {

	Ext.define('CMDBuild.controller.management.common.tabs.email.Grid', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.controller.management.common.tabs.email.Email',
			'CMDBuild.core.constants.Proxy',
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
			'storeLoad',
			'tabEmailGridRecordIsSendable'
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

			this.view = Ext.create('CMDBuild.view.management.common.tabs.email.GridPanel', {
				delegate: this
			});
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
			recordValues[CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION] = false;
			recordValues[CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX] = recordValues.hasOwnProperty(CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX) ? recordValues[CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX] : this.cmfg('configurationGet')[CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX];
			recordValues[CMDBuild.core.constants.Proxy.REFERENCE] = this.cmfg('selectedEntityIdGet');
			recordValues[CMDBuild.core.constants.Proxy.TEMPORARY] = this.cmfg('selectedEntityIdGet') < 0; // Setup temporary parameter

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
			return this.getEmailsByGroup(CMDBuild.core.constants.Proxy.DRAFT);
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
			return !Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.TEMPLATE));
		},

		onGridAddEmailButtonClick: function() {
			var me = this;
			var record = this.createRecord();

			this.addRecord( // To generate an emailId
				record,
				null,
				function(response, options, decodedResponse) { // Success function override
					record.set(CMDBuild.core.constants.Proxy.ID, decodedResponse.response);

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
				!this.cmfg('configurationGet')[CMDBuild.core.constants.Proxy.READ_ONLY]
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
			if (!Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.TEMPLATE)))
				this.cmfg('regenerateSelectedEmails', [record]);
		},

		/**
		 * @param {Mixed} record
		 */
		onGridReplyEmailButtonClick: function(record) {
			var content = '<p>'
					+ CMDBuild.Translation.onDay + ' ' + record.get(CMDBuild.core.constants.Proxy.DATE)
					+ ', <' + record.get(CMDBuild.core.constants.Proxy.FROM) + '> ' + CMDBuild.Translation.hasWrote
				+ ':</p>'
				+ '<blockquote>' + record.get(CMDBuild.core.constants.Proxy.BODY) + '</blockquote>';

			var replyRecordData = {};
			replyRecordData[CMDBuild.core.constants.Proxy.ACCOUNT] = record.get(CMDBuild.core.constants.Proxy.ACCOUNT);
			replyRecordData[CMDBuild.core.constants.Proxy.BCC] = record.get(CMDBuild.core.constants.Proxy.BCC);
			replyRecordData[CMDBuild.core.constants.Proxy.BODY] = content;
			replyRecordData[CMDBuild.core.constants.Proxy.CC] = record.get(CMDBuild.core.constants.Proxy.CC);
			replyRecordData[CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION] = false;
			replyRecordData[CMDBuild.core.constants.Proxy.NOTIFY_WITH] = record.get(CMDBuild.core.constants.Proxy.NOTIFY_WITH);
			replyRecordData[CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX] = record.get(CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX);
			replyRecordData[CMDBuild.core.constants.Proxy.REFERENCE] = this.cmfg('selectedEntityIdGet');
			replyRecordData[CMDBuild.core.constants.Proxy.SUBJECT] = 'RE: ' + record.get(CMDBuild.core.constants.Proxy.SUBJECT);
			replyRecordData[CMDBuild.core.constants.Proxy.TO] = record.get(CMDBuild.core.constants.Proxy.FROM) || record.get(CMDBuild.core.constants.Proxy.TO);

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
			return record.get(CMDBuild.core.constants.Proxy.STATUS) == CMDBuild.core.constants.Proxy.DRAFT;
		},

		/**
		 * @param {Mixed} record
		 * @param {Array} regenerationTrafficLightArray
		 */
		removeRecord: function(record, regenerationTrafficLightArray) {
			if (!Ext.Object.isEmpty(record)) {
				CMDBuild.core.proxy.common.tabs.email.Email.remove({
					params: record.getAsParams([CMDBuild.core.constants.Proxy.ID, CMDBuild.core.constants.Proxy.TEMPORARY]),
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
					!this.cmfg('configurationGet')[CMDBuild.core.constants.Proxy.READ_ONLY]
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
				record.set(CMDBuild.core.constants.Proxy.STATUS, CMDBuild.core.constants.Proxy.OUTGOING);

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
			params[CMDBuild.core.constants.Proxy.REFERENCE] = this.cmfg('selectedEntityIdGet');

			this.view.getStore().load({
				params: params,
				scope: this,
				callback: function(records, operation, success) {
					if (success)
						this.cmfg('getAllTemplatesData');
				}
			});
		},


		/**
		 * @param {Mixed} record
		 *
		 * @return {Boolean}
		 */
		tabEmailGridRecordIsSendable: function(record) {
			return (
				!Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.TO))
				&& !Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.SUBJECT))
				&& record.get(CMDBuild.core.constants.Proxy.STATUS) != CMDBuild.core.constants.Proxy.OUTGOING
				&& record.get(CMDBuild.core.constants.Proxy.STATUS) != CMDBuild.core.constants.Proxy.RECEIVED
				&& record.get(CMDBuild.core.constants.Proxy.STATUS) != CMDBuild.core.constants.Proxy.SENT
			);
		}
	});

})();