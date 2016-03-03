(function() {

	Ext.define('CMDBuild.controller.administration.users.Users', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Users',
			'CMDBuild.model.Users'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAbortButtonClick',
			'onUserAddButtonClick',
			'onUserChangePasswordButtonClick',
			'onUserDisableButtonClick',
			'onUserModifyButtonClick = onUserItemDoubleClick',
			'onUserPrivilegedChange',
			'onUserRowSelected',
			'onUserSaveButtonClick',
			'onUserServiceChange'
		],

		/**
		 * @property {CMDBuild.model.Users.single}
		 */
		selectedUser: undefined,

		/**
		 * @property {CMDBuild.view.administration.users.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.users.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.administration.users.UsersView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.users.UsersView} view
		 */
		constructor: function(view) {
			this.callParent(arguments);

			this.grid = Ext.create('CMDBuild.view.administration.users.GridPanel', {
				delegate: this,
				region: 'north',
				split: true,
				height: '30%'
			});

			this.form = Ext.create('CMDBuild.view.administration.users.FormPanel', {
				delegate: this,
				region: 'center'
			});

			this.view.add(this.grid, this.form);
		},

		/**
		 * @param {Ext.form.FieldSet} fieldset
		 */
		enableFieldset: function(fieldset) {
			fieldset.cascade(function (item) {
				if (
					item
					&& (
						item instanceof Ext.form.Field
						|| item instanceof Ext.form.FieldSet
						|| item.considerAsFieldToDisable
					)
					&& !item.cmImmutable
					&& item.isVisible()
				) {
					item.enable();
				}
			});

			this.form.setDisabledTopBar(true);
			this.form.setDisabledBottomBar(false);
		},

		onUserAbortButtonClick: function() {
			if (!Ext.isEmpty(this.selectedUser)) {
				this.onUserRowSelected();
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		onUserAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.selectedUser = null;

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.defaultGroup.setDisabled(true);
			this.form.loadRecord(Ext.create('CMDBuild.model.Users.single'));
		},

		onUserChangePasswordButtonClick: function() {
			this.enableFieldset(this.form.userPassword);
		},

		onUserDisableButtonClick: function() {
			var params = {};
			params['userid'] = this.selectedUser.get('userid');
			params[CMDBuild.core.proxy.CMProxyConstants.DISABLE] = this.selectedUser.get(CMDBuild.core.proxy.CMProxyConstants.IS_ACTIVE);

			CMDBuild.core.proxy.Users.disable({
				params: params,
				scope: this,
				success: this.success
			});
		},

		onUserModifyButtonClick: function() {
			this.enableFieldset(this.form.userInfo);
		},

		/**
		 * Privileged is a specialization of service, so if someone check privileged is implicit that is a service user
		 */
		onUserPrivilegedChange: function() {
			if (this.form.privilegedCheckbox.getValue())
				this.form.serviceCheckbox.setValue(true);
		},

		onUserRowSelected: function() {
			if (this.grid.getSelectionModel().hasSelection()) {
				var store = this.form.defaultGroup.getStore();

				this.selectedUser = this.grid.getSelectionModel().getSelection()[0];

				this.form.reset();
				this.form.setDisabledModify(true, true);

				// Update disableUser button
				if (this.selectedUser.get(CMDBuild.core.proxy.CMProxyConstants.IS_ACTIVE)) {
					this.form.disableUser.setText(CMDBuild.Translation.disableUser);
					this.form.disableUser.setIconCls('delete');
				} else {
					this.form.disableUser.setText(CMDBuild.Translation.enableUser);
					this.form.disableUser.setIconCls('ok');
				}

				store.load({
					scope: this,
					params: {
						userid: this.selectedUser.get('userid')
					},
					callback: function(records, operation, success) {
						// Store load errors manage
						if (!success) {
							CMDBuild.core.Message.error(null, {
								text: CMDBuild.Translation.errors.unknown_error,
								detail: operation.error
							});
						}

						var defaultGroup = store.findRecord('isdefault', true);

						if (defaultGroup)
							this.selectedUser.set('defaultgroup', defaultGroup.getId());

						this.form.getForm().loadRecord(this.selectedUser);
					}
				});
			}
		},

		onUserSaveButtonClick: function() {
			// Validate before save
			if (this.validate(this.form)) {
				var params = this.form.getData(true);
				params['userid'] = Ext.isEmpty(this.selectedUser) ? -1 : this.selectedUser.get('userid');

				CMDBuild.core.proxy.Users.save({
					params: params,
					scope: this,
					success: this.success
				});
			}
		},

		/**
		 * Privileged is a specialization of service, so if someone uncheck service is implicit that is not a privileged user
		 */
		onUserServiceChange: function() {
			if (!this.form.serviceCheckbox.getValue())
				this.form.privilegedCheckbox.setValue(false);
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
					// Store load errors manage
					if (!success) {
						CMDBuild.core.Message.error(null, {
							text: CMDBuild.Translation.errors.unknown_error,
							detail: operation.error
						});
					}

					var rowIndex = this.find('userid', decodedResult.rows.userid);

					me.grid.getSelectionModel().select(rowIndex, true);
					me.form.setDisabledModify(true);
				}
			});
		}
	});

})();