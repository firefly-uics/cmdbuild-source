(function() {

	Ext.define('CMDBuild.controller.administration.users.Main', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Users'
		],

		/**
		 * @property {CMDBuild.cache.CMUserForGridModel}
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
		 * @param {CMDBuild.view.administration.users.MainPanel} view
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
				case 'onUserAbortButtonClick':
					return this.onUserAbortButtonClick();

				case 'onUserAddButtonClick':
					return this.onUserAddButtonClick();

				case 'onUserChangePasswordButtonClick':
					return this.onUserChangePasswordButtonClick();

				case 'onUserDisableButtonClick':
					return this.onUserDisableButtonClick();

				case 'onItemDoubleClick':
				case 'onUserModifyButtonClick':
					return this.onUserModifyButtonClick();

				case 'onUserRowSelected':
					return this.onUserRowSelected();

				case 'onUserSaveButtonClick':
					return this.onUserSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
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
				this.form.disableModify();
			}
		},

		onUserAddButtonClick: function() {
			this.selectedUser = null;

			this.form.reset();
			this.form.enableModify(true);
			this.form.defaultGroup.disable();

			this.grid.getSelectionModel().deselectAll();
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

		onUserRowSelected: function() {
			if (this.grid.getSelectionModel().hasSelection()) {
				var store = this.form.defaultGroup.getStore();

				this.selectedUser = this.grid.getSelectionModel().getSelection()[0];

				this.form.reset();
				this.form.disableModify(false);

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
					callback: function() {
						var defaultGroup = store.findRecord('isdefault', true);

						if (defaultGroup)
							this.selectedUser.set('defaultgroup', defaultGroup.getId());

						this.form.getForm().loadRecord(this.selectedUser);
					}
				});
			}
		},

		onUserSaveButtonClick: function() {
			var nonvalid = this.form.getNonValidFields();
			if (nonvalid.length > 0) {
				CMDBuild.Msg.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.invalid_fields,
					false
				);

				return;
			}

			var params = this.form.getData(true);
			params['userid'] = Ext.isEmpty(this.selectedUser) ? -1 : this.selectedUser.get('userid');

			CMDBuild.core.proxy.Users.save({
				params: params,
				scope: this,
				success: this.success
			});
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
					var rowIndex = this.find('userid', decodedResult.rows.userid);

					me.grid.getSelectionModel().select(rowIndex, true);
					me.form.disableModify(false);
				}
			});
		}
	});

})();