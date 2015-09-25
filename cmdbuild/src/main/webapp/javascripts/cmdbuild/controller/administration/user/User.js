(function() {

	Ext.define('CMDBuild.controller.administration.user.User', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.User'
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
		 * @property {CMDBuild.view.administration.user.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.user.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.administration.user.UserView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.user.UserView} view
		 */
		constructor: function(view) {
			this.callParent(arguments);

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;

			// Handlers exchange
			this.form.delegate = this;
			this.grid.delegate = this;
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
			this.form.loadRecord(Ext.create('CMDBuild.model.user.User'));
		},

		onUserChangePasswordButtonClick: function() {
			this.enableFieldset(this.form.userPassword);
		},

		onUserDisableButtonClick: function() {
			var params = {};
			params['userid'] = this.selectedUser.get('userid');
			params[CMDBuild.core.constants.Proxy.DISABLE] = this.selectedUser.get(CMDBuild.core.constants.Proxy.IS_ACTIVE);

			CMDBuild.core.proxy.User.disable({
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
				if (this.selectedUser.get(CMDBuild.core.constants.Proxy.IS_ACTIVE)) {
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

		/**
		 * TODO: waiting for a refactor (new CRUD standards)
		 */
		onUserSaveButtonClick: function() {
			if (this.validate(this.form)) { // Validate before save
				var params = this.form.getData(true);

				if (Ext.isEmpty(params['userid'])) { // TODO: rename + translation
					params['userid'] = -1; // TODO: rename + translation

					CMDBuild.core.proxy.User.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.User.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
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