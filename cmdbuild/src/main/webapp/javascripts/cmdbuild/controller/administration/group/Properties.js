(function() {

	Ext.define('CMDBuild.controller.administration.group.Properties', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
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
			if (this.cmfg('selectedGroupIsEmpty')) {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			} else {
				this.onGroupPropertiesTabShow();
			}
		},

		onGroupPropertiesAddButtonClick: function() {
			this.cmfg('selectedGroupSet'); // Reset selected group
			this.cmfg('onGroupSetActiveTab');

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.group.Group'));
		},

		onGroupPropertiesEnableDisableButtonClick: function() {
			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.ID);
			params[CMDBuild.core.proxy.CMProxyConstants.IS_ACTIVE] = !this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.IS_ACTIVE);

			CMDBuild.core.proxy.group.Group.enableDisable({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					_CMCache.onGroupSaved(decodedResponse.group); // TODO: try to avoid to use cache
				}
			});
		},

		/**
		 * Enable/Disable tab evaluating selected group
		 */
		onGroupPropertiesGroupSelected: function() {
			this.view.setDisabled(this.cmfg('selectedGroupIsEmpty'));
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
				params[CMDBuild.core.proxy.CMProxyConstants.ID] = params[CMDBuild.core.proxy.CMProxyConstants.ID] || -1;

				if (Ext.isEmpty(params[CMDBuild.core.proxy.CMProxyConstants.ID])) {
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
		 * TODO: should be refactored to require group details only on tab show
		 */
		onGroupPropertiesTabShow: function() {
			if (!this.cmfg('selectedGroupIsEmpty')) {
				this.form.loadRecord(this.cmfg('selectedGroupGet'));
				this.form.enableDisableButton.setActiveState(this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.IS_ACTIVE));
				this.form.setDisabledModify(true, true);
			}
		},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 *
		 * TODO: server side refactor needed to follow new CMDBuild standards
		 */
		success: function(result, options, decodedResult) {
			_CMCache.onGroupSaved(decodedResult.group);

			this.form.setDisabledModify(true);
		}
	});

})();