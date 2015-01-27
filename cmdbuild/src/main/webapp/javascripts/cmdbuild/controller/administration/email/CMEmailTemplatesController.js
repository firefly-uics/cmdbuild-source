(function() {

	Ext.define('CMDBuild.controller.administration.email.CMEmailTemplatesController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyEmailTemplates'
		],

		/**
		 * @property {CMDBuild.view.administration.email.CMEmailTemplatesForm}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.CMEmailTemplatesGrid}
		 */
		grid: undefined,

		/**
		 * @property {String}
		 */
		selectedName: undefined,

		/**
		 * Buffer to hold all values windows grid datas
		 *
		 * @property {Object}
		 */
		valuesWindowDataBuffer: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.CMEmailTemplatesVariablesWindow}
		 */
		variablesWindow: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.CMEmailTemplates}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.email.CMEmailTemplates} view
		 *
		 * @override
		 */
		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange
			this.grid = view.grid;
			this.form = view.form;
			this.view.delegate = this;
			this.grid.delegate = this;
			this.form.delegate = this;
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

		/**
		 *  As variables window delegate
		 */
			onVariablesButtonClick: function() {
				this.variablesWindow = Ext.create('CMDBuild.view.administration.email.CMEmailTemplatesVariablesWindow', {
					delegate: this
				});
				this.setVariableWindowGridDatas(this.valuesWindowDataBuffer);
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
		// END: As variables window delegate

		onAbortButtonClick: function() {
			if (!Ext.isEmpty(this.selectedName)) {
				this.onRowSelected();
			} else {
				this.form.reset();
				this.form.disableModify();
			}
		},

		onAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();
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
			if (this.grid.getSelectionModel().hasSelection()) {
				var me = this;
				this.selectedName = this.grid.getSelectionModel().getSelection()[0].get(CMDBuild.core.proxy.CMProxyConstants.NAME);

				CMDBuild.core.proxy.CMProxyEmailTemplates.get().load({
					params: {
						name: this.selectedName
					},
					callback: function(records, operation, success) {
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
				CMDBuild.LoadMask.get().show();
				CMDBuild.core.proxy.CMProxyEmailTemplates.remove({
					params: {
						name: this.selectedName
					},
					scope: this,
					success: function() {
						this.form.reset();

						this.grid.getStore().load({
							scope: this,
							callback: function() {
								this.grid.getSelectionModel().select(0, true);

								if (!this.grid.getSelectionModel().hasSelection())
									this.form.disableModify();
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

			this.grid.getStore().load({
				callback: function(records, operation, success) {
					var rowIndex = this.find(
						CMDBuild.core.proxy.CMProxyConstants.NAME,
						me.form.getForm().findField(CMDBuild.core.proxy.CMProxyConstants.NAME).getValue()
					);

					me.grid.getSelectionModel().select(rowIndex, true);
					me.form.disableModify(true);
				}
			});
		}
	});

})();