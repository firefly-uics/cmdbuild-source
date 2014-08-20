(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyEmailAccounts');
	Ext.require('CMDBuild.core.proxy.CMProxyEmailTemplates');

	Ext.define('CMDBuild.controller.administration.email.CMEmailTemplatesController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		form: undefined,
		grid: undefined,
		selectedName: undefined,
		selectionModel: undefined,
		valuesWindowDataBuffer: undefined, // Buffer to hold all values windows grid datas
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.email.CMEmailTemplates} view
		 *
		 * @overwrite
		 */
		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange
			this.grid = view.grid;
			this.form = view.form;
			this.view.delegate = this;
			this.grid.delegate = this;
			this.form.delegate = this;

			this.selectionModel = this.grid.getSelectionModel();
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
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();

				case 'onAddButtonClick':
					return this.onAddButtonClick();

				case 'onItemDoubleClick':
				case 'onModifyButtonClick':
					return this.onModifyButtonClick();

				case 'onRemoveButtonClick':
					return this.onRemoveButtonClick();

				case 'onRowSelected':
					return this.onRowSelected();

				case 'onSaveButtonClick':
					return this.onSaveButtonClick();

				case 'onVariablesButtonClick':
					return this.onVariablesButtonClick();

				case 'onVariablesWindowAbort':
					return this.onVariablesWindowAbort();

				case 'onVariablesWindowSave':
					return this.onVariablesWindowSave();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		// Variables window controller functions
			onVariablesButtonClick: function() {
				this.variablesWindow = Ext.create('CMDBuild.view.administration.email.CMEmailTemplatesVariablesWindow');
				this.setVariableWindowGridDatas(this.valuesWindowDataBuffer);
				this.variablesWindow.delegate = this;
				this.variablesWindow.show();
			},

			onVariablesWindowAbort: function() {
				this.variablesWindow.hide();
				this.setVariableWindowGridDatas(this.valuesWindowDataBuffer);
			},

			onVariablesWindowSave: function() {
				this.variablesWindow.hide();
				this.valuesWindowDataBuffer = this.getVariableWindowGridDatas();
			},

			// GETters functions
				/**
				 * @return {Object} data
				 *
				 * 	Example:
				 * 		{
				 * 			key1: value1,
				 * 			key2: value2
				 * 		}
				 */
				getVariableWindowGridDatas: function() {
					var data = {};

					// To validate and filter grid rows
					this.variablesWindow.grid.getStore().each(function(record) {
						if (
							!Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.KEY))
							&& !Ext.isEmpty(record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE))
						) {
							data[record.get(CMDBuild.core.proxy.CMProxyConstants.KEY)] = record.get(CMDBuild.core.proxy.CMProxyConstants.VALUE);
						}
					});

					return data;
				},

			// SETters functions
				/**
				 * Rewrite of loadData
				 *
				 * @param {Object} data
				 */
				setVariableWindowGridDatas: function(data) {
					var store = this.variablesWindow.grid.getStore();
					store.removeAll();

					if (!Ext.isEmpty(data)) {
						for (var key in data) {
							var recordConf = {};

							recordConf[CMDBuild.core.proxy.CMProxyConstants.KEY] = key;
							recordConf[CMDBuild.core.proxy.CMProxyConstants.VALUE] = data[key] || '';

							store.add(recordConf);
						}
					}
				},
		// END: Variables window controller functions

		onAbortButtonClick: function() {
			if (!Ext.isEmpty(this.selectedName)) {
				this.onRowSelected();
			} else {
				this.form.reset();
				this.form.disableModify();
			}
		},

		onAddButtonClick: function() {
			this.selectionModel.deselectAll();
			this.selectedName = null;
			this.valuesWindowDataBuffer = null;
			this.form.reset();
			this.form.enableModify(true);
		},

		onModifyButtonClick: function() {
			this.form.disableCMTbar();
			this.form.enableCMButtons();
			this.form.enableModify(true);
			this.form.disableNameField();
		},

		onRemoveButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.administration.setup.remove,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == 'yes')
						this.removeItem();
				}
			});
		},

		onRowSelected: function() {
			if (this.selectionModel.hasSelection()) {
				var me = this;
				this.selectedName = this.selectionModel.getSelection()[0].get(CMDBuild.core.proxy.CMProxyConstants.NAME);

				this.selectedDataStore = CMDBuild.core.proxy.CMProxyEmailTemplates.get();
				this.selectedDataStore.load({
					params: {
						name: this.selectedName
					},
					callback: function() {
						me.form.loadRecord(this.getAt(0));
						me.valuesWindowDataBuffer = this.getAt(0).get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES);
						me.form.disableModify(true);
					}
				});
			}
		},

		onSaveButtonClick: function() {
			// Validate before save
			if (this.validate(this.form)) {
				var formData = this.form.getData(true);

				// To put and encode variablesWindow grid values
				formData[CMDBuild.core.proxy.CMProxyConstants.VARIABLES] = Ext.encode(this.valuesWindowDataBuffer);

				CMDBuild.LoadMask.get().show();

				if (Ext.isEmpty(formData.id)) {
					CMDBuild.core.proxy.CMProxyEmailTemplates.create({
						params: formData,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				} else {
					CMDBuild.core.proxy.CMProxyEmailTemplates.update({
						params: formData,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				}
			}
		},

		removeItem: function() {
			if (!Ext.isEmpty(this.selectedName)) {
				var me = this;
				var store = this.grid.store;

				CMDBuild.LoadMask.get().show();

				CMDBuild.core.proxy.CMProxyEmailTemplates.remove({
					params: {
						name: this.selectedName
					},
					scope: this,
					success: function() {
						this.form.reset();

						store.load({
							callback: function() {
								me.selectionModel.select(0, true);

								if (!me.selectionModel.hasSelection())
									me.form.disableModify();
							}
						});
					},
					callback: this.callback()
				});
			}
		},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 */
		success: function(result, options, decodedResult) {
			var me = this;
			var store = this.grid.store;

			store.load({
				callback: function() {
					var rowIndex = this.find(
						CMDBuild.core.proxy.CMProxyConstants.NAME,
						me.form.getForm().findField(CMDBuild.core.proxy.CMProxyConstants.NAME).getValue()
					);

					me.selectionModel.select(rowIndex, true);
					me.form.disableModify(true);
				}
			});
		}
	});

})();