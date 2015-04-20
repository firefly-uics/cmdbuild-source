(function() {

	Ext.define('CMDBuild.controller.administration.email.CMEmailAccountsController', {
		extend: 'CMDBuild.controller.common.AbstractController',

		/**
		 * @cfg {CMDBuild.controller.administration.email.Email}
		 */
		parentDelegate: undefined,

		form: undefined,
		grid: undefined,
		selectedName: undefined,
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.email.Email} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.email.CMEmailAccounts', {
				delegate: this
			});

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
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

				case 'onSetDefaultButtonClick':
					return this.onSetDefaultButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
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
			this.grid.getSelectionModel().deselectAll();
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
				title: CMDBuild.Translation.common.confirmpopup.title,
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
								me.grid.getSelectionModel().select(0, true);

								if (!me.grid.getSelectionModel().hasSelection())
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

					me.grid.getSelectionModel().select(rowIndex, true);
					me.form.disableModify(true);
				}
			});
		},

		callback: function() {
			CMDBuild.LoadMask.get().hide();
		}
	});

})();