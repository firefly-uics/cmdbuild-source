(function() {

	Ext.define('CMDBuild.controller.administration.email.Accounts', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.email.Accounts',
			'CMDBuild.model.email.Accounts'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.email.Email}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onEmailAccountsAbortButtonClick',
			'onEmailAccountsAddButtonClick',
			'onEmailAccountsModifyButtonClick = onEmailAccountsItemDoubleClick',
			'onEmailAccountsRemoveButtonClick',
			'onEmailAccountsRowSelected',
			'onEmailAccountsSaveButtonClick',
			'onEmailAccountsSetDefaultButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.email.accounts.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.accounts.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.email.Accounts.singleAccount}
		 */
		selectedAccount: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.accounts.AccountsView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.email.Email} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.email.accounts.AccountsView', {
				delegate: this
			});

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		onEmailAccountsAbortButtonClick: function() {
			if (!Ext.isEmpty(this.selectedAccount)) {
				this.onEmailAccountsRowSelected();
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		onEmailAccountsAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.selectedAccount = null;

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.email.Accounts.singleAccount'));
		},

		onEmailAccountsModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onEmailAccountsRemoveButtonClick: function() {
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

		onEmailAccountsRowSelected: function() {
			if (this.grid.getSelectionModel().hasSelection()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.NAME] = this.grid.getSelectionModel().getSelection()[0].get(CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.core.proxy.email.Accounts.get({
					params: params,
					scope: this,
					failure: function(response, options, decodedResponse) {
						CMDBuild.Msg.error(
							CMDBuild.Translation.common.failure,
							Ext.String.format(CMDBuild.Translation.errors.getAccountWithNameFailure, this.selectedTemplate.get(CMDBuild.core.constants.Proxy.NAME)),
							false
						);
					},
					success: function(response, options, decodedResponse) {
						this.selectedAccount = Ext.create('CMDBuild.model.email.Accounts.singleAccount', decodedResponse.response);

						this.form.loadRecord(this.selectedAccount);
						this.form.setDisabledModify(true, true);
						this.setDisabledTopToolbarButtons(this.selectedAccount.get(CMDBuild.core.constants.Proxy.IS_DEFAULT));
					}
				});
			}
		},

		onEmailAccountsSaveButtonClick: function() {
			// Validate before save
			if (this.validate(this.form)) {
				var formData = this.form.getData(true);

				if (Ext.isEmpty(formData.id)) {
					CMDBuild.core.proxy.email.Accounts.create({
						params: formData,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.email.Accounts.update({
						params: formData,
						scope: this,
						success: this.success
					});
				}
			}
		},

		onEmailAccountsSetDefaultButtonClick: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.NAME] = this.selectedAccount.get(CMDBuild.core.constants.Proxy.NAME);

			CMDBuild.core.proxy.email.Accounts.setDefault({
				params: params,
				scope: this,
				success: this.success
			});
		},

		removeItem: function() {
			if (!Ext.isEmpty(this.selectedAccount)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.NAME] = this.selectedAccount.get(CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.core.proxy.email.Accounts.remove({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.form.reset();

						this.grid.getStore().load({
							scope: this,
							callback: function(records, operation, success) {
								this.grid.getSelectionModel().select(0, true);

								if (!this.grid.getSelectionModel().hasSelection())
									this.form.setDisabledModify(true, true, true);
							}
						});
					}
				});
			}
		},

		/**
		 * Set disable state to setDefaultButton and removeButton
		 *
		 * @param {Boolean} state
		 */
		setDisabledTopToolbarButtons: function(state) {
			if(Ext.isBoolean(state)) {
				this.form.removeButton.setDisabled(state);
				this.form.setDefaultButton.setDisabled(state);
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
						CMDBuild.core.constants.Proxy.NAME,
						me.form.getForm().findField(CMDBuild.core.constants.Proxy.NAME).getValue()
					);

					me.grid.getSelectionModel().select(rowIndex, true);
					me.form.setDisabledModify(true);

					// Disable buttons based on selected record
					me.setDisabledTopToolbarButtons(this.getAt(rowIndex).get(CMDBuild.core.constants.Proxy.IS_DEFAULT));
				}
			});
		}
	});

})();