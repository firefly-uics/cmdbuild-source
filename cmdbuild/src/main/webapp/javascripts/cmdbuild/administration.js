(function() {

	var controllerNS = CMDBuild.controller;
	var lookupAccordion = null, classesAccordion = null,
		dashboardsAccordion = null, groupsAccordion = null,
		menuAccordion = null, domainAccordion = null,
		reportAccordion = null, processAccordion = null,
		gisAccordion = null, dataViewAccordion = null,
		bimAccordion = null;

	var requestBarrier = null;
	var requests = [];

	Ext.define("CMDBuild.app.Administration", {
		constructor: function() {

			Ext.tip.QuickTipManager.init();

			// fix a problem of Ext 4.2 tooltips width
			// see http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text/page3#24
			delete Ext.tip.Tip.prototype.minWidth;

			// Show the splash screen
			var administration = true;
			var forCredits = false;
			CMDBuild.view.CMMainViewport.showSplash(forCredits, administration);
		},

		addRequest: function(requestor, config) {
			config.callback = barrier().getCallback();
			requests.push({
				requestor: requestor,
				config: config
			});
		},

		loadAllYouNeed: function() {
			addLegacyRequests(this);

			initAppLayout(this,
				function() {
					for (var i = 0; i < requests.length; i++) {
						var request = requests[i];
						request.requestor(request.config);
					}

					barrier().start();
				} //
			);
		}

	});

	/*
	 * Load the UI configuration at first,
	 * then load the main CMDBuild configuration,
	 * and at least build the layout container
	 * 
	 * maybe a single request with all the configuration could be better
	 */
	function initAppLayout(me, callback) {
		CMDBuild.ServiceProxy.group.getUIConfiguration({
			success: function(response, options,decoded) {
				_CMUIConfiguration = new CMDBuild.model.CMUIConfigurationModel(decoded.response);

				CMDBuild.ServiceProxy.configuration.readMainConfiguration({
					success : function(response, options, decoded) {
						CMDBuild.Config.cmdbuild = decoded.data;
						buildLayout(me, callback);
					}
				});
			}
		});
	};

	function barrier() {
		if (requestBarrier == null) {
			requestBarrier = new CMDBuild.Utils.CMRequestBarrier(
				function callback() {

					_CMMainViewportController.addAccordion([
						classesAccordion,
						processAccordion,
						domainAccordion,
						dataViewAccordion,
						new CMDBuild.view.administration.accordion.CMFilterAccordion(),
						lookupAccordion,
						dashboardsAccordion,
						reportAccordion,
						menuAccordion,
						groupsAccordion,
						gisAccordion,
						bimAccordion,
						new CMDBuild.view.administration.accordion.CMConfigurationAccordion()
					]);

					/* *********************************
					 * Resume here the layouts operations 
					 */
					Ext.resumeLayouts(true);
					/* *********************************/
					_CMMainViewportController.viewport.doLayout();

					CMDBuild.view.CMMainViewport.hideSplash(function() {
						_CMMainViewportController.setInstanceName(CMDBuild.Config.cmdbuild.instance_name);
						_CMMainViewportController.selectFirstSelectableLeafOfOpenedAccordion();
					});

				}
			);
		}

		return requestBarrier;
	}

	function buildLayout(me, callback) {

		/* **********************************************
		 * Suspend here the layouts, and resume after all
		 * the load are end
		 * **********************************************/
		Ext.suspendLayouts();
		/* ***********************************************/

		var panels = [
			new Ext.Panel({
				cls : 'empty_panel x-panel-body'
			}),
			new CMDBuild.view.administration.filter.CMGroupFilterPanel({
				cmControllerType: controllerNS.administration.filter.CMGroupFilterPanelController,
				cmName: "groupfilter"
			}),
			new CMDBuild.view.administration.configuration.CMModConfigurationGenericOption({
				cmControllerType : controllerNS.administration.configuration.CMModConfigurationController,
				cmName : "modsetupcmdbuild"
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
				cmControllerType : controllerNS.common.CMUnconfiguredModPanelController,
				cmName : "notconfiguredpanel"
			})
		];

		if (!_CMUIConfiguration.isCloudAdmin()) {
			dataViewAccordion = new CMDBuild.view.administration.accordion.CMDataViewAccordion();

			panels = panels.concat([
			new CMDBuild.view.administration.dataview.CMSqlDataView({
				cmControllerType : controllerNS.administration.dataview.CMSqlDataViewController,
				cmName : "sqldataview"
			}),
			new CMDBuild.view.administration.dataview.CMFilterDataView({
				cmControllerType : controllerNS.administration.dataview.CMFilerDataViewController,
				cmName : "filterdataview"
			}),
				new CMDBuild.view.administration.configuration.CMModConfigurationEmail({
					cmControllerType : controllerNS.administration.configuration.CMModConfigurationController,
					cmName : "modsetupemail"
				}),
				new CMDBuild.view.administration.configuration.CMModConfigurationGis({
					cmControllerType : controllerNS.administration.configuration.CMModConfigurationController,
					cmName : "modsetupgis"
				}),
				new CMDBuild.view.administration.configuration.CMModConfigurationGraph({
					cmControllerType : controllerNS.administration.configuration.CMModConfigurationController,
					cmName : "modsetupgraph"
				}),
				new CMDBuild.view.administration.configuration.CMModConfigurationAlfresco({
					cmControllerType : controllerNS.administration.configuration.CMModConfigurationController,
					cmName : "modsetupalfresco"
				}),
				new CMDBuild.view.administration.configuration.CMModConfigurationWorkflow({
					cmControllerType : controllerNS.administration.configuration.CMModConfigurationController,
					cmName : "modsetupworkflow"
				}),
				new CMDBuild.view.administration.configuration.CMModConfigurationServer({
					cmControllerType : controllerNS.administration.configuration.CMModConfigurationServerController,
					cmName : "modsetupserver"
				}),
				new CMDBuild.view.administration.configuration.CMModConfigurationBIM({
					cmControllerType : controllerNS.administration.configuration.CMModConfigurationController,
					cmName : "modsetupbim"
				})

			]);
		}

		_CMMainViewportController = new CMDBuild.controller.CMMainViewportController(new CMDBuild.view.CMMainViewport({
			cmAccordions: [],
			cmPanels: panels
		}));

		callback(me);
	};

	function addLegacyRequests(me) {

		/*
		 * Workflow configuration
		 * */
		me.addRequest( //
			CMDBuild.ServiceProxy.configuration.readWFConfiguration, //
			{
				success : function(response, options, decoded) {
					CMDBuild.Config.workflow = decoded.data;
					CMDBuild.Config.workflow.enabled = ('true' == CMDBuild.Config.workflow.enabled);
				}
			} //
		);

		/*
		 * GIS configuration
		 * */
		me.addRequest( //
			CMDBuild.ServiceProxy.configuration.readGisConfiguration, //
			{
				success : function(response, options, decoded) {
					CMDBuild.Config.gis = decoded.data;
					CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);

					if (!_CMUIConfiguration.isCloudAdmin()) {
						gisAccordion = new CMDBuild.view.administration.accordion.CMGISAccordion({
							disabled: !CMDBuild.Config.gis.enabled
						});

						_CMMainViewportController.addPanel([
							new CMDBuild.Administration.ModIcons(),
							new CMDBuild.view.administration.gis.CMModGISNavigationConfiguration({
								cmControllerType : controllerNS.administration.gis.CMModGISNavigationConfigurationController
							}),
							new CMDBuild.Administration.ModExternalServices(),
							new CMDBuild.view.administration.gis.CMModGeoServer({
								cmControllerType : controllerNS.administration.gis.CMModGeoServerController
							}),
							new CMDBuild.Administration.ModLayerOrder({
								cmControllerType : controllerNS.administration.gis.CMModLayerOrderController
							})
						]);
					}
				}
			} //
		);

		/*
		 * BIM Configuration
		 * */
		me.addRequest( //
			CMDBuild.ServiceProxy.configuration.readBimConfiguration,
			{
				success: function(response, option, decoded) {
					var disabled = decoded.data.enabled == "false";
					bimAccordion = new CMDBuild.view.administration.accordion.CMBIMAccordion({
						disabled: disabled
					});
				}
			}
		);

		/*
		 * Classes and process
		 */
		me.addRequest( //
			CMDBuild.ServiceProxy.classes.read, //
			{
				params : {
					active : false
				},
				success : function(response, options, decoded) {
					_CMCache.addClasses(decoded.classes);

					if (!_CMUIConfiguration.isCloudAdmin()) {
						classesAccordion = new CMDBuild.view.administration.accordion.CMClassAccordion({
							cmControllerType : CMDBuild.controller.accordion.CMClassAccordionController
						});
						classesAccordion.updateStore();

						processAccordion = new CMDBuild.view.administration.accordion.CMProcessAccordion({
							cmControllerType : CMDBuild.controller.accordion.CMProcessAccordionController,
							disabled: !CMDBuild.Config.workflow.enabled
						});
						processAccordion.updateStore();

						_CMMainViewportController.addPanel([
							new CMDBuild.view.administration.classes.CMModClass({
								cmControllerType : controllerNS.administration.classes.CMModClassController
							}),
							new CMDBuild.view.administration.workflow.CMModProcess({
								cmControllerType : controllerNS.administration.workflow.CMModProcessController
							})
						]);
					}

					// Do a separate request for the widgets because, at this time
					// it is not possible serialize them with the classes
					CMDBuild.ServiceProxy.CMWidgetConfiguration.read({
						callback: barrier().getCallback(),
						success : function(response, options, decoded) {
							_CMCache.addWidgetToEntryTypes(decoded.response);
						}
					});
				}
			} //
		);

		/*
		 * Lookups
		 * */
		me.addRequest( //
			CMDBuild.ServiceProxy.lookup.readAllTypes, //
			{
				success : function(response, options, decoded) {
					_CMCache.addLookupTypes(decoded);
					lookupAccordion = new CMDBuild.view.administration.accordion.CMLookupAccordion({
						cmControllerType : CMDBuild.controller.accordion.CMLookupAccordionController
					});
					lookupAccordion.updateStore();

					_CMMainViewportController.addPanel(
						new CMDBuild.Administration.ModLookup({
							cmControllerType : controllerNS.administration.lookup.CMModLookupController
						})
					);
				}
			} //
		);

		/*
		 * Groups
		 * */
		me.addRequest( //
			CMDBuild.ServiceProxy.group.read, //
			{
				success : function(response, options, decoded) {
					_CMCache.addGroups(decoded.groups);

					groupsAccordion = new CMDBuild.view.administration.accordion.CMGroupsAccordion({
						cmControllerType : CMDBuild.controller.accordion.CMGroupAccordionController
					});
					groupsAccordion.updateStore();

					menuAccordion = new CMDBuild.view.administration.accordion.CMMenuAccordion({
						cmControllerType : CMDBuild.controller.accordion.CMMenuAccordionController
					});
					menuAccordion.updateStore();

					_CMMainViewportController.addPanel([
						new CMDBuild.Administration.ModMenu({
							cmControllerType : controllerNS.administration.menu.CMModMenuController
						}),
						new CMDBuild.view.administration.group.CMModGroup({
							cmControllerType : controllerNS.administration.group.CMModGroupsController
						}),
						new CMDBuild.view.administration.user.CMModUser({
							cmControllerType : controllerNS.administration.user.CMModUserController
						})
					]);
				}
			} //
		);

		/*
		 * Report
		 * */
		me.addRequest( //
			CMDBuild.ServiceProxy.report.getMenuTree, //
			{
				success : function(response, options, reports) {
					_CMCache.addReports(reports);

					reportAccordion = new CMDBuild.view.common.report.CMReportAccordion();
					reportAccordion.updateStore();

					_CMMainViewportController.addPanel(
						new CMDBuild.view.administration.report.CMModReport({
							cmControllerType : controllerNS.administration.report.CMModReportController
						})
					);
				},
			} //
		);

		/*
		 * Domains
		 * */
		me.addRequest( //
			CMDBuild.ServiceProxy.administration.domain.list, //
			{
				success : function(response, options, decoded) {
					_CMCache.addDomains(decoded.domains);

					if (!_CMUIConfiguration.isCloudAdmin()) {
						domainAccordion = new CMDBuild.view.administration.accordion.CMDomainAccordion({
							cmControllerType : CMDBuild.controller.accordion.CMDomainAccordionController
						});
						domainAccordion.updateStore();

						_CMMainViewportController.addPanel(
							new CMDBuild.view.administration.domain.CMModDomain({
								cmControllerType : controllerNS.administration.domain.CMModDomainController
							})
						);
					}
				}
			} //
		);

		/*
		 * Dashboards
		 * */
		me.addRequest( //
			CMDBuild.ServiceProxy.Dashboard.fullList, //
			{
				success : function(response, options, decoded) {
					_CMCache.addDashboards(decoded.response.dashboards);
					_CMCache.setAvailableDataSources(decoded.response.dataSources);

					if (!_CMUIConfiguration.isCloudAdmin()) {
						dashboardsAccordion = new CMDBuild.view.administration.accordion.CMDashboardAccordion({
							cmControllerType : CMDBuild.controller.accordion.CMDashboardAccordionController
						});
						dashboardsAccordion.updateStore();

						_CMMainViewportController.addPanel(
							new CMDBuild.view.administration.dashboard.CMModDashboard({
								cmControllerType : controllerNS.administration.dashboard.CMModDashboardController
							})
						);
					}
				}
			}
		);
	}
})();
