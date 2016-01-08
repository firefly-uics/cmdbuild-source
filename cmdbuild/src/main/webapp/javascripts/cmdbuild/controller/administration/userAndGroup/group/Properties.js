(function() {

	Ext.define('CMDBuild.controller.administration.userAndGroup.group.Properties', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.group.Group',
			'CMDBuild.model.userAndGroup.group.Group'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.Group}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupGroupPropertiesAbortButtonClick',
			'onUserAndGroupGroupPropertiesAddButtonClick = onUserAndGroupGroupAddButtonClick',
			'onUserAndGroupGroupPropertiesEnableDisableButtonClick',
			'onUserAndGroupGroupPropertiesGroupSelected = onUserAndGroupGroupSelected',
			'onUserAndGroupGroupPropertiesModifyButtonClick',
			'onUserAndGroupGroupPropertiesSaveButtonClick',
			'onUserAndGroupGroupPropertiesTabShow'
		],

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.group.properties.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.group.properties.PropertiesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.userAndGroup.group.Group} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.group.properties.PropertiesView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		onUserAndGroupGroupPropertiesAbortButtonClick: function() {
			if (this.cmfg('userAndGroupGroupSelectedGroupIsEmpty')) {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			} else {
				this.onUserAndGroupGroupPropertiesTabShow();
			}
		},

		onUserAndGroupGroupPropertiesAddButtonClick: function() {
			this.cmfg('userAndGroupGroupSelectedGroupSet'); // Reset selected group
			this.cmfg('onUserAndGroupGroupSetActiveTab');

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.userAndGroup.group.Group'));
		},

		onUserAndGroupGroupPropertiesEnableDisableButtonClick: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.IS_ACTIVE] = !this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_ACTIVE);

			CMDBuild.core.proxy.userAndGroup.group.Group.enableDisable({
				params: params,
				scope: this,
				success: this.success
			});
		},

		/**
		 * Enable/Disable tab evaluating selected group
		 */
		onUserAndGroupGroupPropertiesGroupSelected: function() {
			this.view.setDisabled(this.cmfg('userAndGroupGroupSelectedGroupIsEmpty'));
		},

		onUserAndGroupGroupPropertiesModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		/**
		 * TODO: waiting for a refactor (new CRUD standards)
		 */
		onUserAndGroupGroupPropertiesSaveButtonClick: function() {
			if (this.validate(this.form)) { // Validate before save
				var params = this.form.getData(true);

				if (Ext.isEmpty(params[CMDBuild.core.constants.Proxy.ID])) {
					params[CMDBuild.core.constants.Proxy.ID] = -1;

					CMDBuild.core.proxy.userAndGroup.group.Group.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.userAndGroup.group.Group.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		/**
		 * TODO: waiting for refactor (crud)
		 */
		onUserAndGroupGroupPropertiesTabShow: function() {
			if (!this.cmfg('userAndGroupGroupSelectedGroupIsEmpty'))
				CMDBuild.core.proxy.userAndGroup.group.Group.read({
					scope: this,
					success: function(result, options, decodedResult) {
						decodedResult = decodedResult[CMDBuild.core.constants.Proxy.GROUPS];

						var selectedGroupModel = Ext.Array.findBy(decodedResult, function(groupObject, i) {
							return this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID) == groupObject[CMDBuild.core.constants.Proxy.ID];
						}, this);

						if (!Ext.isEmpty(selectedGroupModel)) {
							this.cmfg('userAndGroupGroupSelectedGroupSet', selectedGroupModel); // Update selectedGroup data (to delete on refactor)

							var params = {};
							params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

							this.form.startingClassCombo.getStore().load({
								params: params,
								scope: this,
								callback: function(records, operation, success) {
									this.form.loadRecord(this.cmfg('userAndGroupGroupSelectedGroupGet'));
									this.form.enableDisableButton.setActiveState(this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_ACTIVE));
									this.form.setDisabledModify(true, true);
								}
							});
						}
					}
				});
		},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 */
		success: function(result, options, decodedResult) {
			_CMMainViewportController.findAccordionByCMName(CMDBuild.core.constants.ModuleIdentifiers.getUserAndGroup()).updateStore(
				decodedResult[CMDBuild.core.constants.Proxy.GROUP][CMDBuild.core.constants.Proxy.ID]
			);

			this.form.setDisabledModify(true);
		}
	});

})();