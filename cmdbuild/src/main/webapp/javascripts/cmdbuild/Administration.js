(function() {

	var bimAccordion = null;
	var classesAccordion = null;
	var controllerNS = CMDBuild.controller;
	var dashboardsAccordion = null;
	var dataViewAccordion = null;
	var domainAccordion = null;
	var gisAccordion = null;
	var groupsAccordion = null;
	var lookupAccordion = null;
	var menuAccordion = null;
	var navigationTreesAccordion = null;
	var processAccordion = null;

	Ext.define('CMDBuild.app.Administration', {
		extend: 'Ext.app.Application',

		requires: [
			'CMDBuild.core.buttons.Buttons',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.Configuration',
			'CMDBuild.core.proxy.Domain',
			'CMDBuild.core.proxy.Lookup',
			'CMDBuild.core.proxy.reports.Reports'
		],

		name: 'CMDBuild',
		appFolder: './javascripts/cmdbuild',

		statics: {
			init: function() {
				var me = this;
				var administration = true;
				var forCredits = false;

				Ext.create('CMDBuild.core.LoggerManager'); // Logger configuration
				Ext.create('CMDBuild.core.Data'); // Data connections configuration

				Ext.tip.QuickTipManager.init();
				// fix a problem of Ext 4.2 tooltips width
				// see http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text/page3#24
				delete Ext.tip.Tip.prototype.minWidth;

				CMDBuild.view.CMMainViewport.showSplash(forCredits, administration);

				// Setup config localization model
				CMDBuild.Config[CMDBuild.core.proxy.CMProxyConstants.LOCALIZATION] = Ext.create('CMDBuild.model.configuration.Localization');

				// Get server language
				CMDBuild.core.proxy.Configuration.getLanguage({
					success: function(result, options, decodedResult) {
						CMDBuild.Config[CMDBuild.core.proxy.CMProxyConstants.LOCALIZATION].set(
							CMDBuild.core.proxy.CMProxyConstants.LANGUAGE,
							decodedResult[CMDBuild.core.proxy.CMProxyConstants.LANGUAGE]
						);
					}
				});

				// maybe a single request with all the configuration could be better
				CMDBuild.ServiceProxy.group.getUIConfiguration({
					success: function(response, options,decoded) {
						_CMUIConfiguration = new CMDBuild.model.CMUIConfigurationModel(decoded.response);

						CMDBuild.ServiceProxy.configuration.readMainConfiguration({
							success: function(response, options, decoded) {
								// CMDBuild
								CMDBuild.Config.cmdbuild = decoded.data;

								// Localization
								// TODO: refactor to avoid to use Cache
								_CMCache.setActiveTranslations(decoded.data.enabled_languages);
								CMDBuild.Config[CMDBuild.core.proxy.CMProxyConstants.LOCALIZATION].setLanguagesWithLocalizations(decoded.data.enabled_languages);

								/* **********************************************
								 * Suspend here the layouts, and resume after all
								 * the load are end
								 * **********************************************/
								Ext.suspendLayouts();
								/* ***********************************************/

								var panels = [
									Ext.create('Ext.panel.Panel', {
										cls: 'empty_panel x-panel-body'
									}),
									Ext.create('CMDBuild.view.administration.configuration.ConfigurationView', {
										cmControllerType: 'CMDBuild.controller.administration.configuration.Configuration',
										cmName: 'configuration'
									}),
									Ext.create('CMDBuild.view.administration.email.EmailView', {
										cmControllerType: 'CMDBuild.controller.administration.email.Email',
										cmName: 'email'
									}),
									Ext.create('CMDBuild.view.administration.filters.FiltersView', {
										cmControllerType: 'CMDBuild.controller.administration.filters.Filters',
										cmName: 'filters'
									}),
									Ext.create('CMDBuild.view.administration.localizations.LocalizationsView', {
										cmControllerType: 'CMDBuild.controller.administration.localizations.Localizations',
										cmName: 'localizations'
									}),
									Ext.create('CMDBuild.view.administration.reports.ReportsView', {
										cmControllerType: 'CMDBuild.controller.administration.reports.Reports',
										cmName: 'report'
									}),
									Ext.create('CMDBuild.view.administration.tasks.CMTasks', {
										cmControllerType: 'CMDBuild.controller.administration.tasks.CMTasksController',
										cmName: 'tasks'
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
										cmControllerType: controllerNS.common.CMUnconfiguredModPanelController,
										cmName: 'notconfiguredpanel'
									})
								];

								/**
								 * DataViews
								 */
								if (!_CMUIConfiguration.isCloudAdmin()) {
									dataViewAccordion = Ext.create('CMDBuild.view.administration.accordion.DataViews', {
										cmName: 'dataview',
									});

									panels = panels.concat([
										Ext.create('CMDBuild.view.administration.dataViews.DataViewsView', {
											cmControllerType: 'CMDBuild.controller.administration.dataViews.DataViews',
											cmName: 'dataviews'
										}),
									]);
								}

								_CMMainViewportController = new CMDBuild.controller.CMMainViewportController(
									new CMDBuild.view.CMMainViewport({
										cmAccordions: [],
										cmPanels: panels
									})
								);

								me.loadResources();
							}
						});
					}
				});
			},

			loadResources: function() {
				var reqBarrier = new CMDBuild.Utils.CMRequestBarrier(
					function callback() {
						_CMMainViewportController.addAccordion([
							classesAccordion,
							processAccordion,
							domainAccordion,
							dataViewAccordion,
							Ext.create('CMDBuild.view.administration.accordion.Filters', { cmName: 'filters' }),
							navigationTreesAccordion,
							lookupAccordion,
							dashboardsAccordion,
							Ext.create('CMDBuild.view.administration.accordion.Reports', { cmName: 'report' }),
							menuAccordion,
							groupsAccordion,
							Ext.create('CMDBuild.view.administration.accordion.Tasks', { cmName: 'tasks' }),
							Ext.create('CMDBuild.view.administration.accordion.Email', { cmName: 'email' }),
							gisAccordion,
							bimAccordion,
							Ext.create('CMDBuild.view.administration.accordion.Localizations', { cmName: 'localizations' }),
							Ext.create('CMDBuild.view.administration.accordion.Configuration', { cmName: 'setup' })
						]);

						// Resume here the layouts operations
						Ext.resumeLayouts(true);

						_CMMainViewportController.viewport.doLayout();

						CMDBuild.view.CMMainViewport.hideSplash(function() {
							_CMMainViewportController.setInstanceName(CMDBuild.Config.cmdbuild.instance_name);
							_CMMainViewportController.selectFirstSelectableLeafOfOpenedAccordion();
						});
					}
				);

				/**
				 * BIM Configuration
				 * */
				CMDBuild.ServiceProxy.configuration.readBimConfiguration({
					success: function(response, option, decoded) {
						var disabled = decoded.data.enabled == 'false';
						bimAccordion = new CMDBuild.view.administration.accordion.CMBIMAccordion({
							disabled: disabled
						});
					}
				});

				/**
				 * Classes and process
				 */
				CMDBuild.ServiceProxy.classes.read({
					params: {
						active: false
					},
					success: function(response, options, decoded) {
						_CMCache.addClasses(decoded.classes);

						if (!_CMUIConfiguration.isCloudAdmin()) {
							classesAccordion = new CMDBuild.view.administration.accordion.CMClassAccordion({
								cmControllerType: CMDBuild.controller.accordion.CMClassAccordionController
							});
							classesAccordion.updateStore();

							processAccordion = new CMDBuild.view.administration.accordion.CMProcessAccordion({
								cmControllerType: CMDBuild.controller.accordion.CMProcessAccordionController,
								disabled: (CMDBuild.Config.workflow) ? !CMDBuild.Config.workflow.enabled : true // FIX: to avoid InternetExplorer error on startup
							});
							processAccordion.updateStore();

							_CMMainViewportController.addPanel([
								new CMDBuild.view.administration.classes.CMModClass({
									cmControllerType: controllerNS.administration.classes.CMModClassController
								}),
								new CMDBuild.view.administration.workflow.CMProcess({
									cmControllerType: controllerNS.administration.workflow.CMProcessController
								})
							]);
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
				 * Workflow configuration
				 */
				CMDBuild.ServiceProxy.configuration.readWFConfiguration({
					success: function(response, options, decoded) {
						CMDBuild.Config.workflow = decoded.data;
						CMDBuild.Config.workflow.enabled = ('true' == CMDBuild.Config.workflow.enabled);
					},
					callback: reqBarrier.getCallback()
				});

				/**
				 * GIS configuration
				 */
				CMDBuild.ServiceProxy.configuration.readGisConfiguration({
					success: function(response, options, decoded) {
						CMDBuild.Config.gis = decoded.data;
						CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);

						if (!_CMUIConfiguration.isCloudAdmin()) {
							gisAccordion = new CMDBuild.view.administration.accordion.CMGISAccordion({
								disabled: !CMDBuild.Config.gis.enabled
							});

							_CMMainViewportController.addPanel([
								new CMDBuild.Administration.ModIcons(),
								new CMDBuild.view.administration.gis.CMModGISNavigationConfiguration({
									cmControllerType: controllerNS.administration.gis.CMModGISNavigationConfigurationController
								}),
								Ext.create('CMDBuild.view.administration.gis.ExternalServices', {
									cmControllerType: 'CMDBuild.controller.administration.gis.ExternalServicesController',
									cmName: 'gis-external-services'
								}),
								Ext.create('CMDBuild.view.administration.gis.CMModGeoServer', {
									cmControllerType: 'CMDBuild.controller.administration.gis.CMModGeoServerController',
									cmName: 'gis-geoserver'
								}),
								new CMDBuild.Administration.ModLayerOrder({
									cmControllerType: controllerNS.administration.gis.CMModLayerOrderController
								})
							]);
						}
					},

					callback: reqBarrier.getCallback()
				});

				/**
				 * Lookups
				 */
				CMDBuild.core.proxy.Lookup.readAll({
					success: function(response, options, decodedResponse) {
						_CMCache.addLookupTypes(decodedResponse);

						lookupAccordion = new CMDBuild.view.administration.accordion.CMLookupAccordion({
							cmControllerType: CMDBuild.controller.accordion.CMLookupAccordionController
						});
						lookupAccordion.updateStore();

						_CMMainViewportController.addPanel(
							new CMDBuild.Administration.ModLookup({
								cmControllerType: controllerNS.administration.lookup.CMModLookupController
							})
						);
					},
					callback: reqBarrier.getCallback()
				});

				/**
				 * Groups
				 */
				CMDBuild.ServiceProxy.group.read({
					success: function(response, options, decoded) {
						_CMCache.addGroups(decoded.groups);

						groupsAccordion = Ext.create('CMDBuild.view.administration.accordion.Groups', { cmName: 'groups' });
						groupsAccordion.updateStore();

						menuAccordion = new CMDBuild.view.administration.accordion.CMMenuAccordion({
							cmControllerType: CMDBuild.controller.accordion.CMMenuAccordionController
						});
						menuAccordion.updateStore();

						_CMMainViewportController.addPanel([
							new CMDBuild.Administration.ModMenu({
								cmControllerType: controllerNS.administration.menu.CMModMenuController
							}),
							new CMDBuild.view.administration.group.CMModGroup({
								cmControllerType: controllerNS.administration.group.CMModGroupsController
							}),
							Ext.create('CMDBuild.view.administration.users.UsersView', {
								cmControllerType: 'CMDBuild.controller.administration.users.Users',
								cmName: 'users'
							})
						]);
					},
					callback: reqBarrier.getCallback()
				});

				/**
				 * Domains
				 */
				CMDBuild.core.proxy.Domain.getAll({
					scope: this,
					success: function(response, options, decodedResponse) {
						_CMCache.addDomains(decodedResponse.domains);

						if (!_CMUIConfiguration.isCloudAdmin()) {
							domainAccordion = Ext.create('CMDBuild.view.administration.accordion.Domain', {
								cmControllerType: 'CMDBuild.controller.accordion.Domain',
								cmName: 'domain'
							});
							domainAccordion.updateStore();

							_CMMainViewportController.addPanel(
								Ext.create('CMDBuild.view.administration.domain.DomainView', {
									cmControllerType: 'CMDBuild.controller.administration.domain.Domain',
									cmName:'domain'
								})
							);
						}
					},
					callback: reqBarrier.getCallback()
				});

				/**
				 * Navigation trees
				 */
				_CMCache.listNavigationTrees({
					success: function(response, options, decoded) {

						if (!_CMUIConfiguration.isCloudAdmin()) {
							navigationTreesAccordion = new CMDBuild.view.administration.accordion.CMNavigationTreesAccordion({
								cmControllerType: CMDBuild.controller.accordion.CMNavigationTreesAccordionController
							});
							navigationTreesAccordion.updateStore();

							_CMMainViewportController.addPanel(
								new CMDBuild.view.administration.navigationTrees.CMModNavigationTrees({
									cmControllerType: controllerNS.administration.navigationTrees.CMModNavigationTreesController
								})
							);
						}
					},
					callback: reqBarrier.getCallback()
				});

				/**
				 * Dashboards
				 */
				CMDBuild.ServiceProxy.Dashboard.fullList({
					success: function(response, options, decoded) {
						_CMCache.addDashboards(decoded.response.dashboards);
						_CMCache.setAvailableDataSources(decoded.response.dataSources);

						if (!_CMUIConfiguration.isCloudAdmin()) {
							dashboardsAccordion = new CMDBuild.view.administration.accordion.CMDashboardAccordion({
								cmControllerType: CMDBuild.controller.accordion.CMDashboardAccordionController
							});
							dashboardsAccordion.updateStore();

							_CMMainViewportController.addPanel(
								new CMDBuild.view.administration.dashboard.CMModDashboard({
									cmControllerType: controllerNS.administration.dashboard.CMModDashboardController
								})
							);
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