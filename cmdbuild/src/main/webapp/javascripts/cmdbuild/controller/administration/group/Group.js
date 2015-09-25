(function() {

	Ext.define('CMDBuild.controller.administration.group.Group', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.group.Group'
		],

		/**
		 * @property {CMDBuild.controller.administration.group.DefaultFilters}
		 */
		controllerDefaultFilters: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.privileges.Privileges}
		 */
		controllerPrivileges: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.Properties}
		 */
		controllerProperties: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.UserInterface}
		 */
		controllerUserInterface: undefined,

		/**
		 * @property {CMDBuild.controller.administration.group.Users}
		 */
		controllerUsers: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'controllerPropertyGet',
			'groupSelectedGroupGet',
			'groupSelectedGroupIsEmpty',
			'groupSelectedGroupSet',
			'onGroupAddButtonClick -> controllerProperties, controllerPrivileges, controllerUsers, controllerUserInterface, controllerDefaultFilters',
			'onGroupGroupSelected -> controllerProperties, controllerPrivileges, controllerUsers, controllerUserInterface, controllerDefaultFilters',
			'onGroupSetActiveTab'
		],

		/**
		 * @property {CMDBuild.model.lookup.Type} or null
		 *
		 * @private
		 */
		selectedGroup: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.group.GroupView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.group.GroupView} view
		 */
		constructor: function(view) {
			this.callParent(arguments);

			this.view.tabPanel.removeAll();

			// Controller build
			this.controllerDefaultFilters = Ext.create('CMDBuild.controller.administration.group.DefaultFilters', { parentDelegate: this });
			this.controllerPrivileges = Ext.create('CMDBuild.controller.administration.group.privileges.Privileges', { parentDelegate: this });
			this.controllerProperties = Ext.create('CMDBuild.controller.administration.group.Properties', { parentDelegate: this });
			this.controllerUserInterface = Ext.create('CMDBuild.controller.administration.group.UserInterface', { parentDelegate: this });
			this.controllerUsers = Ext.create('CMDBuild.controller.administration.group.Users', { parentDelegate: this });

			// Inject tabs (sorted)
			this.view.tabPanel.add(this.controllerProperties.getView());
			this.view.tabPanel.add(this.controllerPrivileges.getView());
			this.view.tabPanel.add(this.controllerUsers.getView());
			this.view.tabPanel.add(this.controllerUserInterface.getView());
			this.view.tabPanel.add(this.controllerDefaultFilters.getView());

			this.onGroupSetActiveTab();
		},

		// SelectedGroup property methods
			/**
			 * Returns full model object or just one property if required
			 *
			 * @param {String} parameterName
			 *
			 * @returns {CMDBuild.model.group.Group} or Mixed
			 */
			groupSelectedGroupGet: function(parameterName) {
				if (!Ext.isEmpty(parameterName))
					return this.selectedGroup.get(parameterName);

				return this.selectedGroup;
			},

			/**
			 * @returns {Boolean}
			 */
			groupSelectedGroupIsEmpty: function() {
				return Ext.isEmpty(this.selectedGroup);
			},

			/**
			 * @property {Object} selectedGroupObject
			 */
			groupSelectedGroupSet: function(selectedGroupObject) {
				this.selectedGroup = null;

				if (!Ext.isEmpty(selectedGroupObject) && Ext.isObject(selectedGroupObject)) {
					if (Ext.getClassName(selectedGroupObject) == 'CMDBuild.model.group.Group') {
						this.selectedGroup = selectedGroupObject;
					} else {
						this.selectedGroup = Ext.create('CMDBuild.model.group.Group', selectedGroupObject);
					}
				}
			},

		/**
		 * @param {Number} index
		 */
		onGroupSetActiveTab: function(index) {
			this.view.tabPanel.setActiveTab(index || 0);
		},

		/**
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} parameters
		 */
		onViewOnFront: function(parameters) {
			if (!Ext.isEmpty(parameters))
				CMDBuild.core.proxy.group.Group.read({ // TODO: waiting for refactor (crud)
					scope: this,
					success: function(result, options, decodedResult) {
						decodedResult = decodedResult[CMDBuild.core.constants.Proxy.GROUPS];

						var selectedGroupModel = Ext.Array.findBy(decodedResult, function(groupObject, i) {
							return parameters.get(CMDBuild.core.constants.Proxy.ID) == groupObject[CMDBuild.core.constants.Proxy.ID];
						}, this);

						if (!Ext.isEmpty(selectedGroupModel)) {
							this.groupSelectedGroupSet(selectedGroupModel);

							this.cmfg('onGroupGroupSelected');

							this.setViewTitle(parameters.get(CMDBuild.core.constants.Proxy.TEXT));

							if (Ext.isEmpty(this.view.tabPanel.getActiveTab()))
								this.onGroupSetActiveTab();

							this.view.tabPanel.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
						}
					}
				});
		}
	});

})();