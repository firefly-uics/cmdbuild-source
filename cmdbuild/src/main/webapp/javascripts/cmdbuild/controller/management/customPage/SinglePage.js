(function() {

	Ext.define('CMDBuild.controller.management.customPage.SinglePage', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.configurations.CustomPage'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onCustomPageModuleInit = onModuleInit'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.management.customPage.SinglePagePanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.customPage.SinglePagePanel', { delegate: this });
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {Object} parameters
		 * @param {CMDBuild.model.common.Accordion} parameters.node
		 *
		 * @override
		 */
		onCustomPageModuleInit: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			if (Ext.isObject(parameters.node) && !Ext.isEmpty(parameters.node)) {
				var basePath = window.location.toString().split('/');
				basePath = Ext.Array.slice(basePath, 0, basePath.length - 1).join('/');

				this.setViewTitle(parameters.node.get(CMDBuild.core.constants.Proxy.DESCRIPTION));

				this.view.removeAll();
				this.view.add({
					xtype: 'component',

					autoEl: {
						tag: 'iframe',
						src: CMDBuild.core.configurations.CustomPage.getCustomizationsPath()
							+ parameters.node.get(CMDBuild.core.constants.Proxy.NAME)
							+ '/?basePath=' + basePath
							+ '&frameworkVersion=' + CMDBuild.core.configurations.CustomPage.getVersion()
							+ '&language=' + CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.LANGUAGE)
					}
				});

				// History record save
				if (!Ext.isEmpty(parameters.node))
					CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
						moduleId: this.cmfg('identifierGet'),
						entryType: {
							description: parameters.node.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
							id: parameters.node.get(CMDBuild.core.constants.Proxy.ID),
							object: parameters.node
						}
					});

				this.onModuleInit(parameters); // Custom callParent() implementation
			}
		}
	});

})();
