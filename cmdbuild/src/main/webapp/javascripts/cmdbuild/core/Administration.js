(function() {

	/**
	 * Call sequence: init() -> buildConfiguration() -> buildCache() -> buildUserInterface()
	 */
	Ext.define('CMDBuild.core.Administration', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.configuration.Configuration',
			'CMDBuild.core.proxy.domain.Domain',
			'CMDBuild.core.proxy.lookup.Type',
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
			CMDBuild.core.Splash.show(true);

			CMDBuild.core.Administration.buildConfiguration();
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
				CMDBuild.core.Administration.buildUserInterface();
			});

			/**
			 * Class and process
			 */
			params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

			CMDBuild.core.proxy.Classes.readAll({
				params: params,
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

					_CMCache.addClasses(decodedResponse);
				},
				callback: CMDBuild.core.RequestBarrier.getCallback(barrierId)
			});

			/**
			 * Domain
			 */
			if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
				CMDBuild.core.proxy.domain.Domain.readAll({
					loadMask: false,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DOMAINS];

						_CMCache.addDomains(decodedResponse);
					},
					callback: CMDBuild.core.RequestBarrier.getCallback(barrierId)
				});

			/**
			 * Groups
			 */
			CMDBuild.core.proxy.userAndGroup.group.Group.readAll({
				loadMask: false,
				scope: this,
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

			/**
			 * Navigation tree
			 */
			_CMCache.listNavigationTrees({
				loadMask: false,
				scope: this,
				success: Ext.emptyFn,
				callback: CMDBuild.core.RequestBarrier.getCallback(barrierId)
			});

			/**
			 * Dashboard
			 */
			CMDBuild.ServiceProxy.Dashboard.fullList({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					_CMCache.addDashboards(decodedResponse.dashboards);
					_CMCache.setAvailableDataSources(decodedResponse.dataSources);
				},
				callback: CMDBuild.core.RequestBarrier.getCallback(barrierId)
			});

			/**
			 * Widget
			 */
			CMDBuild.core.proxy.widget.Widget.readAll({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					_CMCache.addWidgetToEntryTypes(decodedResponse);
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

			/**
			 * @deprecated
			 */
			Ext.ns('CMDBuild.Config');

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
						 * GIS configuration
						 *
						 * @deprecated (CMDBuild.configuration.gis)
						 */
						CMDBuild.Config.gis = decodedResponse.gis;
						CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);
					},
					callback: CMDBuild.core.Administration.buildCache
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

			Ext.ns('CMDBuild.global.controller');
			CMDBuild.global.controller.MainViewport = Ext.create('CMDBuild.controller.common.MainViewport', {
				isAdministration: true,
				accordion: [ // Display order
					CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN) ? null :
						Ext.create('CMDBuild.controller.administration.accordion.Classes', { identifier: 'class' })
					,
					CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN) ? null :
						Ext.create('CMDBuild.controller.administration.accordion.Workflow', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getWorkflow() })
					,
					CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN) ? null :
						Ext.create('CMDBuild.controller.administration.accordion.Domain', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getDomain() })
					,
					CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN) ? null :
						Ext.create('CMDBuild.controller.administration.accordion.DataView', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getDataView() })
					,
					Ext.create('CMDBuild.controller.administration.accordion.Filter', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getFilter() }),
					CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN) ? null :
						Ext.create('CMDBuild.controller.administration.accordion.NavigationTree', { identifier: 'navigationtree' })
					,
					Ext.create('CMDBuild.controller.administration.accordion.Lookup', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getLookupType() }),
					CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN) ? null :
						Ext.create('CMDBuild.controller.administration.accordion.Dashboard', { identifier: 'dashboard' })
					,
					Ext.create('CMDBuild.controller.administration.accordion.Report', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getReport() }),
					Ext.create('CMDBuild.controller.administration.accordion.Menu', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getMenu() }),
					Ext.create('CMDBuild.controller.administration.accordion.UserAndGroup', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getUserAndGroup() }),
					Ext.create('CMDBuild.controller.administration.accordion.Task', { identifier: 'task' }),
					Ext.create('CMDBuild.controller.administration.accordion.Email', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getEmail() }),
					Ext.create('CMDBuild.controller.administration.accordion.Gis', { identifier: 'gis' }),
					Ext.create('CMDBuild.controller.administration.accordion.Bim', { identifier: 'bim' }),
					Ext.create('CMDBuild.controller.administration.accordion.Localization', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getLocalization() }),
					Ext.create('CMDBuild.controller.administration.accordion.Configuration', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getConfiguration() })
				],
				module: [
					Ext.create('CMDBuild.controller.administration.configuration.Configuration', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getConfiguration() }),
					Ext.create('CMDBuild.controller.administration.dataView.DataView', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getDataView() }),
					Ext.create('CMDBuild.controller.administration.domain.Domain', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getDomain() }),
					Ext.create('CMDBuild.controller.administration.email.Email', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getEmail() }),
					Ext.create('CMDBuild.controller.administration.filter.Filter', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getFilter() }),
					Ext.create('CMDBuild.controller.administration.localization.Localization', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getLocalization() }),
					Ext.create('CMDBuild.controller.administration.lookup.Lookup', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getLookupType() }),
					Ext.create('CMDBuild.controller.administration.menu.Menu', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getMenu() }),
					Ext.create('CMDBuild.controller.administration.report.Report', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getReport() }),
					Ext.create('CMDBuild.controller.administration.userAndGroup.UserAndGroup', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getUserAndGroup() }),
					Ext.create('CMDBuild.controller.administration.workflow.Workflow', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getWorkflow() }),
					Ext.create('CMDBuild.view.administration.gis.CMModGeoServer', {
						cmControllerType: 'CMDBuild.controller.administration.gis.CMModGeoServerController',
						cmName: 'gis-geoserver'
					}),
					Ext.create('CMDBuild.view.administration.gis.ExternalServices', {
						cmControllerType: 'CMDBuild.controller.administration.gis.ExternalServicesController',
						cmName: 'gis-external-services'
					}),
					Ext.create('CMDBuild.view.administration.tasks.CMTasks', {
						cmControllerType: 'CMDBuild.controller.administration.tasks.CMTasksController',
						cmName: 'task'
					}),
					new CMDBuild.view.administration.bim.CMBIMPanel({
						cmControllerType: CMDBuild.controller.administration.filter.CMBIMPanelController,
						cmName: 'bim-project'
					}),
					new CMDBuild.bim.administration.view.CMBimLayers({
						cmControllerType: CMDBuild.controller.administration.filter.CMBimLayerController,
						cmName: 'bim-layers'
					}),
					new CMDBuild.view.common.CMUnconfiguredModPanel({
						cmControllerType: CMDBuild.controller.common.CMUnconfiguredModPanelController,
						cmName: 'notconfiguredpanel'
					}),
					new CMDBuild.view.administration.classes.CMModClass({
						cmControllerType: CMDBuild.controller.administration.classes.CMModClassController,
						cmName: 'class'
					}),
					new CMDBuild.Administration.ModIcons({
						cmName: 'gis-icons'
					}),
					new CMDBuild.view.administration.gis.CMModGISNavigationConfiguration({
						cmControllerType: CMDBuild.controller.administration.gis.CMModGISNavigationConfigurationController,
						cmName: 'gis-filter-configuration'
					}),
					new CMDBuild.Administration.ModLayerOrder({
						cmControllerType: CMDBuild.controller.administration.gis.CMModLayerOrderController,
						cmName: 'gis-layers-order'
					}),
					new CMDBuild.view.administration.navigationTrees.CMModNavigationTrees({
						cmControllerType: CMDBuild.controller.administration.navigationTrees.CMModNavigationTreesController,
						cmName: 'navigationtree'
					}),
					new CMDBuild.view.administration.dashboard.CMModDashboard({
						cmControllerType: CMDBuild.controller.administration.dashboard.CMModDashboardController,
						cmName: 'dashboard'
					})
				]
			});

			Ext.resumeLayouts();

			CMDBuild.core.Splash.hide(function() {
				CMDBuild.global.controller.MainViewport.cmfg('mainViewportInstanceNameSet', CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.INSTANCE_NAME));
				CMDBuild.global.controller.MainViewport.cmfg('mainViewportSelectFirstExpandedAccordionSelectableNode');
			}, this);
		}
	});

})();