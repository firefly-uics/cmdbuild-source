(function() {

	Ext.define('CMDBuild.controller.administration.userAndGroup.user.User', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.user.User'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.user.UserAndGroup}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupUserAbortButtonClick',
			'onUserAndGroupUserAccordionSelect = onUserAndGroupAccordionSelect',
			'onUserAndGroupUserAddButtonClick',
			'onUserAndGroupUserChangePasswordButtonClick',
			'onUserAndGroupUserDisableButtonClick',
			'onUserAndGroupUserModifyButtonClick = onUserAndGroupUserItemDoubleClick',
			'onUserAndGroupUserPrivilegedChange',
			'onUserAndGroupUserRowSelected',
			'onUserAndGroupUserSaveButtonClick',
			'onUserAndGroupUserServiceChange',
			'onUserAndGroupUserShow'
		],

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.user.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.user.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.userAndGroup.user.User}
		 *
		 * @private
		 */
		selectedUser: undefined,

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.user.UserView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.userAndGroup.user.UserAndGroup} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.user.UserView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		/**
		 * @param {Ext.form.FieldSet} fieldset
		 *
		 * @private
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

		onUserAndGroupUserAbortButtonClick: function() {
			if (!this.userAndGroupUserSelectedUserIsEmpty()) {
				this.onUserAndGroupUserRowSelected();
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		/**
		 * Empty function to avoid warning
		 */
		onUserAndGroupUserAccordionSelect: Ext.emptyFn,

		onUserAndGroupUserAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.userAndGroupUserSelectedUserReset();

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.defaultGroupCombo.setDisabled(true);
			this.form.loadRecord(Ext.create('CMDBuild.model.userAndGroup.user.User'));
		},

		onUserAndGroupUserChangePasswordButtonClick: function() {
			this.enableFieldset(this.form.userPasswordFieldSet);
		},

		onUserAndGroupUserDisableButtonClick: function() {
			var params = {};
			params['userid'] = this.userAndGroupUserSelectedUserGet('userid');
			params[CMDBuild.core.constants.Proxy.DISABLE] = this.userAndGroupUserSelectedUserGet(CMDBuild.core.constants.Proxy.IS_ACTIVE);

			CMDBuild.core.proxy.userAndGroup.user.User.disable({
				params: params,
				scope: this,
				success: this.success
			});
		},

		onUserAndGroupUserModifyButtonClick: function() {
			this.enableFieldset(this.form.userInfoFieldSet);
		},

		/**
		 * Privileged is a specialization of service, so if someone check privileged is implicit that is a service user
		 */
		onUserAndGroupUserPrivilegedChange: function() {
			if (this.form.privilegedCheckbox.getValue())
				this.form.serviceCheckbox.setValue(true);
		},

		onUserAndGroupUserRowSelected: function() {
			if (this.grid.getSelectionModel().hasSelection()) {
				this.userAndGroupUserSelectedUserSet({ value: this.grid.getSelectionModel().getSelection()[0] });

				this.form.reset();
				this.form.setDisabledModify(true, true);

				// Update toggleEnableDisableButton button
				this.form.toggleEnableDisableButton.setActiveState(this.userAndGroupUserSelectedUserGet(CMDBuild.core.constants.Proxy.IS_ACTIVE));

				var params = {};
				params['userid'] = this.userAndGroupUserSelectedUserGet('userid');

				this.form.defaultGroupCombo.getStore().load({
					params: params,
					scope: this,
					callback: function(records, operation, success) {
						var defaultGroup = this.form.defaultGroupCombo.getStore().findRecord('isdefault', true);

						if (!Ext.isEmpty(defaultGroup))
							this.userAndGroupUserSelectedUserSet({
								propertyName: 'defaultgroup',
								value: defaultGroup.get(CMDBuild.core.constants.Proxy.ID)
							});

						this.form.getForm().loadRecord(this.userAndGroupUserSelectedUserGet());
					}
				});
			}
		},

		/**
		 * TODO: waiting for a refactor (new CRUD standards)
		 */
		onUserAndGroupUserSaveButtonClick: function() {
			if (this.validate(this.form)) { // Validate before save
				var params = this.form.getData(true);

				if (Ext.isEmpty(params['userid'])) {
					params['userid'] = -1;

					CMDBuild.core.proxy.userAndGroup.user.User.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.userAndGroup.user.User.update({
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
		onUserAndGroupUserServiceChange: function() {
			if (!this.form.serviceCheckbox.getValue())
				this.form.privilegedCheckbox.setValue(false);
		},

		onUserAndGroupUserShow: function() { // TODO: implementation of activeOnly/all user display
			var params = {};
			params['includeUnactive'] = this.view.includeUnactiveUsers.getValue();

			this.grid.getStore().load({
				params: params,
				scope: this,
				callback: function(records, operation, success) {
					if (!this.grid.getSelectionModel().hasSelection())
						this.grid.getSelectionModel().select(0, true);
				}
			});
		},

		// SelectedUser property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @return {Mixed or undefined}
			 */
			userAndGroupUserSelectedUserGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedUser';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @return {Mixed or undefined}
			 */
			userAndGroupUserSelectedUserIsEmpty: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedUser';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			userAndGroupUserSelectedUserReset: function() {
				this.propertyManageReset('selectedUser');
			},

			/**
			 * @param {Object} parameters
			 */
			userAndGroupUserSelectedUserSet: function(parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.userAndGroup.user.User';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedUser';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 *
		 * @private
		 */
		success: function(result, options, decodedResult) {
			var me = this;

			this.grid.getStore().load({
				callback: function(records, operation, success) {
					var rowIndex = this.find('userid', decodedResult.rows.userid);

					me.grid.getSelectionModel().select(rowIndex, true);
					me.form.setDisabledModify(true);
				}
			});
		}
	});

})();
