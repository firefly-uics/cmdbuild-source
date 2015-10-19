(function() {

	var classesAccordion = new CMDBuild.view.administration.accordion.CMClassAccordion({
		cmControllerType: CMDBuild.controller.accordion.CMClassAccordionController
	});;
	var dashboardsAccordion = new CMDBuild.view.administration.accordion.CMDashboardAccordion({
		cmControllerType: CMDBuild.controller.accordion.CMDashboardAccordionController
	});
	var navigationTreesAccordion = new CMDBuild.view.administration.accordion.CMNavigationTreesAccordion({
		cmControllerType: CMDBuild.controller.accordion.CMNavigationTreesAccordionController
	});
	var processAccordion = new CMDBuild.view.administration.accordion.CMProcessAccordion({
		cmControllerType: CMDBuild.controller.accordion.CMProcessAccordionController
	});

	Ext.define('CMDBuild.app.Administration', {
		extend: 'Ext.app.Application',

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.configuration.Configuration',
//			'CMDBuild.core.proxy.configuration.GeneralOptions',
//			'CMDBuild.core.proxy.configuration.Gis',
//			'CMDBuild.core.proxy.configuration.Workflow',
			'CMDBuild.core.proxy.domain.Domain',
			'CMDBuild.core.proxy.lookup.Type',
			'CMDBuild.core.proxy.report.Report',
			'CMDBuild.core.proxy.userAndGroup.group.Group'
		],

		appFolder: './javascripts/cmdbuild',
		name: 'CMDBuild',

		statics: {
			init: function() {
				Ext.create('CMDBuild.core.LoggerManager'); // Logger configuration
				Ext.create('CMDBuild.core.Data'); // Data connections configuration
				Ext.create('CMDBuild.core.Rest'); // Setup REST connection

				Ext.tip.QuickTipManager.init();
				// fix a problem of Ext 4.2 tooltips width
				// see http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text/page3#24
				delete Ext.tip.Tip.prototype.minWidth;

				CMDBuild.view.CMMainViewport.showSplash(false, true);

				var configurationsRequestBarrier = Ext.create('CMDBuild.core.RequestBarrier', {
					callback: function() {
						CMDBuild.core.proxy.configuration.Configuration.readAll({
							loadMask: false,
							scope: this,
							success: function(response, options, decodedResponse) {
								/**
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

								/**
								 * Workflow configuration
								 *
								 * @deprecated (CMDBuild.configuration.workflow)
								 */
								CMDBuild.Config.workflow = decodedResponse.workflow;
								CMDBuild.Config.workflow.enabled = ('true' == CMDBuild.Config.workflow.enabled);
							},
							callback: CMDBuild.app.Administration.buildComponents
						});
					}
				});

				Ext.create('CMDBuild.core.configurationBuilders.Instance', { callback: configurationsRequestBarrier.getCallback() }); // CMDBuild instance configuration
				Ext.create('CMDBuild.core.configurationBuilders.Bim', { callback: configurationsRequestBarrier.getCallback() }); // CMDBuild BIM configuration
				Ext.create('CMDBuild.core.configurationBuilders.Gis', { callback: configurationsRequestBarrier.getCallback() }); // CMDBuild GIS configuration
				Ext.create('CMDBuild.core.configurationBuilders.Localization', { callback: configurationsRequestBarrier.getCallback() }); // CMDBuild localization configuration
				Ext.create('CMDBuild.core.configurationBuilders.UserInterface', { callback: configurationsRequestBarrier.getCallback() }); // CMDBuild UserInterface configuration

				configurationsRequestBarrier.start();
			},

			buildComponents: function() {
				Ext.suspendLayouts(); // Suspend here the layouts, and resume after all the load are end

				_CMMainViewportController = new CMDBuild.controller.CMMainViewportController(
					new CMDBuild.view.CMMainViewport({
						cmAccordions: [ // Sorted
							classesAccordion,
							processAccordion,
							CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN) ? null :
								Ext.create('CMDBuild.view.administration.accordion.Domain', {
									cmControllerType: 'CMDBuild.controller.common.AbstractAccordionController',
									cmName: 'domain'
								})
							,
							CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN) ? null :
								Ext.create('CMDBuild.view.administration.accordion.DataView', {
									cmControllerType: 'CMDBuild.controller.common.AbstractAccordionController',
									cmName: 'dataview'
								})
							,
							Ext.create('CMDBuild.view.administration.accordion.Filter', {
								cmControllerType: 'CMDBuild.controller.common.AbstractAccordionController',
								cmName: 'filter'
							}),
							navigationTreesAccordion,
							Ext.create('CMDBuild.view.administration.accordion.Lookup', {
								cmControllerType: 'CMDBuild.controller.administration.accordion.Lookup',
								cmName: 'lookuptype',
							}),
							dashboardsAccordion,
							Ext.create('CMDBuild.view.administration.accordion.Report', {
								cmControllerType: 'CMDBuild.controller.common.AbstractAccordionController',
								cmName: 'report'
							}),
							Ext.create('CMDBuild.view.administration.accordion.Menu', {
								cmControllerType: 'CMDBuild.controller.common.AbstractAccordionController',
								cmName: 'menu'
							}),
							Ext.create('CMDBuild.view.administration.accordion.UserAndGroup', {
								cmControllerType: 'CMDBuild.controller.common.AbstractAccordionController',
								cmName: 'userandgroup'
							}),
							Ext.create('CMDBuild.view.administration.accordion.Task', {
								cmControllerType: 'CMDBuild.controller.administration.accordion.Task',
								cmName: 'task'
							}),
							Ext.create('CMDBuild.view.administration.accordion.Email', {
								cmControllerType: 'CMDBuild.controller.common.AbstractAccordionController',
								cmName: 'email'
							}),
							Ext.create('CMDBuild.view.administration.accordion.Gis', {
								cmControllerType: 'CMDBuild.controller.common.AbstractAccordionController',
								cmName: 'gis'
							}),
							Ext.create('CMDBuild.view.administration.accordion.Bim', {
								cmControllerType: 'CMDBuild.controller.common.AbstractAccordionController',
								cmName: 'bim'
							}),
							Ext.create('CMDBuild.view.administration.accordion.Localization', {
								cmControllerType: 'CMDBuild.controller.common.AbstractAccordionController',
								cmName: 'localizations'
							}),
							Ext.create('CMDBuild.view.administration.accordion.Configuration', {
								cmControllerType: 'CMDBuild.controller.common.AbstractAccordionController',
								cmName: 'configuration'
							})
						],
						cmPanels: [
							Ext.create('Ext.panel.Panel', {
								cls: 'empty_panel x-panel-body'
							}),
							Ext.create('CMDBuild.view.administration.configuration.ConfigurationView', {
								cmControllerType: 'CMDBuild.controller.administration.configuration.Configuration',
								cmName: 'configuration'
							}),
							Ext.create('CMDBuild.view.administration.dataView.DataViewView', {
								cmControllerType: 'CMDBuild.controller.administration.dataView.DataView',
								cmName: 'dataview'
							}),
							Ext.create('CMDBuild.view.administration.domain.DomainView', {
								cmControllerType: 'CMDBuild.controller.administration.domain.Domain',
								cmName: 'domain'
							}),
							Ext.create('CMDBuild.view.administration.email.EmailView', {
								cmControllerType: 'CMDBuild.controller.administration.email.Email',
								cmName: 'email'
							}),
							Ext.create('CMDBuild.view.administration.filter.FilterView', {
								cmControllerType: 'CMDBuild.controller.administration.filter.Filter',
								cmName: 'filter'
							}),
							Ext.create('CMDBuild.view.administration.gis.CMModGeoServer', {
								cmControllerType: 'CMDBuild.controller.administration.gis.CMModGeoServerController',
								cmName: 'gis-geoserver'
							}),
							Ext.create('CMDBuild.view.administration.gis.ExternalServices', {
								cmControllerType: 'CMDBuild.controller.administration.gis.ExternalServicesController',
								cmName: 'gis-external-services'
							}),
							Ext.create('CMDBuild.view.administration.localization.LocalizationView', {
								cmControllerType: 'CMDBuild.controller.administration.localization.Localization',
								cmName: 'localizations'
							}),
							Ext.create('CMDBuild.view.administration.lookup.LookupView', {
								cmControllerType: 'CMDBuild.controller.administration.lookup.Lookup',
								cmName: 'lookuptype'
							}),
							Ext.create('CMDBuild.view.administration.menu.MenuView', {
								cmControllerType: 'CMDBuild.controller.administration.menu.Menu',
								cmName: 'menu'
							}),
							Ext.create('CMDBuild.view.administration.report.ReportView', {
								cmControllerType: 'CMDBuild.controller.administration.report.Report',
								cmName: 'report'
							}),
							Ext.create('CMDBuild.view.administration.tasks.CMTasks', {
								cmControllerType: 'CMDBuild.controller.administration.tasks.CMTasksController',
								cmName: 'task'
							}),
							Ext.create('CMDBuild.view.administration.userAndGroup.UserAndGroupView', {
								cmControllerType: 'CMDBuild.controller.administration.userAndGroup.UserAndGroup',
								cmName: 'userandgroup'
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
								cmControllerType: CMDBuild.controller.administration.classes.CMModClassController
							}),
							new CMDBuild.view.administration.workflow.CMProcess({
								cmControllerType: CMDBuild.controller.administration.workflow.CMProcessController
							}),
							new CMDBuild.Administration.ModIcons(),
							new CMDBuild.view.administration.gis.CMModGISNavigationConfiguration({
								cmControllerType: CMDBuild.controller.administration.gis.CMModGISNavigationConfigurationController
							}),
							new CMDBuild.Administration.ModLayerOrder({
								cmControllerType: CMDBuild.controller.administration.gis.CMModLayerOrderController
							}),
							new CMDBuild.view.administration.navigationTrees.CMModNavigationTrees({
								cmControllerType: CMDBuild.controller.administration.navigationTrees.CMModNavigationTreesController
							}),
							new CMDBuild.view.administration.dashboard.CMModDashboard({
								cmControllerType: CMDBuild.controller.administration.dashboard.CMModDashboardController
							})
						]
					})
				);

				var reqBarrier = Ext.create('CMDBuild.core.RequestBarrier', {
					callback: function() {
						Ext.resumeLayouts(true); // Resume here the layouts operations

						_CMMainViewportController.viewport.doLayout();

						CMDBuild.view.CMMainViewport.hideSplash(function() {
							_CMMainViewportController.setInstanceName(CMDBuild.Config.cmdbuild.instance_name);
							_CMMainViewportController.selectFirstSelectableLeafOfOpenedAccordion();
						});
					}
				});

				/**
				 * Classes and process
				 */
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

				CMDBuild.core.proxy.Classes.readAll({
					params: params,
					loadMask: false,
					scope: this,
					success: function(response, options, decoded) {
						_CMCache.addClasses(decoded.classes);

						if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN)) {
							classesAccordion.updateStore();

							processAccordion.setDisabled((CMDBuild.Config.workflow) ? !CMDBuild.Config.workflow.enabled : true); // FIX: to avoid InternetExplorer error on startup
							processAccordion.updateStore();
						}

						// Do a separate request for the widgets because, at this time
						// it is not possible serialize them with the classes
						CMDBuild.ServiceProxy.CMWidgetConfiguration.read({
							scope: this,
							callback: reqBarrier.getCallback(),
							success: function(response, options, decoded) {
								_CMCache.addWidgetToEntryTypes(decoded.response);
							}
						});
					},
					callback: reqBarrier.getCallback()
				});

				/**
				 * Domains
				 *
				 * Cache build call
				 */
				if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
					CMDBuild.core.proxy.domain.Domain.readAll({
						loadMask: false,
						scope: this,
						success: function(response, options, decodedResponse) {
							_CMCache.addDomains(decodedResponse.domains);
						},
						callback: reqBarrier.getCallback()
					});

				/**
				 * Groups
				 *
				 * Cache build call
				 */
				CMDBuild.core.proxy.userAndGroup.group.Group.readAll({
					loadMask: false,
					callback: reqBarrier.getCallback()
				});

				/**
				 * Lookup
				 *
				 * Cache build call
				 */
				CMDBuild.core.proxy.lookup.Type.readAll({
					loadMask: false,
					success: function(response, options, decodedResponse) {
						_CMCache.addLookupTypes(decodedResponse);
					},
					callback: reqBarrier.getCallback()
				});

				/**
				 * Navigation trees
				 */
				_CMCache.listNavigationTrees({
					loadMask: false,
					scope: this,
					success: function(response, options, decoded) {
						if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN)) {
							navigationTreesAccordion.updateStore();
						}
					},
					callback: reqBarrier.getCallback()
				});

				/**
				 * Dashboards
				 */
				CMDBuild.ServiceProxy.Dashboard.fullList({
					loadMask: false,
					scope: this,
					success: function(response, options, decoded) {
						_CMCache.addDashboards(decoded.response.dashboards);
						_CMCache.setAvailableDataSources(decoded.response.dataSources);

						if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN)) {
							dashboardsAccordion.updateStore();
						}
					},
					callback: reqBarrier.getCallback()
				});

				reqBarrier.start();
			}
		}
	});

	Ext.application('CMDBuild.app.Administration');

})();