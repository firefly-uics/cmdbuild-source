(function() {

	Ext.define('CMDBuild.controller.administration.group.Properties', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.group.Group',
			'CMDBuild.model.group.Group'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.group.Group}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGroupPropertiesAbortButtonClick',
			'onGroupPropertiesAddButtonClick = onGroupAddButtonClick',
			'onGroupPropertiesEnableDisableButtonClick',
			'onGroupPropertiesGroupSelected = onGroupGroupSelected',
			'onGroupPropertiesModifyButtonClick',
			'onGroupPropertiesSaveButtonClick',
			'onGroupPropertiesTabShow'
		],

		/**
		 * @property {CMDBuild.view.administration.group.properties.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.group.properties.PropertiesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.group.Group} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.group.properties.PropertiesView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		onGroupPropertiesAbortButtonClick: function() {
			if (this.cmfg('groupSelectedGroupIsEmpty')) {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			} else {
				this.onGroupPropertiesTabShow();
			}
		},

		onGroupPropertiesAddButtonClick: function() {
			this.cmfg('groupSelectedGroupSet'); // Reset selected group
			this.cmfg('onGroupSetActiveTab');

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.group.Group'));
		},

		onGroupPropertiesEnableDisableButtonClick: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.IS_ACTIVE] = !this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_ACTIVE);

			CMDBuild.core.proxy.group.Group.enableDisable({
				params: params,
				scope: this,
				success: this.success
			});
		},

		/**
		 * Enable/Disable tab evaluating selected group
		 */
		onGroupPropertiesGroupSelected: function() {
			this.view.setDisabled(this.cmfg('groupSelectedGroupIsEmpty'));
		},

		onGroupPropertiesModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		/**
		 * TODO: waiting for a refactor (new CRUD standards)
		 */
		onGroupPropertiesSaveButtonClick: function() {
			if (this.validate(this.form)) { // Validate before save
				var params = this.form.getData(true);

				if (Ext.isEmpty(params[CMDBuild.core.constants.Proxy.ID])) {
					params[CMDBuild.core.constants.Proxy.ID] = -1;

					CMDBuild.core.proxy.group.Group.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.group.Group.update({
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
		onGroupPropertiesTabShow: function() {
			if (!this.cmfg('groupSelectedGroupIsEmpty'))
				CMDBuild.core.proxy.group.Group.read({
					scope: this,
					success: function(result, options, decodedResult) {
						decodedResult = decodedResult[CMDBuild.core.constants.Proxy.GROUPS];

						var selectedGroupModel = Ext.Array.findBy(decodedResult, function(groupObject, i) {
							return this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID) == groupObject[CMDBuild.core.constants.Proxy.ID];
						}, this);

						if (!Ext.isEmpty(selectedGroupModel)) {
							this.cmfg('groupSelectedGroupSet',selectedGroupModel); // Update selectedGroup data (to delete on refactor)

							this.form.loadRecord(this.cmfg('groupSelectedGroupGet'));
							this.form.enableDisableButton.setActiveState(this.cmfg('groupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_ACTIVE));
							this.form.setDisabledModify(true, true);
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
			_CMMainViewportController.findAccordionByCMName('group').deselect();
			_CMMainViewportController.findAccordionByCMName('group').updateStore(decodedResult[CMDBuild.core.constants.Proxy.GROUP][CMDBuild.core.constants.Proxy.ID]);

			this.form.setDisabledModify(true);
		}
	});

})();