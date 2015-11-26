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
	var reportAccordion = null;

	Ext.define('CMDBuild.app.Administration', {
		extend: 'Ext.app.Application',

		requires: [
			'CMDBuild.core.buttons.Buttons',
			'CMDBuild.core.constants.Server',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Configuration',
			'CMDBuild.core.proxy.group.Group',
			'CMDBuild.core.proxy.Localizations',
			'CMDBuild.core.proxy.Report'
		],

		name: 'CMDBuild',
		appFolder: './javascripts/cmdbuild',

		statics: {
			init: function() {
				var me = this;
				var administration = true;
				var forCredits = false;

				Ext.create('CMDBuild.core.Data'); // Data connections configuration
				Ext.create('CMDBuild.core.Rest'); // Setup REST connection
				Ext.create('CMDBuild.core.configurationBuilders.UserInterface'); // CMDBuild UserInterface configuration

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

				/**
				 * Maybe a single request with all the configuration could be better
				 *
				 * TODO: use new implementation of CMDBuild.configuration.userInterface
				 */
				CMDBuild.core.proxy.group.Group.getUIConfiguration({
					scope: this,
					success: function(result, options, decodedResult) {
						_CMUIConfiguration = new CMDBuild.model.CMUIConfigurationModel(decodedResult.response);

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
									Ext.create('CMDBuild.view.administration.tasks.CMTasks', {
										cmControllerType: 'CMDBuild.controller.administration.tasks.CMTasksController',
										cmName: 'tasks'
									}),
									Ext.create('CMDBuild.view.administration.email.EmailView', {
										cmControllerType: 'CMDBuild.controller.administration.email.Email',
										cmName: 'email'
									}),
									Ext.create('CMDBuild.view.administration.localizations.MainPanel', {
										cmControllerType: 'CMDBuild.controller.administration.localizations.Main',
										cmName: 'localizations'
									}),
									new CMDBuild.view.administration.filter.CMGroupFilterPanel({
										cmControllerType: controllerNS.administration.filter.CMGroupFilterPanelController,
										cmName: 'groupfilter'
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

								if (!_CMUIConfiguration.isCloudAdmin()) {
									dataViewAccordion = new CMDBuild.view.administration.accordion.CMDataViewAccordion();

									panels = panels.concat([
										new CMDBuild.view.administration.dataview.CMSqlDataView({
											cmControllerType: controllerNS.administration.dataview.CMSqlDataViewController,
											cmName: 'sqldataview'
										}),
										new CMDBuild.view.administration.dataview.CMFilterDataView({
											cmControllerType: controllerNS.administration.dataview.CMFilerDataViewController,
											cmName: 'filterdataview'
										})
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
							Ext.create('CMDBuild.view.administration.accordion.CMFilterAccordion'),
							navigationTreesAccordion,
							lookupAccordion,
							dashboardsAccordion,
							reportAccordion,
							menuAccordion,
							groupsAccordion,
							Ext.create('CMDBuild.view.administration.accordion.Tasks'),
							Ext.create('CMDBuild.view.administration.accordion.Email'),
							gisAccordion,
							bimAccordion,
//							Ext.create('CMDBuild.view.administration.accordion.Localizations'), // TODO: will be implemented in future releases
							Ext.create('CMDBuild.view.administration.accordion.Configuration')
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

				/*
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

				/*
				 * Classes and process
				 */
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = false;

				CMDBuild.core.proxy.Classes.read({
					params: params,
					loadMask: false,
					scope: this,
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

				/*
				 * Workflow configuration
				 */
				CMDBuild.ServiceProxy.configuration.readWFConfiguration({
					success: function(response, options, decoded) {
						CMDBuild.Config.workflow = decoded.data;
						CMDBuild.Config.workflow.enabled = ('true' == CMDBuild.Config.workflow.enabled);
					},
					callback: reqBarrier.getCallback()
				});

				/*
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

				/*
				 * Lookups
				 */
				CMDBuild.ServiceProxy.lookup.readAllTypes({
					success: function(response, options, decoded) {
						_CMCache.addLookupTypes(decoded);
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
				CMDBuild.core.proxy.group.Group.readAll({
					scope: this,
					success: function(result, options, decodedResult) {
						_CMCache.addGroups(decodedResult.groups); // TODO: refactor to avoid cache usage

						groupsAccordion = Ext.create('CMDBuild.view.administration.accordion.UserAndGroup', {
							cmControllerType: 'CMDBuild.controller.administration.accordion.UserAndGroup',
							cmName: 'group'
						});
						groupsAccordion.updateStore();

						menuAccordion = new CMDBuild.view.administration.accordion.CMMenuAccordion({
							cmControllerType: CMDBuild.controller.accordion.CMMenuAccordionController
						});
						menuAccordion.updateStore();

						_CMMainViewportController.addPanel([
							new CMDBuild.Administration.ModMenu({
								cmControllerType: controllerNS.administration.menu.CMModMenuController
							}),
							Ext.create('CMDBuild.view.administration.group.GroupView', {
								cmControllerType: 'CMDBuild.controller.administration.group.Group',
								cmName: 'group',
							}),
							Ext.create('CMDBuild.view.administration.users.UsersView', {
								cmControllerType: 'CMDBuild.controller.administration.users.Users',
								cmName: 'users',
							})
						]);
					},
					callback: reqBarrier.getCallback()
				});

				/*
				 * Report
				 */
				CMDBuild.core.proxy.Report.getMenuTree({
					success: function(response, options, reports) {
						_CMCache.addReports(reports);

						reportAccordion = Ext.create('CMDBuild.view.administration.accordion.Report');
						reportAccordion.updateStore();

						_CMMainViewportController.addPanel(
							new CMDBuild.view.administration.report.CMModReport({
								cmControllerType: controllerNS.administration.report.CMModReportController
							})
						);
					},
					callback: reqBarrier.getCallback()
				});

				/*
				 * Domains
				 */
				CMDBuild.ServiceProxy.administration.domain.list({
					success: function(response, options, decoded) {
						_CMCache.addDomains(decoded.domains);

						if (!_CMUIConfiguration.isCloudAdmin()) {
							domainAccordion = new CMDBuild.view.administration.accordion.CMDomainAccordion({
								cmControllerType: CMDBuild.controller.accordion.CMDomainAccordionController
							});
							domainAccordion.updateStore();

							_CMMainViewportController.addPanel(
								new CMDBuild.view.administration.domain.CMModDomain({
									cmControllerType: controllerNS.administration.domain.CMModDomainController
								})
							);
						}
					},
					callback: reqBarrier.getCallback()
				});

				/*
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

				/*
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