(function () {

	Ext.define('CMDBuild.controller.administration.configuration.Dms', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.configuration.Dms',
			'CMDBuild.model.configuration.dms.Form'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationDmsFieldSetExpand',
			'onConfigurationDmsSaveButtonClick',
			'onConfigurationDmsTabShow = onConfigurationDmsAbortButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.DmsPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.configuration.Configuration} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.configuration.DmsPanel', { delegate: this });
		},

		/**
		 * @param {String} expandedFieldSetIdentifier
		 */
		onConfigurationDmsFieldSetExpand: function (expandedFieldSetIdentifier) {
			if (!Ext.isEmpty(expandedFieldSetIdentifier) && Ext.isString(expandedFieldSetIdentifier))
				switch (expandedFieldSetIdentifier) {
					case CMDBuild.core.constants.Proxy.ALFRESCO:
						return this.view.fieldSetCmis.collapse();

					case CMDBuild.core.constants.Proxy.CMIS:
						return this.view.fieldSetAlfresco.collapse();
				}
		},

		onConfigurationDmsSaveButtonClick: function () {
			CMDBuild.core.proxy.configuration.Dms.update({
				params: CMDBuild.model.configuration.dms.Form.convertToLegacy(this.view.getData()),
				scope: this,
				success: function (response, options, decodedResponse) {
					this.cmfg('onConfigurationDmsTabShow');

					CMDBuild.core.Message.success();
				}
			});
		},

		onConfigurationDmsTabShow: function () {
			CMDBuild.core.proxy.configuration.Dms.read({
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					if (!Ext.isEmpty(decodedResponse)) {
						this.view.reset();
						this.view.loadRecord(Ext.create('CMDBuild.model.configuration.dms.Form', CMDBuild.model.configuration.dms.Form.convertFromLegacy(decodedResponse)));
					}
				}
			});
		}
	});

})();
