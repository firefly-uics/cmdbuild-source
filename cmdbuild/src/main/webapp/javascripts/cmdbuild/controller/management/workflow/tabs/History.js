(function () {

	/**
	 * Processes specific history tab controller
	 */
	Ext.define('CMDBuild.controller.management.workflow.tabs.History', {
		extend: 'CMDBuild.controller.management.common.tabs.History',

		requires: ['CMDBuild.core.proxy.common.tabs.history.Processes'],

		mixins: {
			observable: 'Ext.util.Observable',
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		/**
		 * @cfg {CMDBuild.controller.management.workflow.CMModWorkflowController}
		 */
		parentDelegate: undefined,

		/**
		 * Attributes to hide from selectedEntity object
		 *
		 * @cfg {Array}
		 */
		attributesKeysToFilter: [
			'Id',
			'IdClass',
			'IdClass_value',
			CMDBuild.core.proxy.CMProxyConstants.BEGIN_DATE,
			CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME,
			CMDBuild.core.proxy.CMProxyConstants.USER
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'getTabHistoryGridColumns',
			'getTabHistoryGridStore',
			'onProcessesTabHistoryIncludeSystemActivitiesCheck',
			'onTabHistoryPanelShow = onTabHistoryIncludeRelationCheck', // Reloads store to be consistent with includeRelationsCheckbox state
			'onTabHistoryRowExpand',
			'tabHistorySelectedEntityGet',
			'tabHistorySelectedEntitySet'
		],

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		entryType: undefined,

		/**
		 * @cfg {Array}
		 */
		managedStatuses: ['ABORTED', 'COMPLETED', 'OPEN', 'SUSPENDED', 'TERMINATED'],

		/**
		 * @cfg {Object}
		 *
		 * Ex. {
		 *		ABORTED: '...',
		 *		COMPLETED: '...',
		 *		OPEN: '...',
		 *		SUSPENDED: '...',
		 *		TERMINATED: '...'
		 *	}
		 */
		statusTranslationObject: {},

		/**
		 * @property {CMDBuild.model.CMProcessInstance}
		 */
		selectedEntity: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.workflow.CMModWorkflowController} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.statusBuildTranslationObject( ); // Build status translation object from lookup

			this.grid = Ext.create('CMDBuild.view.management.workflow.tabs.history.GridPanel', {
				delegate: this
			});

			this.view.add(this.grid);

			_CMWFState.addDelegate(this);
		},

		/**
		 * It's implemented with ugly workarounds because of server side ugly code.
		 *
		 * @override
		 */
		addCurrentCardToStore: function() {
			var selectedEntityAttributes = {};
			var selectedEntityValues = this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.VALUES);

			// Filter selectedEntity's attributes values to avoid the display of incorrect data
			Ext.Object.each(selectedEntityValues, function(key, value, myself) {
				if (!Ext.Array.contains(this.attributesKeysToFilter, key) && key.indexOf('_') != 0)
					selectedEntityAttributes[key] = value;
			}, this);

			selectedEntityValues[CMDBuild.core.proxy.CMProxyConstants.USER] = this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.VALUES)[CMDBuild.core.proxy.CMProxyConstants.USER];

			this.valuesFormattingAndCompare(selectedEntityAttributes); // Formats values only

			this.clearStoreAdd(this.buildCurrentEntityModel(selectedEntityAttributes));

			this.callParent(arguments);
		},

		/**
		 * @param {Object} entityAttributeData
		 *
		 * @returns {CMDBuild.model.common.tabs.history.processes.CardRecord} currentEntityModel
		 */
		buildCurrentEntityModel: function(entityAttributeData) {
			var performers = [];

			// Build performers array
			Ext.Array.forEach(this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_INSTANCE_INFO_LIST), function(activityObject, i, array) {
				if (!Ext.isEmpty(activityObject[CMDBuild.core.proxy.CMProxyConstants.PERFORMER_NAME]))
					performers.push(activityObject[CMDBuild.core.proxy.CMProxyConstants.PERFORMER_NAME]);
			}, this);

			var currentEntityModel = Ext.create('CMDBuild.model.common.tabs.history.processes.CardRecord', this.selectedEntity.getData());
			currentEntityModel.set(CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_NAME, this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.VALUES)['Code']);
			currentEntityModel.set(CMDBuild.core.proxy.CMProxyConstants.PERFORMERS, performers);
			currentEntityModel.set(CMDBuild.core.proxy.CMProxyConstants.STATUS, this.statusTranslationGet(this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.FLOW_STATUS)));
			currentEntityModel.set(CMDBuild.core.proxy.CMProxyConstants.VALUES, entityAttributeData);
			currentEntityModel.commit();

			return currentEntityModel;
		},

		/**
		 * Adds clear and re-apply filters functionalities
		 *
		 * @param {Array or Object} itemsToAdd
		 *
		 * @override
		 */
		clearStoreAdd: function(itemsToAdd) {
			this.grid.getStore().clearFilter();

			this.callParent(arguments);

			this.onProcessesTabHistoryIncludeSystemActivitiesCheck();
		},

		/**
		 * @param {CMDBuild.model.common.tabs.history.classes.CardRecord} record
		 *
		 * @override
		 */
		currentCardRowExpand: function(record) {
			var predecessorRecord = this.grid.getStore().getAt(1); // Get expanded record predecessor record
			var selectedEntityAttributes = {};
			var selectedEntityValues = this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.VALUES);

			// Filter selectedEntity's attributes values to avoid the display of incorrect data
			Ext.Object.each(selectedEntityValues, function(key, value, myself) {
				if (!Ext.Array.contains(this.attributesKeysToFilter, key) && key.indexOf('_') != 0)
					selectedEntityAttributes[key] = value;
			}, this);

			selectedEntityValues[CMDBuild.core.proxy.CMProxyConstants.USER] = this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.VALUES)[CMDBuild.core.proxy.CMProxyConstants.USER];

			if (!Ext.isEmpty(predecessorRecord)) {
				var predecessorParams = {};
				predecessorParams[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = predecessorRecord.get(CMDBuild.core.proxy.CMProxyConstants.ID); // Historic card ID
				predecessorParams[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = this.selectedEntity.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME);

				this.getProxy().getHistoric({
					params: predecessorParams,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.valuesFormattingAndCompare(selectedEntityAttributes, decodedResponse.response[CMDBuild.core.proxy.CMProxyConstants.VALUES]);

						// Setup record property with historic card details to use XTemplate functionalities to render
						record.set(CMDBuild.core.proxy.CMProxyConstants.VALUES, selectedEntityAttributes);
					}
				});
			}
		},

		/**
		 * @returns {CMDBuild.core.proxy.common.tabs.history.Classes}
		 *
		 * @override
		 */
		getProxy: function() {
			return CMDBuild.core.proxy.common.tabs.history.Processes;
		},

		/**
		 * @returns {Array} columns
		 *
		 * @override
		 */
		getTabHistoryGridColumns: function() {
			var columns = this.callParent(arguments);

			if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.proxy.CMProxyConstants.SIMPLE_HISTORY_MODE_FOR_PROCESS)) {
				Ext.Array.push(columns, [
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_NAME,
						text: CMDBuild.Translation.activityName,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.PERFORMERS,
						text: CMDBuild.Translation.activityPerformer,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.STATUS,
						text: CMDBuild.Translation.status,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						flex: 1
					}
				]);
			}

			return columns;
		},

		/**
		 * Equals to onEntryTypeSelected in classes
		 *
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 */
		onProcessClassRefChange: function(entryType) {
			this.entryType = entryType;

			this.view.disable();
		},

		/**
		 * Include or not System activities rows in history grid.
		 */
		onProcessesTabHistoryIncludeSystemActivitiesCheck: function() {
			this.getRowExpanderPlugin().collapseAll();

			if (this.grid.includeSystemActivitiesCheckbox.getValue()) { // Checked: Remove any filter from store
				if (this.grid.getStore().isFiltered()) {
					this.grid.getStore().clearFilter();
					this.grid.getStore().sort(); // Resort store because clearFilter() breaks it
				}
			} else { // Unchecked: Apply filter to hide 'System' activities rows
				this.grid.getStore().filterBy(function(record, id) {
					return record.get(CMDBuild.core.proxy.CMProxyConstants.USER).indexOf('system') < 0; // System user name
				}, this);
			}
		},

		/**
		 * Equals to onCardSelected in classes
		 *
		 * @param {CMDBuild.model.CMProcessInstance} processInstance
		 */
		onProcessInstanceChange: function(processInstance) {
			this.selectedEntity = processInstance;

			this.view.setDisabled(processInstance.isNew());

			this.cmfg('onTabHistoryPanelShow');
		},

		// Status translation management
			statusBuildTranslationObject: function() {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.TYPE] = 'FlowStatus';
				params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = true;
				params[CMDBuild.core.proxy.CMProxyConstants.SHORT] = false;

				CMDBuild.ServiceProxy.lookup.get({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						Ext.Array.forEach(decodedResponse.rows, function(lookup, i, array) {
							switch (lookup['Code']) {
								case 'closed.aborted': {
									this.statusTranslationObject['ABORTED'] = lookup['Description'];
								} break;

								case 'closed.completed': {
									this.statusTranslationObject['COMPLETED'] = lookup['Description'];
								} break;

								case 'closed.terminated': {
									this.statusTranslationObject['TERMINATED'] = lookup['Description'];
								} break;

								case 'open.running': {
									this.statusTranslationObject['OPEN'] = lookup['Description'];
								} break;

								case 'open.not_running.suspended': {
									this.statusTranslationObject['SUSPENDED'] = lookup['Description'];
								} break;
							}
						}, this);
					}
				});
			},

			/**
			 * @param {String} status
			 *
			 * @returns {String or null}
			 */
			statusTranslationGet: function(status) {
				if (Ext.Array.contains(this.managedStatuses, status))
					return this.statusTranslationObject[status];

				return null;
			}
	});

})();