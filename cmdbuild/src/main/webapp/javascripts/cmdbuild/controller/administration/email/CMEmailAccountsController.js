(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyEmailAccounts');

	Ext.define('CMDBuild.controller.administration.email.CMEmailAccountsController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		form: undefined,
		grid: undefined,
		selectedName: undefined,
		selectionModel: undefined,
		view: undefined,

		/**
		 * @param (Object) view
		 */
		// Overwrite
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
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
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

				case 'onSetDefaultButtonClick':
					return this.onSetDefaultButtonClick();

				default: {
					if (
						this.parentDelegate
						&& typeof this.parentDelegate == 'object'
					) {
						return this.parentDelegate.cmOn(name, param, callBack);
					}
				}
			}
		},

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
				this.selectedName = this.selectionModel.getSelection()[0].get(CMDBuild.ServiceProxy.parameter.NAME);

				// Selected user asynchronous store query
				this.selectedDataStore = CMDBuild.core.proxy.CMProxyEmailAccounts.get();
				this.selectedDataStore.load({
					params: {
						name: this.selectedName
					},
					callback: function() {
						me.form.loadRecord(this.getAt(0));
						me.form.disableSetDefaultAndRemoveButton();
						me.form.disableModify(true);
					}
				});
			}
		},

		onSaveButtonClick: function() {
			// Validate before save
			if (this.validate(this.form)) {
				var formData = this.form.getData(true);

				CMDBuild.LoadMask.get().show();

				if (Ext.isEmpty(formData.id)) {
					CMDBuild.core.proxy.CMProxyEmailAccounts.create({
						params: formData,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				} else {
					CMDBuild.core.proxy.CMProxyEmailAccounts.update({
						params: formData,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				}
			}
		},

		onSetDefaultButtonClick: function() {
			CMDBuild.LoadMask.get().show();

			CMDBuild.core.proxy.CMProxyEmailAccounts.setDefault({
				params: { name: this.selectedName },
				scope: this,
				success: this.success,
				callback: this.callback
			});
		},

		removeItem: function() {
			if (!Ext.isEmpty(this.selectedName)) {
				var me = this;
				var store = this.grid.store;

				CMDBuild.LoadMask.get().show();

				CMDBuild.core.proxy.CMProxyEmailAccounts.remove({
					params: {
						name: this.selectedName
					},
					scope: this,
					success: function() {
						this.form.reset();

						store.load({
							callback: function() {
								me.selectionModel.select(0, true);
							}
						});
					},
					callback: this.callback()
				});
			}
		},

		/**
		 * @param (Object) result
		 * @param (Object) options
		 * @param (Object) decodedResult
		 */
		success: function(result, options, decodedResult) {
			var me = this;
			var store = this.grid.store;

			store.load({
				callback: function() {
					var rowIndex = this.find(
						CMDBuild.ServiceProxy.parameter.NAME,
						me.form.getForm().findField(CMDBuild.ServiceProxy.parameter.NAME).getValue()
					);

					me.selectionModel.select(rowIndex, true);
					me.form.disableModify(true);
				}
			});
		}
	});

})();