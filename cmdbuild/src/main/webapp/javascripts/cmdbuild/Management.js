(function() {

	var reportAccordion = Ext.create('CMDBuild.view.management.accordion.Report', { cmName: 'report' });

	// TODO move in common
	var menuAccordion = new CMDBuild.view.administration.accordion.CMMenuAccordion({
		cmControllerType: CMDBuild.controller.management.menu.CMMenuAccordionController
	});

	// TODO move in common
	var classesAccordion = new CMDBuild.view.common.classes.CMClassAccordion({
		title: CMDBuild.Translation.administration.modClass.tree_title
	});
	// TODO move in common
	var processAccordion = new CMDBuild.view.administration.accordion.CMProcessAccordion({
		rootVisible: true
	});
	// TODO move in common
	var dashboardsAccordion = new CMDBuild.view.administration.accordion.CMDashboardAccordion();

	var dataViewAccordion = new CMDBuild.view.management.dataView.CMDataViewAccordion({
		cmControllerType: CMDBuild.controller.management.common.CMFakeIdAccordionController
	});

	Ext.define('CMDBuild.app.Management', {
		extend: 'Ext.app.Application',

		requires: [
			'Ext.ux.Router',
			'CMDBuild.routes.management.Cards',
			'CMDBuild.routes.management.Classes',
			'CMDBuild.routes.management.Instances',
			'CMDBuild.routes.management.Processes',
			'CMDBuild.core.buttons.Buttons',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Configuration',
			'CMDBuild.core.proxy.group.Group',
			'CMDBuild.core.proxy.Report'
		],

		name: 'CMDBuild',
		appFolder: './javascripts/cmdbuild',

		routes: {
			// Classes
			'classes/:classIdentifier/cards': 'CMDBuild.routes.management.Classes#saveRoute', // Alias (wrong implementation, to delete in future)
			'classes/:classIdentifier/cards/': 'CMDBuild.routes.management.Classes#saveRoute',
			'classes/:classIdentifier/print': 'CMDBuild.routes.management.Classes#saveRoute',

			'exec/classes/:classIdentifier/cards': 'CMDBuild.routes.management.Classes#detail', // Alias (wrong implementation, to delete in future)
			'exec/classes/:classIdentifier/cards/': 'CMDBuild.routes.management.Classes#detail',
			'exec/classes/:classIdentifier/print': 'CMDBuild.routes.management.Classes#print',

			// Cards
			'classes/:classIdentifier/cards/:cardIdentifier': 'CMDBuild.routes.management.Cards#saveRoute', // Alias (wrong implementation, to delete in future)
			'classes/:classIdentifier/cards/:cardIdentifier/': 'CMDBuild.routes.management.Cards#saveRoute',
			'classes/:classIdentifier/cards/:cardIdentifier/print': 'CMDBuild.routes.management.Cards#saveRoute',

			'exec/classes/:classIdentifier/cards/:cardIdentifier': 'CMDBuild.routes.management.Cards#detail', // Alias (wrong implementation, to delete in future)
			'exec/classes/:classIdentifier/cards/:cardIdentifier/': 'CMDBuild.routes.management.Cards#detail',
			'exec/classes/:classIdentifier/cards/:cardIdentifier/print': 'CMDBuild.routes.management.Cards#print',

			// Processes
			'processes/:processIdentifier/instances/': 'CMDBuild.routes.management.Processes#saveRoute',
			'processes/:processIdentifier/print': 'CMDBuild.routes.management.Processes#saveRoute',
			'processes/': 'CMDBuild.routes.management.Processes#saveRoute',

			'exec/processes/:processIdentifier/instances/': 'CMDBuild.routes.management.Processes#detail',
			'exec/processes/:processIdentifier/print': 'CMDBuild.routes.management.Processes#print',
			'exec/processes/': 'CMDBuild.routes.management.Processes#showAll',

			// Instances
			'processes/:processIdentifier/instances/:instanceIdentifier/': 'CMDBuild.routes.management.Instances#saveRoute',

			'exec/processes/:processIdentifier/instances/:instanceIdentifier/': 'CMDBuild.routes.management.Instances#detail',
		},

		statics: {
			init: function() {
				Ext.create('CMDBuild.core.Data'); // Data connections configuration
				Ext.create('CMDBuild.core.Rest'); // Setup REST connection
				Ext.create('CMDBuild.core.configurationBuilders.UserInterface'); // CMDBuild UserInterface configuration

				Ext.tip.QuickTipManager.init();
				// Fix a problem of Ext 4.2 tooltips width
				// See http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text/page3#24
				delete Ext.tip.Tip.prototype.minWidth;

				var me = this;
				var cb = function() {
					me.buildComponents();
				}

				CMDBuild.view.CMMainViewport.showSplash();

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

						CMDBuild.ServiceProxy.configuration.readAll({
							success: function(response, options, decoded) {
								// Cmdbuild
								CMDBuild.Config.cmdbuild = decoded.cmdbuild;

								// Localization
								CMDBuild.Config[CMDBuild.core.proxy.CMProxyConstants.LOCALIZATION].setLanguagesWithLocalizations(decoded.cmdbuild.enabled_languages);

								// DMS
								CMDBuild.Config.dms = decoded.dms;
								CMDBuild.Config.dms.enabled = ('true' == CMDBuild.Config.dms.enabled);

								// Bim
								CMDBuild.Config.bim = decoded.bim;
								CMDBuild.Config.bim.enabled = ('true' == CMDBuild.Config.bim.enabled);

								// Graph
								CMDBuild.Config.graph = decoded.graph;

								// Workflow
								CMDBuild.Config.workflow = decoded.workflow;

								// Gis
								CMDBuild.Config.gis = decoded.gis;
								CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);

								// Gis and bim extra configuration
								CMDBuild.Config.cmdbuild.cardBrowserByDomainConfiguration = {};
								CMDBuild.ServiceProxy.gis.getGisTreeNavigation({
									success: function(operation, config, response) {
										CMDBuild.Config.cmdbuild.cardBrowserByDomainConfiguration.root = response.root;
										CMDBuild.Config.cmdbuild.cardBrowserByDomainConfiguration.geoServerLayersMapping = response.geoServerLayersMapping;

										if (CMDBuild.Config.bim.enabled) {
											CMDBuild.bim.proxy.rootClassName({
												success: function(operation, config, response) {
													CMDBuild.Config.bim.rootClass = response.root;
												},
												callback: cb
											});
										} else {
											cb();
										}
									}
								});

							}
						});
					}
				});
			},

			buildComponents: function() {
				/* **********************************************
				 * Suspend here the layouts, and resume after all
				 * the load are end
				 * **********************************************/
				Ext.suspendLayouts();
				/* ***********************************************/

				this.cmAccordions = [
					this.menuAccordion = menuAccordion
				];

				this.cmPanels = [
					Ext.create('Ext.panel.Panel'),
					this.cardPanel = new CMDBuild.view.management.classes.CMModCard({
						cmControllerType: CMDBuild.controller.management.classes.CMModCardController
					}),
					this.processPanel = new CMDBuild.view.management.workflow.CMModProcess({
						cmControllerType: CMDBuild.controller.management.workflow.CMModWorkflowController
					}),
					Ext.create('CMDBuild.view.management.report.ReportView', {
						cmControllerType: 'CMDBuild.controller.management.report.Report',
						cmName: 'report'
					}),
					Ext.create('CMDBuild.view.management.report.SingleReportPanel', {
						cmControllerType: 'CMDBuild.controller.management.report.SingleReport',
						cmName: 'singlereport'
					}),
					Ext.create('CMDBuild.view.management.customPage.SinglePagePanel', {
						cmControllerType: 'CMDBuild.controller.management.customPage.SinglePage',
						cmName: 'custompage'
					}),
					this.dashboardPanel = new CMDBuild.view.management.dashboard.CMModDashboard({
						cmControllerType: CMDBuild.controller.management.dashboard.CMModDashboardController
					}),
					this.dataViewPanel = Ext.create('CMDBuild.view.management.dataView.DataViewView', {
						cmControllerType: 'CMDBuild.controller.management.dataView.DataView',
						cmName: 'dataView'
					})
				];

				if (!_CMUIConfiguration.isModuleDisabled(classesAccordion.cmName)) {
					this.classesAccordion = classesAccordion;
					this.cmAccordions.push(this.classesAccordion);
				}

				if (!_CMUIConfiguration.isModuleDisabled(processAccordion.cmName) && CMDBuild.Config.workflow.enabled == 'true') {
					this.processAccordion = processAccordion;
					this.cmAccordions.push(this.processAccordion);
				}
				if (!_CMUIConfiguration.isModuleDisabled(dataViewAccordion.cmName)) {
					this.dataViewAccordion = dataViewAccordion;
					this.cmAccordions.push(this.dataViewAccordion);
				}

				if (!_CMUIConfiguration.isModuleDisabled(dashboardsAccordion.cmName)) {
					this.dashboardsAccordion = dashboardsAccordion;
					this.cmAccordions.push(this.dashboardsAccordion);
				}

				if (!_CMUIConfiguration.isModuleDisabled(reportAccordion.cmName)) {
					this.reportAccordion = reportAccordion;
					this.cmAccordions.push(this.reportAccordion);
				}

				if (!_CMUIConfiguration.isModuleDisabled(CMDBuild.core.proxy.CMProxyConstants.CUSTOM_PAGES)) {
					this.cmAccordions.push(Ext.create('CMDBuild.view.management.accordion.CustomPage', { cmName: 'custompage' }));
				}

				this.utilitiesTree = new CMDBuild.administration.utilities.UtilitiesAccordion({ // TODO move in common
					title: CMDBuild.Translation.management.modutilities.title
				});

				if (this.utilitiesTree.getRootNode().childNodes.length > 0)
					this.cmAccordions.push(this.utilitiesTree);

				for (var moduleName in this.utilitiesTree.submodules) {
					var cmName = this.utilitiesTree.getSubmoduleCMName(moduleName);

					if (!_CMUIConfiguration.isModuleDisabled(cmName))
						addUtilitySubpanel(cmName, this.cmPanels);
				}

				this.loadResources();

				if (_CMUIConfiguration.isFullScreenMode())
					_CMUIState.onlyGrid();
			},

			loadResources: function() {
				_CMCache.syncAttachmentCategories();

				var me = this;
				var params = {};
				var reqBarrier = new CMDBuild.Utils.CMRequestBarrier(function callback() {
					hideIfEmpty(processAccordion);
					hideIfEmpty(reportAccordion);
					hideIfEmpty(menuAccordion);
					hideIfEmpty(classesAccordion);

					_CMMainViewportController = new CMDBuild.controller.CMMainViewportController(
						new CMDBuild.view.CMMainViewport({
							cmAccordions: me.cmAccordions,
							cmPanels: me.cmPanels,
							hideAccordions: _CMUIConfiguration.isHideSidePanel()
						})
					);

					/* *********************************
					 * Resume here the layouts operations
					 */
					Ext.resumeLayouts(true);
					/* *********************************/

					_CMMainViewportController.viewport.doLayout();

					CMDBuild.view.CMMainViewport.hideSplash(function() {
						_CMMainViewportController.setInstanceName(CMDBuild.Config.cmdbuild.instance_name);

						// Execute routes
						CMDBuild.routes.Routes.exec();

						_CMMainViewportController.selectStartingClass();
					});
				});

				params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = true;

				CMDBuild.core.proxy.Classes.read({
					params: params,
					loadMask: false,
					scope: this,
					success: function(response, options, decoded) {
						_CMCache.addClasses(decoded.classes);
						classesAccordion.updateStore();
						processAccordion.updateStore();

						CMDBuild.ServiceProxy.CMWidgetConfiguration.read({
							scope: this,
							success: function(response, options, decoded) {
								// A day I'll can do a request to have only the active, now the cache discards the inactive if the flag onlyActive is true
								_CMCache.addWidgetToEntryTypes(decoded.response, onlyActive = true);
							},
							callback: reqBarrier.getCallback()
						});

						// To fill the menu is needed that the classes are already loaded
						params = {};
						params[CMDBuild.core.proxy.CMProxyConstants.GROUP_NAME] = CMDBuild.Runtime.DefaultGroupName;

						CMDBuild.ServiceProxy.menu.read({
							params: params,
							success: function(response, options, decoded) {
								menuAccordion.updateStore(decoded.menu);
							},
							callback: reqBarrier.getCallback()
						});

						_CMProxy.dataView.read({
							success: function(response, options, decoded) {
								dataViewAccordion.updateStore(decoded.views);
							},
							callback: reqBarrier.getCallback()
						});
					},
					failure: function() {
						_CMCache.addClasses([]);
						classesAccordion.updateStore();
						processAccordion.updateStore();
					},
					callback: reqBarrier.getCallback()
				});

				CMDBuild.core.proxy.Report.getTypesTree({
					scope: this,
					success: function(response, options, reports) {
						_CMCache.addReports(reports);
						reportAccordion.updateStore();
					},
					callback: reqBarrier.getCallback()
				});

				params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = true;

				CMDBuild.ServiceProxy.administration.domain.list({ //TODO change 'administration'
					params: params,
					success: function(response, options, decoded) {
						_CMCache.addDomains(decoded.domains);
					},
					callback: reqBarrier.getCallback()
				});

				CMDBuild.ServiceProxy.Dashboard.fullList({
					success : function(response, options, decoded) {
						_CMCache.addDashboards(decoded.response.dashboards);
						_CMCache.setAvailableDataSources(decoded.response.dataSources);
						dashboardsAccordion.updateStore();
					},
					callback: reqBarrier.getCallback()
				});

				CMDBuild.ServiceProxy.lookup.readAllTypes({
					success : function(response, options, decoded) {
						_CMCache.addLookupTypes(decoded);
					},
					callback: reqBarrier.getCallback()
				});

				reqBarrier.start();
			}
		}
	});

	Ext.application('CMDBuild.app.Management');

	function hideIfEmpty(a) {
		if (a.isEmpty()) {
			a.disable();
			a.hide();
		}
	}

	function addUtilitySubpanel(cmName, panels) {
		var builders = {
			changepassword : function() {
				return new CMDBuild.view.management.utilities.CMModChangePassword();
			},
			bulkcardupdate : function() {
				return new CMDBuild.view.management.utilites.CMModBulkCardUpdate({
					cmControllerType: CMDBuild.controller.management.utilities.CMModBulkUpdateController
				});
			},
			importcsv : function() {
				return new CMDBuild.view.management.utilities.CMModImportCSV({
					cmControllerType: CMDBuild.controller.management.utilities.CMModImportCSVController
				});
			},
			exportcsv : function() {
				return new CMDBuild.view.management.utilities.CMModExportCSV();
			}
		};

		if (typeof builders[cmName] == 'function')
			panels.push(builders[cmName]());
	}

})();