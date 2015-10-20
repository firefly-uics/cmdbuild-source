(function() {

	Ext.define('CMDBuild.controller.administration.userAndGroup.UserAndGroup', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupAccordionSelect',
			'userAndGroupSelectedAccordionGet',
			'userAndGroupSelectedAccordionIsEmpty'
		],

		/**
		 * @parameter {CMDBuild.model.userAndGroup.SelectedAccordion}
		 *
		 * @private
		 */
		selectedAccordion: undefined,

		/**
		 * @property {Object}
		 */
		sectionController: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.UserAndGroupView}
		 */
		view: undefined,

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} node
		 *
		 * @override
		 */
		onViewOnFront: function(node) {
			if (!Ext.Object.isEmpty(node)) {
				this.userAndGroupSelectedAccordionSet({ value: node.getData() });

				this.view.removeAll(true);

				switch (this.userAndGroupSelectedAccordionGet(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]) {
					case 'user': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.userAndGroup.user.User', { parentDelegate: this });
					} break;

					case 'group':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.userAndGroup.group.Group', { parentDelegate: this });
					}
				}

				var titleParts = [this.sectionController.getBaseTitle()];

				if (!this.userAndGroupSelectedAccordionIsEmpty(CMDBuild.core.constants.Proxy.DESCRIPTION))
					titleParts.push(this.userAndGroupSelectedAccordionGet(CMDBuild.core.constants.Proxy.DESCRIPTION));

				this.setViewTitle(titleParts);

				this.view.add(this.sectionController.getView());

				this.sectionController.cmfg('onUserAndGroupAccordionSelect');

				this.sectionController.getView().fireEvent('show'); // Manual show event fire

				this.callParent(arguments);
			}
		},

		// SelectedAccordion property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @return {Mixed or undefined}
			 */
			userAndGroupSelectedAccordionGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedAccordion';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @return {Mixed or undefined}
			 */
			userAndGroupSelectedAccordionIsEmpty: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedAccordion';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 */
			userAndGroupSelectedAccordionSet: function(parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.userAndGroup.SelectedAccordion';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedAccordion';

					this.propertyManageSet(parameters);
				}
			}
	});

})();