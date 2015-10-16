(function() {

	var reportAccordion = Ext.create('CMDBuild.view.management.accordion.Reports', { cmName: 'report' });
	var menuAccordion = Ext.create('CMDBuild.view.management.accordion.Menu', {
		cmControllerType: 'CMDBuild.controller.management.accordion.Menu',
		cmName: 'menu',
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

	Ext.define('CMDBuild.app.Management', {
		extend: 'Ext.app.Application',

		requires: [
			'Ext.ux.Router',
			'CMDBuild.routes.management.Cards',
			'CMDBuild.routes.management.Classes',
			'CMDBuild.routes.management.Instances',
			'CMDBuild.routes.management.Processes',
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.Configuration',
			'CMDBuild.core.proxy.dataView.DataView',
			'CMDBuild.core.proxy.domain.Domain',
			'CMDBuild.core.proxy.userAndGroup.group.Group',
			'CMDBuild.core.proxy.lookup.Type',
			'CMDBuild.core.proxy.Menu',
			'CMDBuild.core.proxy.report.Report'
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
				Ext.create('CMDBuild.core.LoggerManager'); // Logger configuration
				Ext.create('CMDBuild.core.Data'); // Data connections configuration
				Ext.create('CMDBuild.core.Rest'); // Setup REST connection
				Ext.create('CMDBuild.core.configurationBuilders.Instance'); // CMDBuild instance configuration
				Ext.create('CMDBuild.core.configurationBuilders.Localization'); // CMDBuild localization configuration
				Ext.create('CMDBuild.core.configurationBuilders.UserInterface'); // CMDBuild UserInterface configuration

				Ext.tip.QuickTipManager.init();
				// Fix a problem of Ext 4.2 tooltips width
				// See http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text/page3#24
				delete Ext.tip.Tip.prototype.minWidth;

				CMDBuild.view.CMMainViewport.showSplash();

				CMDBuild.core.proxy.Configuration.readAll({
					success: function(response, options, decoded) {
						/**
						 * CMDBuild
						 *
						 * @deprecated
						 */
						CMDBuild.Config.cmdbuild = decoded.cmdbuild;

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
										callback: CMDBuild.app.Management.buildComponents
									});
								} else {
									CMDBuild.app.Management.buildComponents();
								}
							}
						});
					}
				});
			},

			buildComponents: function() {
				Ext.suspendLayouts(); // Suspend here the layouts, and resume after all the load are end

				_CMCache.syncAttachmentCategories();

				var params = {};
				var reqBarrier = Ext.create('CMDBuild.core.RequestBarrier', {
					callback: function() {
						hideIfEmpty(processAccordion);
						hideIfEmpty(reportAccordion);
						hideIfEmpty(menuAccordion);
						hideIfEmpty(classesAccordion);

						_CMMainViewportController = new CMDBuild.controller.CMMainViewportController(
							new CMDBuild.view.CMMainViewport({
								cmAccordions: [ // Sorted
									menuAccordion,
									CMDBuild.configuration.userInterface.isDisabledModule('class') ? null : classesAccordion,
									CMDBuild.configuration.userInterface.isDisabledModule('process') || !(CMDBuild.Config.workflow.enabled == 'true') ? null : processAccordion,
									CMDBuild.configuration.userInterface.isDisabledModule(CMDBuild.core.constants.Proxy.DATA_VIEW) ? null :
										Ext.create('CMDBuild.view.management.accordion.DataView', {
											cmControllerType: 'CMDBuild.controller.management.accordion.DataView',
											cmName: 'dataview'
										})
									,
									CMDBuild.configuration.userInterface.isDisabledModule('dashboard') ? null : dashboardsAccordion,
									CMDBuild.configuration.userInterface.isDisabledModule('report') ? null : reportAccordion,
									CMDBuild.configuration.userInterface.isDisabledModule(CMDBuild.core.constants.Proxy.CUSTOM_PAGES) ? null :
										Ext.create('CMDBuild.view.management.accordion.CustomPage', {
											cmControllerType: 'CMDBuild.controller.common.AbstractAccordionController',
											cmName: 'custompage'
										})
									,
									Ext.create('CMDBuild.view.management.accordion.Utility', {
										cmControllerType: 'CMDBuild.controller.common.AbstractAccordionController',
										cmName: 'utility'
									})
								],
								cmPanels: [
									Ext.create('Ext.panel.Panel'),
									Ext.create('CMDBuild.view.management.customPage.SinglePagePanel', {
										cmControllerType: 'CMDBuild.controller.management.customPage.SinglePage',
										cmName: 'custompage'
									}),
									Ext.create('CMDBuild.view.management.dataView.DataViewView', {
										cmControllerType: 'CMDBuild.controller.management.dataView.DataView',
										cmName: 'dataview'
									}),
									Ext.create('CMDBuild.view.management.report.ReportView', {
										cmControllerType: 'CMDBuild.controller.management.report.Report',
										cmName: 'report'
									}),
									Ext.create('CMDBuild.view.management.report.SingleReportPanel', {
										cmControllerType: 'CMDBuild.controller.management.report.SingleReport',
										cmName: 'singlereport'
									}),
									this.cardPanel = new CMDBuild.view.management.classes.CMModCard({
										cmControllerType: CMDBuild.controller.management.classes.CMModCardController
									}),
									this.processPanel = new CMDBuild.view.management.workflow.CMModProcess({
										cmControllerType: CMDBuild.controller.management.workflow.CMModWorkflowController
									}),
									this.dashboardPanel = new CMDBuild.view.management.dashboard.CMModDashboard({
										cmControllerType: CMDBuild.controller.management.dashboard.CMModDashboardController
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
							})
						);

						Ext.resumeLayouts(true); // Resume here the layouts operations

						_CMMainViewportController.viewport.doLayout();

						CMDBuild.view.CMMainViewport.hideSplash(function() {
							_CMMainViewportController.setInstanceName(CMDBuild.Config.cmdbuild.instance_name);

							// Execute routes
							CMDBuild.routes.Routes.exec();

							_CMMainViewportController.selectStartingClass();
						});
					}
				});

				/**
				 * Classes
				 */
				params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.core.proxy.Classes.readAll({
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
						params[CMDBuild.core.constants.Proxy.GROUP_NAME] = CMDBuild.Runtime.DefaultGroupName;
						params[CMDBuild.core.constants.Proxy.LOCALIZED] = true;

						CMDBuild.core.proxy.Menu.read({
							params: params,
							scope: this,
							loadMask: false,
							success: function(response, options, decodedResponse) {
								menuAccordion.updateStore(decodedResponse.menu);
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

				/**
				 * Domains
				 */
				params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.core.proxy.domain.Domain.readAll({
					params: params,
					loadMask: false,
					scope: this,
					success: function(response, options, decodedResponse) {
						_CMCache.addDomains(decodedResponse.domains);
					},
					callback: reqBarrier.getCallback()
				});

				/**
				 * Lookup
				 */
				CMDBuild.core.proxy.lookup.Type.readAll({
					loadMask: false,
					success: function(response, options, decodedResponse) {
						_CMCache.addLookupTypes(decodedResponse);
 					},
 					callback: reqBarrier.getCallback()
 				});

				/**
				 * Reports
				 */
				CMDBuild.core.proxy.report.Report.getTypesTree({
					scope: this,
					success: function(response, options, decodedResponse) {
						_CMCache.addReports(decodedResponse);

						reportAccordion.updateStore();
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

				reqBarrier.start();

				if (CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.FULL_SCREEN_MODE))
					_CMUIState.onlyGrid();
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

})();