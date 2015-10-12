(function() {

	Ext.define('CMDBuild.controller.administration.configuration.Configuration', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Configuration'
		],

		/**
		 * @cfg {Object}
		 */
		delegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationRead',
			'onConfigurationSave'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.ConfigurationView}
		 */
		view: undefined,

		/**
		 * @param {Object} parameters
		 * @param {String} parameters.fileName
		 * @param {Mixed} parameters.view
		 */
		onConfigurationRead: function(parameters) {
			if (
				!Ext.isEmpty(parameters)
				&& !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.FILE_NAME])
				&& !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.VIEW])
			) {
				var fileName = parameters[CMDBuild.core.constants.Proxy.FILE_NAME];
				var view = parameters[CMDBuild.core.constants.Proxy.VIEW];

				CMDBuild.core.proxy.Configuration.read({
					scope: this,
					loadMask: true,
					success: function(result, options, decodedResult){
						var decodedResult = decodedResult.data;

						// FIX bug with Firefox that breaks UI on fast configuration page switch
						if (view.isVisible())
							view.getForm().setValues(decodedResult);

						if (Ext.isFunction(view.afterSubmit))
							view.afterSubmit(decodedResult);
					}
				}, fileName);
			}
		},

		/**
		 * @param {Object} parameters
		 * @param {String} parameters.fileName
		 * @param {Mixed} parameters.view
		 */
		onConfigurationSave: function(parameters) {
			if (
				!Ext.isEmpty(parameters)
				&& !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.FILE_NAME])
				&& !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.VIEW])
			) {
				var fileName = parameters[CMDBuild.core.constants.Proxy.FILE_NAME];
				var view = parameters[CMDBuild.core.constants.Proxy.VIEW];

				CMDBuild.core.proxy.Configuration.read({
					scope: this,
					loadMask: true,
					success: function(result, options, decodedResult){
						var decodedResult = decodedResult.data;

						Ext.apply(decodedResult, view.getValues());

						CMDBuild.core.proxy.Configuration.save({
							scope: this,
							params: decodedResult,
							loadMask: true,
							success: function(result, options, decodedResult) {
								this.onConfigurationRead(fileName, view);

								CMDBuild.view.common.field.translatable.Utils.commit(view);

								CMDBuild.core.Message.success();
							}
						}, fileName);
					}
				}, fileName);
			}
		},

		/**
		 * Setup view items on accordion click
		 *
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} parameters
		 *
		 * @override
		 */
		onViewOnFront: function(parameters) {
			if (!Ext.Object.isEmpty(parameters)) {
				this.view.removeAll(true);

				switch(parameters.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]) {
					case 'alfresco': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Alfresco', { parentDelegate: this });
					} break;

					case 'bim': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Bim', { parentDelegate: this });
					} break;

					case 'gis': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Gis', { parentDelegate: this });
					} break;

					case 'relationGraph': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.RelationGraph', { parentDelegate: this });
					} break;

					case 'server': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Server', { parentDelegate: this });
					} break;

					case 'workflow': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.Workflow', { parentDelegate: this });
					} break;

					case 'generalOptions':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.configuration.GeneralOptions', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.setViewTitle(parameters.get(CMDBuild.core.constants.Proxy.TEXT));

				this.callParent(arguments);
			}
		}
	});

})();