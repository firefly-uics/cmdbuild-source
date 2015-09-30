(function() {

	Ext.define('CMDBuild.controller.administration.userAndGroup.group.privileges.UiConfiguration', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.group.privileges.Classes'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.privileges.Grid}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupGroupPrivilegesGridUIConfigurationAbortButtonClick',
			'onUserAndGroupGroupPrivilegesGridUIConfigurationSaveButtonClick',
			'onUserAndGroupGroupPrivilegesGridUIConfigurationShow'
		],

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.model.userAndGroup.group.privileges.GridRecord}
		 */
		record: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.group.privileges.UiConfigurationWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.userAndGroup.group.privileges.Grid} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.group.privileges.UiConfigurationWindow', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		onUserAndGroupGroupPrivilegesGridUIConfigurationAbortButtonClick: function() {
			this.view.hide();
		},

		onUserAndGroupGroupPrivilegesGridUIConfigurationSaveButtonClick: function() {
			var params = this.form.getForm().getValues();
			params[CMDBuild.core.constants.Proxy.CLASS_ID] = this.record.get(CMDBuild.core.constants.Proxy.ID);;
			params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

			CMDBuild.core.proxy.userAndGroup.group.privileges.Classes.updateUIConfiguration({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					this.form.getForm().setValues(Ext.decode(decodedResponse.response)); // TODO: waiting for refactor

					this.onUserAndGroupGroupPrivilegesGridUIConfigurationAbortButtonClick();
				}
			});
		},

		onUserAndGroupGroupPrivilegesGridUIConfigurationShow: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_ID] = this.record.get(CMDBuild.core.constants.Proxy.ID);;
			params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

			CMDBuild.core.proxy.userAndGroup.group.privileges.Classes.readUIConfiguration({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					this.form.getForm().setValues(Ext.decode(decodedResponse.response));
				}
			});
		},

		/**
		 * @param {CMDBuild.model.userAndGroup.group.privileges.GridRecord} record
		 */
		setRecord: function(record) {
			if(!Ext.isEmpty(record))
				this.record = record;
		}
	});

})();