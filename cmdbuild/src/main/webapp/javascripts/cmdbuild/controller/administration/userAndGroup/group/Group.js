(function() {

	Ext.define('CMDBuild.controller.administration.userAndGroup.group.Group', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.userAndGroup.group.Group'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.user.UserAndGroup}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.DefaultFilters}
		 */
		controllerDefaultFilters: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.privileges.Privileges}
		 */
		controllerPrivileges: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.Properties}
		 */
		controllerProperties: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.UserInterface}
		 */
		controllerUserInterface: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.Users}
		 */
		controllerUsers: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupGroupAccordionSelect = onUserAndGroupAccordionSelect',
			'onUserAndGroupGroupAddButtonClick -> controllerProperties, controllerPrivileges, controllerUsers, controllerUserInterface, controllerDefaultFilters',
			'onUserAndGroupGroupSelected -> controllerProperties, controllerPrivileges, controllerUsers, controllerUserInterface, controllerDefaultFilters',
			'onUserAndGroupGroupSetActiveTab',
			'userAndGroupGroupSelectedGroupGet',
			'userAndGroupGroupSelectedGroupIsEmpty',
			'userAndGroupGroupSelectedGroupSet'
		],

		/**
		 * @property {CMDBuild.model.lookup.Type} or null
		 *
		 * @private
		 */
		selectedGroup: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.group.GroupView}
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

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.group.GroupView', { delegate: this });

			this.view.tabPanel.removeAll();

			// Controller build
			this.controllerDefaultFilters = Ext.create('CMDBuild.controller.administration.userAndGroup.group.DefaultFilters', { parentDelegate: this });
			this.controllerPrivileges = Ext.create('CMDBuild.controller.administration.userAndGroup.group.privileges.Privileges', { parentDelegate: this });
			this.controllerProperties = Ext.create('CMDBuild.controller.administration.userAndGroup.group.Properties', { parentDelegate: this });
			this.controllerUserInterface = Ext.create('CMDBuild.controller.administration.userAndGroup.group.UserInterface', { parentDelegate: this });
			this.controllerUsers = Ext.create('CMDBuild.controller.administration.userAndGroup.group.Users', { parentDelegate: this });

			// Inject tabs (sorted)
			this.view.tabPanel.add(this.controllerProperties.getView());
			this.view.tabPanel.add(this.controllerPrivileges.getView());
			this.view.tabPanel.add(this.controllerUsers.getView());
			this.view.tabPanel.add(this.controllerUserInterface.getView());
			this.view.tabPanel.add(this.controllerDefaultFilters.getView());

			this.onUserAndGroupGroupSetActiveTab();
		},

		onUserAndGroupGroupAccordionSelect: function() {
			if (!this.cmfg('userAndGroupSelectedAccordionIsEmpty'))
				CMDBuild.core.proxy.userAndGroup.group.Group.read({ // TODO: waiting for refactor (crud)
					scope: this,
					success: function(result, options, decodedResult) {
						decodedResult = decodedResult[CMDBuild.core.constants.Proxy.GROUPS];

						var selectedGroupModel = Ext.Array.findBy(decodedResult, function(groupObject, i) {
							return this.cmfg('userAndGroupSelectedAccordionGet', CMDBuild.core.constants.Proxy.ID) == groupObject[CMDBuild.core.constants.Proxy.ID];
						}, this);

						if (!Ext.isEmpty(selectedGroupModel)) {
							this.userAndGroupGroupSelectedGroupSet(selectedGroupModel);

							this.cmfg('onUserAndGroupGroupSelected');

							if (Ext.isEmpty(this.view.tabPanel.getActiveTab()))
								this.onUserAndGroupGroupSetActiveTab();

							this.view.tabPanel.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
						}
					}
				});
		},

		/**
		 * @param {Number} index
		 */
		onUserAndGroupGroupSetActiveTab: function(index) {
			this.view.tabPanel.setActiveTab(index || 0);
		},

		// SelectedGroup property methods
			/**
			 * Returns full model object or just one property if required
			 *
			 * @param {String} parameterName
			 *
			 * @returns {CMDBuild.model.userAndGroup.group.Group} or Mixed
			 */
			userAndGroupGroupSelectedGroupGet: function(parameterName) {
				if (!Ext.isEmpty(parameterName))
					return this.selectedGroup.get(parameterName);

				return this.selectedGroup;
			},

			/**
			 * @returns {Boolean}
			 */
			userAndGroupGroupSelectedGroupIsEmpty: function() {
				return Ext.isEmpty(this.selectedGroup);
			},

			/**
			 * @property {Object} selectedGroupObject
			 */
			userAndGroupGroupSelectedGroupSet: function(selectedGroupObject) {
				this.selectedGroup = null;

				if (!Ext.isEmpty(selectedGroupObject) && Ext.isObject(selectedGroupObject)) {
					if (Ext.getClassName(selectedGroupObject) == 'CMDBuild.model.userAndGroup.group.Group') {
						this.selectedGroup = selectedGroupObject;
					} else {
						this.selectedGroup = Ext.create('CMDBuild.model.userAndGroup.group.Group', selectedGroupObject);
					}
				}
			}
	});

})();