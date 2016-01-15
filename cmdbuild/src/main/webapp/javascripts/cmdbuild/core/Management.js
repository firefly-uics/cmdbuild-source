(function() {

	/**
	 * Call sequence: init() -> buildConfiguration() -> buildCache() -> buildUserInterface()
	 */
	Ext.define('CMDBuild.core.Management', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.configuration.Configuration',
			'CMDBuild.core.proxy.dataView.DataView',
			'CMDBuild.core.proxy.domain.Domain',
			'CMDBuild.core.proxy.lookup.Type',
			'CMDBuild.core.proxy.Menu',
			'CMDBuild.core.proxy.report.Report',
			'CMDBuild.core.proxy.userAndGroup.group.Group',
			'CMDBuild.core.proxy.widget.Widget',
			'CMDBuild.core.RequestBarrier',
			'CMDBuild.core.Splash'
		],

		singleton: true,

		/**
		 * Entry-point
		 */
		init: function() {
			CMDBuild.core.Splash.show();

			CMDBuild.core.Management.buildConfiguration();
		},

		/**
		 * Builds all entities cache objects
		 *
		 * @private
		 */
		buildCache: function() {
			var barrierId = 'cache';
			var params = {};

			CMDBuild.core.RequestBarrier.init(barrierId, function() {
				CMDBuild.core.Management.buildUserInterface();
			});

			/**
			 * Class and process
			 */
			params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

			CMDBuild.core.proxy.Classes.readAll({
				params: params,
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

					_CMCache.addClasses(decodedResponse);

					/**
					 * Widget
					 *
					 * Widgets must be added to cache only before classes, because widget object is added to class model
					 */
					CMDBuild.core.proxy.widget.Widget.readAll({
						loadMask: false,
						scope: this,
						success: function(response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

							// A day I'll can do a request to have only the active, now the cache discards the inactive if the flag onlyActive is true
							_CMCache.addWidgetToEntryTypes(decodedResponse, true);
						},
						callback: CMDBuild.core.RequestBarrier.getCallback(barrierId)
					});
				},
				callback: CMDBuild.core.RequestBarrier.getCallback(barrierId)
			});

			/**
			 * Dashboard
			 */
			CMDBuild.ServiceProxy.Dashboard.fullList({
				loadMask: false,
				scope: this,
				success : function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					_CMCache.addDashboards(decodedResponse.dashboards);
					_CMCache.setAvailableDataSources(decodedResponse.dataSources);
				},
				callback: CMDBuild.core.RequestBarrier.getCallback(barrierId)
			});

			/**
			 * Domain
			 */
			params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

			CMDBuild.core.proxy.domain.Domain.readAll({
				params: params,
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DOMAINS];

					_CMCache.addDomains(decodedResponse);
				},
				callback: CMDBuild.core.RequestBarrier.getCallback(barrierId)
			});

			/**
			 * Lookup
			 */
			CMDBuild.core.proxy.lookup.Type.readAll({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					_CMCache.addLookupTypes(decodedResponse);
				},
				callback: CMDBuild.core.RequestBarrier.getCallback(barrierId)
			});

			CMDBuild.core.RequestBarrier.finalize(barrierId);
		},

		/**
		 * Builds CMDBuild configurations objects
		 *
		 * @private
		 */
		buildConfiguration: function() {
			var barrierId = 'config';

			CMDBuild.core.RequestBarrier.init(barrierId, function() {
				CMDBuild.core.proxy.configuration.Configuration.readAll({
					loadMask: false,
					scope: this,
					success: function(response, options, decodedResponse) {
						/**
						 * CMDBuild (aka Instance) configuration
						 *
						 * @deprecated (CMDBuild.configuration.instance)
						 */
						CMDBuild.Config.cmdbuild = decodedResponse.cmdbuild;

						/**
						 * DMS configuration
						 *
						 * @deprecated (CMDBuild.configuration.dms)
						 */
						CMDBuild.Config.dms = decodedResponse.dms;
						CMDBuild.Config.dms.enabled = ('true' == CMDBuild.Config.dms.enabled);

						/**
						 * BIM configuration
						 *
						 * @deprecated (CMDBuild.configuration.bim)
						 */
						CMDBuild.Config.bim = decodedResponse.bim;
						CMDBuild.Config.bim.enabled = ('true' == CMDBuild.Config.bim.enabled);

						/**
						 * Graph (aka RelationGraph) configuration
						 *
						 * @deprecated (CMDBuild.configuration.graph)
						 */
						CMDBuild.Config.graph = decodedResponse.graph;

						/**
						 * Workflow configuration
						 *
						 * @deprecated (CMDBuild.configuration.workflow)
						 */
						CMDBuild.Config.workflow = decodedResponse.workflow;

						/**
						 * GIS configuration
						 *
						 * @deprecated (CMDBuild.configuration.gis)
						 */
						CMDBuild.Config.gis = decodedResponse.gis;
						CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);

						/**
						 * GIS and BIM extra configuration. Now this configurations are inside relative configuration objects
						 *
						 * @deprecated (CMDBuild.configuration.gis and CMDBuild.configuration.bim)
						 */
						CMDBuild.Config.cmdbuild.cardBrowserByDomainConfiguration = {};
						CMDBuild.ServiceProxy.gis.getGisTreeNavigation({
							success: function(response, options, decodedResponse) {
								CMDBuild.Config.cmdbuild.cardBrowserByDomainConfiguration.root = decodedResponse.root;
								CMDBuild.Config.cmdbuild.cardBrowserByDomainConfiguration.geoServerLayersMapping = decodedResponse.geoServerLayersMapping;

								if (CMDBuild.Config.bim.enabled) {
									CMDBuild.bim.proxy.rootClassName({
										success: function(response, options, decodedResponse) {
											CMDBuild.Config.bim.rootClass = decodedResponse.root;
										},
										callback: CMDBuild.core.Management.buildCache
									});
								} else {
									CMDBuild.core.Management.buildCache();
								}
							}
						});
					}
				});
			});

			Ext.create('CMDBuild.core.configurationBuilders.Instance', { callback: CMDBuild.core.RequestBarrier.getCallback(barrierId) }); // CMDBuild instance configuration
			Ext.create('CMDBuild.core.configurationBuilders.Bim', { callback: CMDBuild.core.RequestBarrier.getCallback(barrierId) }); // CMDBuild BIM configuration
			Ext.create('CMDBuild.core.configurationBuilders.Dms', { callback: CMDBuild.core.RequestBarrier.getCallback(barrierId) }); // CMDBuild DMS configuration
			Ext.create('CMDBuild.core.configurationBuilders.Gis', { callback: CMDBuild.core.RequestBarrier.getCallback(barrierId) }); // CMDBuild GIS configuration
			Ext.create('CMDBuild.core.configurationBuilders.Localization', { callback: CMDBuild.core.RequestBarrier.getCallback(barrierId) }); // CMDBuild localization configuration
			Ext.create('CMDBuild.core.configurationBuilders.RelationGraph', { callback: CMDBuild.core.RequestBarrier.getCallback(barrierId) }); // CMDBuild RelationGraph configuration
			Ext.create('CMDBuild.core.configurationBuilders.UserInterface', { callback: CMDBuild.core.RequestBarrier.getCallback(barrierId) }); // CMDBuild UserInterface configuration
			Ext.create('CMDBuild.core.configurationBuilders.Workflow', { callback: CMDBuild.core.RequestBarrier.getCallback(barrierId) }); // CMDBuild Workflow configuration

			CMDBuild.core.RequestBarrier.finalize(barrierId);
		},

		/**
		 * Build all UI modules
		 *
		 * @private
		 */
		buildUserInterface: function() {
			Ext.suspendLayouts();

			_CMCache.syncAttachmentCategories();

			Ext.ns('CMDBuild.global.controller');
			CMDBuild.global.controller.MainViewport = Ext.create('CMDBuild.controller.common.MainViewport', {
				accordion: [ // Display order
					Ext.create('CMDBuild.controller.management.accordion.Menu', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getMenu() }),
					CMDBuild.configuration.userInterface.isDisabledModule('class') ? null :
						Ext.create('CMDBuild.controller.management.accordion.Classes', { identifier: 'class' })
					,
					CMDBuild.configuration.userInterface.isDisabledModule('process') || !(CMDBuild.Config.workflow.enabled == 'true') ? null :
						Ext.create('CMDBuild.controller.management.accordion.Workflow', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getWorkflow() })
					,
					CMDBuild.configuration.userInterface.isDisabledModule(CMDBuild.core.constants.Proxy.DATA_VIEW) ? null :
						Ext.create('CMDBuild.controller.management.accordion.DataView', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getDataView() })
					,
					CMDBuild.configuration.userInterface.isDisabledModule('dashboard') ? null :
						Ext.create('CMDBuild.controller.management.accordion.Dashboard', { identifier: 'dashboard' })
					,
					CMDBuild.configuration.userInterface.isDisabledModule('report') ? null :
						Ext.create('CMDBuild.controller.management.accordion.Report', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getReport() })
					,
					CMDBuild.configuration.userInterface.isDisabledModule(CMDBuild.core.constants.Proxy.CUSTOM_PAGES) ? null :
						Ext.create('CMDBuild.controller.management.accordion.CustomPage', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getCustomPage() })
					,
					Ext.create('CMDBuild.controller.management.accordion.Utility', { identifier: 'utility' })
				],
				module: [
					Ext.create('CMDBuild.controller.management.customPage.SinglePage', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getCustomPage() }),
					Ext.create('CMDBuild.controller.management.dataView.DataView', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getDataView() }),
					Ext.create('CMDBuild.controller.management.report.Report', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getReport() }),
					Ext.create('CMDBuild.controller.management.report.Single', { identifier: 'singlereport' }),
					new CMDBuild.view.management.classes.CMModCard({
						cmControllerType: CMDBuild.controller.management.classes.CMModCardController,
						cmName: 'class'
					}),
					new CMDBuild.view.management.workflow.CMModProcess({
						cmControllerType: CMDBuild.controller.management.workflow.CMModWorkflowController,
						cmName: CMDBuild.core.constants.ModuleIdentifiers.getWorkflow()
					}),
					new CMDBuild.view.management.dashboard.CMModDashboard({
						cmControllerType: CMDBuild.controller.management.dashboard.CMModDashboardController,
						cmName: 'dashboard'
					}),
					new CMDBuild.view.management.utilities.CMModChangePassword(),
					new CMDBuild.view.management.utilites.CMModBulkCardUpdate({
						cmControllerType: CMDBuild.controller.management.utilities.CMModBulkUpdateController
					}),
					new CMDBuild.view.management.utilities.CMModImportCSV({
						cmControllerType: CMDBuild.controller.management.utilities.CMModImportCSVController
					}),
					new CMDBuild.view.management.utilities.CMModExportCSV()
				],
				hideAccordions: CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.HIDE_SIDE_PANEL)
			});

			Ext.resumeLayouts();

			CMDBuild.core.Splash.hide(function() {
				CMDBuild.global.controller.MainViewport.cmfg('mainViewportInstanceNameSet', CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.INSTANCE_NAME));

				// Execute routes
				CMDBuild.routes.Routes.exec();

				CMDBuild.global.controller.MainViewport.cmfg('mainViewportStartingEntitySelect');
			}, this);

			if (CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.FULL_SCREEN_MODE))
				_CMUIState.onlyGrid();
		}
	});

})();