(function() {

	Ext.define('CMDBuild.controller.management.customPage.SinglePage', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.configurations.CustomPages'
		],

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @property {Object}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.model.common.accordion.Generic} node
		 */
		onViewOnFront: function(node) {
			if (!Ext.isEmpty(node)) {
				var basePath = window.location.toString().split('/');
				basePath = Ext.Array.slice(basePath, 0, basePath.length - 1).join('/');

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.DESCRIPTION));

				this.view.removeAll();
				this.view.add({
					xtype: 'component',

					autoEl: {
						tag: 'iframe',
						src: CMDBuild.core.configurations.CustomPages.getCustomizationsPath()
							+ node.get(CMDBuild.core.constants.Proxy.NAME)
							+ '/?basePath=' + basePath
							+ '&frameworkVersion=' + CMDBuild.core.configurations.CustomPages.getVersion()
					}
				});

				// History: custompage selected save
				CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
					moduleId: this.cmName,
					entryType: {
						description: node.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
						id: node.get(CMDBuild.core.constants.Proxy.ID),
						object: node
					}
				});

				this.callParent(arguments);
			}
		}

	});

})();