(function() {

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksFormConnectorController", {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		delegateStep: undefined,
		parentDelegate: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: 'connector',
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();

				case 'onAddButtonClick':
					return this.onAddButtonClick(name, param, callBack);

				case 'onClassSelected':
					this.onClassSelected(param.className);

				case 'onCloneButtonClick':
					return this.onCloneButtonClick();

				case 'onModifyButtonClick':
					return this.onModifyButtonClick();

				case 'onRemoveButtonClick':
					return this.onRemoveButtonClick();

				case 'onRowSelected':
					return this.onRowSelected();

				case 'onSaveButtonClick':
					return this.onSaveButtonClick();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Filter class store to delete unselected classes
		 *
		 * @return (Object) store
		 */
		getStoreFilteredClass: function() {
			var classStore = CMDBuild.core.proxy.CMProxyTasks.getClassStore();
			var store = Ext.create('Ext.data.Store', {
				autoLoad: true,
				fields: [CMDBuild.core.proxy.CMProxyConstants.NAME, CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
				data: []
			});

			CMDBuild.core.proxy.CMProxyTasks.getClassStore().each(function(record, id) {
				if (CMDBuild.Utils.inArray(record.get(CMDBuild.core.proxy.CMProxyConstants.NAME), this.delegateStep[3].getSelectedClassArray()))
					store.add({
						name: record.get(CMDBuild.core.proxy.CMProxyConstants.NAME),
						description: record.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION)
					});
			}, this);

			return store;
		},

		/**
		 * Filter source store to delete unselected views
		 *
		 * @return (Object) store
		 */
		getStoreFilteredSource: function() {
			var sourceArray = this.delegateStep[3].getSelectedSourceArray();
			var store = Ext.create('Ext.data.Store', {
				autoLoad: true,
				fields: [CMDBuild.core.proxy.CMProxyConstants.NAME],
				data: []
			});

			for(item in sourceArray)
				store.add({ name: sourceArray[item] });

			return store;
		},

		// overwrite
		onRowSelected: function() {
			if (this.selectionModel.hasSelection()) {
				this.selectedId = this.selectionModel.getSelection()[0].get(CMDBuild.core.proxy.CMProxyConstants.ID);

				// Selected task asynchronous store query
				this.selectedDataStore = CMDBuild.core.proxy.CMProxyTasks.get(this.taskType);
				this.selectedDataStore.load({
					scope: this,
					params: {
						id: this.selectedId
					},
					callback: function(records, operation, success) {
						if (!Ext.isEmpty(records)) {
							var record = records[0];

							this.parentDelegate.loadForm(this.taskType);

							// HOPING FOR A FIX: loadRecord() fails with comboboxes, and i can't find a working fix, so i must set all fields manually

							// Set step1 [0] datas
							this.delegateStep[0].setValueActive(record.get(CMDBuild.core.proxy.CMProxyConstants.ACTIVE));
							this.delegateStep[0].setValueDescription(record.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION));
							this.delegateStep[0].setValueId(record.get(CMDBuild.core.proxy.CMProxyConstants.ID));

							// Set step2 [1] datas
							this.delegateStep[1].setValueAdvancedFields(record.get(CMDBuild.core.proxy.CMProxyConstants.CRON_EXPRESSION));
							this.delegateStep[1].setValueBase(record.get(CMDBuild.core.proxy.CMProxyConstants.CRON_EXPRESSION));

							// Set step3 [2] datas
							this.delegateStep[2].setValueDataSourceConfiguration(
								record.get(CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_TYPE),
								record.get(CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_CONFIGURATION)
							);

							// Set step4 [3] datas

							// Set step5 [4] datas
							this.delegateStep[4].setData(record.get(CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_MAPPING));

							// Set step6 [5] datas

							this.view.disableModify(true);
						}
					}
				});

				this.view.wizard.changeTab(0);
			}
		},

		// overwrite
		onSaveButtonClick: function() {
			var formData = this.view.getData(true);
			var submitDatas = {};

			// Validate before save
			if (this.validate(formData[CMDBuild.core.proxy.CMProxyConstants.ACTIVE])) {
//				CMDBuild.LoadMask.get().show();

				submitDatas[CMDBuild.core.proxy.CMProxyConstants.CRON_EXPRESSION] = this.delegateStep[1].getCronDelegate().getValue(
					formData[CMDBuild.core.proxy.CMProxyConstants.CRON_INPUT_TYPE]
				);

				// Form submit values formatting
					var dataSourceType = this.delegateStep[2].getTypeDataSource();
					if (dataSourceType) {
						var configurationObject = {};

						switch (dataSourceType) {
							case 'db': {
								configurationObject[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_ADDRESS] = formData[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_ADDRESS];
								configurationObject[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_DB_NAME] = formData[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_DB_NAME];
								configurationObject[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_DB_PASSWORD] = formData[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_DB_PASSWORD];
								configurationObject[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_DB_PORT] = formData[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_DB_PORT];
								configurationObject[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_DB_TYPE] = formData[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_DB_TYPE];
								configurationObject[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_DB_USERNAME] = formData[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_DB_USERNAME];
								configurationObject[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_TABLE_VIEW_PREFIX] = formData[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_TABLE_VIEW_PREFIX];

								if (!Ext.isEmpty(formData[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_DB_INSATANCE_NAME]))
									configurationObject[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_DB_INSATANCE_NAME] = formData[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_DB_INSATANCE_NAME];
							} break;

							default:
								throw 'CMTasksFormConnectorController: onSaveButtonClick() datasource type not recognized';
						}

						submitDatas[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_TYPE] = dataSourceType;
						submitDatas[CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_CONFIGURATION] = Ext.encode(configurationObject);
					}

					var classMappingData = this.delegateStep[3].getData();
					if (!Ext.isEmpty(classMappingData))
						submitDatas[CMDBuild.core.proxy.CMProxyConstants.CLASS_MAPPING] = Ext.encode(classMappingData);

					var attributeMappingData = this.delegateStep[4].getData();
					if (!Ext.isEmpty(attributeMappingData))
						submitDatas[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_MAPPING] = Ext.encode(attributeMappingData);

				// Data filtering to submit only right values
				submitDatas[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = formData[CMDBuild.core.proxy.CMProxyConstants.ACTIVE];
				submitDatas[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = formData[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION];
				submitDatas[CMDBuild.core.proxy.CMProxyConstants.ID] = formData[CMDBuild.core.proxy.CMProxyConstants.ID];
_debug('Step 4 datas [3]');
_debug(this.delegateStep[3].getData());
_debug(Ext.encode(this.delegateStep[3].getData()));

_debug('Step 5 datas [4]');
_debug(this.delegateStep[4].getData());
_debug(Ext.encode(this.delegateStep[4].getData()));

_debug('Step 6 datas [5]');
_debug(Ext.encode(this.delegateStep[5].getData()));

_debug(formData);
_debug(submitDatas);
				if (Ext.isEmpty(formData[CMDBuild.core.proxy.CMProxyConstants.ID])) {
					CMDBuild.core.proxy.CMProxyTasks.create({
						type: this.taskType,
						params: submitDatas,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				} else {
					CMDBuild.core.proxy.CMProxyTasks.update({
						type: this.taskType,
						params: submitDatas,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				}
			}
		},

		/**
		 * Task validation
		 *
		 * @param (Boolean) enable
		 *
		 * @return (Boolean)
		 */
		// overwrite
		validate: function(enable) {
			// Cron field validation
			this.delegateStep[1].getCronDelegate().validate(enable);

			// DataSource configuration validation
			this.delegateStep[2].validate(enable);

			return this.callParent(arguments);
		},

		/**
		 * Function to validate single step grids deleting invalid fields
		 *
		 * @param (Object) gridStore
		 */
		validateStepGrid: function(gridStore) {
			if (gridStore.count() > 0) {
				gridStore.each(function(record, id) {
					if (
						!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME))
						&& !CMDBuild.Utils.inArray(record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME), this.delegateStep[3].getSelectedClassArray())
					)
						record.set(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME, '');

					if (
						!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME))
						&& !CMDBuild.Utils.inArray(record.get(CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME), this.delegateStep[3].getSelectedSourceArray())
					)
						record.set(CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME, '');
				}, this);
			}
		}
	});

})();