(function() {

	Ext.define('CMDBuild.controller.administration.group.privileges.UiConfiguration', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.group.privileges.Classes'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.group.privileges.Grid}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGroupPrivilegesGridUIConfigurationAbortButtonClick',
			'onGroupPrivilegesGridUIConfigurationSaveButtonClick',
			'onGroupPrivilegesGridUIConfigurationShow'
		],

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.model.group.privileges.GridRecord}
		 */
		record: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.group.privileges.UiConfigurationWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.group.privileges.Grid} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.group.privileges.UiConfigurationWindow', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		onGroupPrivilegesGridUIConfigurationAbortButtonClick: function() {
			this.view.hide();
		},

		onGroupPrivilegesGridUIConfigurationSaveButtonClick: function() {
			var params = this.form.getForm().getValues();
			params[CMDBuild.core.proxy.CMProxyConstants.CLASS_ID] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.ID);;
			params[CMDBuild.core.proxy.CMProxyConstants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.ID);

			CMDBuild.core.proxy.group.privileges.Classes.updateUIConfiguration({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					this.form.getForm().setValues(Ext.decode(decodedResponse.response)); // TODO: waiting for refactor

					this.onGroupPrivilegesGridUIConfigurationAbortButtonClick();
				}
			});
		},

		onGroupPrivilegesGridUIConfigurationShow: function() {
			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.CLASS_ID] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.ID);;
			params[CMDBuild.core.proxy.CMProxyConstants.GROUP_ID] = this.cmfg('selectedGroupGet', CMDBuild.core.proxy.CMProxyConstants.ID);

			CMDBuild.core.proxy.group.privileges.Classes.readUIConfiguration({
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					this.form.getForm().setValues(Ext.decode(decodedResponse.response));
				}
			});
		},

		/**
		 * @param {CMDBuild.model.group.privileges.GridRecord} record
		 */
		setRecord: function(record) {
			if(!Ext.isEmpty(record))
				this.record = record;
		}
	});

})();