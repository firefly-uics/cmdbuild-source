(function() {
	var lookupAccordion = new CMDBuild.view.administraton.accordion.CMLookupAccordion({
		cmControllerType: CMDBuild.controller.accordion.CMLookupAccordionController
	});
	
	
Ext.define("CMDBuild.app.Administration", {
	statics: {
		init: function() {
			CMDBuild.ServiceProxy.lookup.readAllTypes({
				success: function(response, options, decoded) {
					CMDBuild.Cache.setTables(decoded);
					lookupAccordion.updateStore();
				}
			});
	
			CMDBuild.ServiceProxy.configuration.readMainConfiguration({
				success: function(response, options, decoded) {
					CMDBuild.Config.cmdbuild = decoded.data;
					
					CMDBuild.ServiceProxy.configuration.readWFConfiguration({
						success: function(response, options, decoded) {
							CMDBuild.Config.workflow = decoded.data;
							CMDBuild.Config.workflow.enabled = ('true' == CMDBuild.Config.workflow.enabled);
							
							CMDBuild.ServiceProxy.configuration.readGisConfiguration({
								success: function(response, options, decoded) {
									CMDBuild.Config.gis = decoded.data;
									CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);
									
									CMDBuild.ServiceProxy.classes.read({
										params: {
											active: false
										},
										success: function(response, options, decoded) {
											CMDBuild.Cache.setTables(decoded.classes);
												
											CMDBuild.ServiceProxy.group.read({
												success: function(response, options, decoded) {
													CMDBuild.Cache.setTables(decoded.groups);
													
													CMDBuild.ServiceProxy.menu.read({
														success: function(response, options, decoded) {
															CMDBuild.Cache.setTables(decoded);
															renderThemAll();
														}
													});
												}
											});
										}
									});
								}
							});
						}
					});
				}
			});
		}
	}
});
function renderThemAll() {
	var viewNS = CMDBuild.view.administration;
	var controllerNS = CMDBuild.controller;
	
	CMDBuild.log.info("Init administration");
	_CMMainViewportController = 
		new CMDBuild.controller.CMMainViewportController(
			new CMDBuild.view.CMMainViewport({
				cmAccordions: [
					lookupAccordion,
					new CMDBuild.view.administraton.accordion.CMGISAccordion(),
					new CMDBuild.view.administraton.accordion.CMConfigurationAccordion()
				],

				cmPanels: [
					new Ext.Panel({
						cls: 'empty_panel x-panel-body'
					}),
					new CMDBuild.Administration.ModLookup({
						cmControllerType: controllerNS.administration.lookup.CMModLookupController
					}),
					new CMDBuild.Administration.ModIcons(),
					new CMDBuild.Administration.ModExternalServices(),
					
					new CMDBuild.view.administration.gis.CMModGeoServer({
						cmControllerType: controllerNS.administration.gis.CMModGeoServerController
					}),
					new CMDBuild.Administration.ModLayerOrder(),
					
					new CMDBuild.view.common.CMUnconfiguredModPanel({
						cmControllerType: controllerNS.common.CMUnconfiguredModPanelController,
						cmName: "notconfiguredpanel"
					}),
					
					new CMDBuild.view.administration.configuration.CMModConfigurationGenericOption({
						cmControllerType: controllerNS.administration.configuration.CMModConfigurationController,
						cmName: "modsetupcmdbuild"
					}),
					new CMDBuild.view.administration.configuration.CMModConfigurationEmail({
						cmControllerType: controllerNS.administration.configuration.CMModConfigurationController,
						cmName: "modsetupemail"
					}),
					new CMDBuild.view.administration.configuration.CMModConfigurationGis({
						cmControllerType: controllerNS.administration.configuration.CMModConfigurationController,
						cmName: "modsetupgis"
					}),
					new CMDBuild.view.administration.configuration.CMModConfigurationGraph({
						cmControllerType: controllerNS.administration.configuration.CMModConfigurationController,
						cmName: "modsetupgraph"
					}),
					new CMDBuild.view.administration.configuration.CMModConfigurationAlfresco({
						cmControllerType: controllerNS.administration.configuration.CMModConfigurationController,
						cmName: "modsetupalfresco"
					}),
					new CMDBuild.view.administration.configuration.CMModConfigurationWorkflow({
						cmControllerType: controllerNS.administration.configuration.CMModConfigurationController,
						cmName: "modsetupworkflow"
					}),
					new CMDBuild.view.administration.configuration.CMModConfigurationServer({
						cmControllerType: controllerNS.administration.configuration.CMModConfigurationServerController,
						cmName: "modsetupserver"
					})
				]
			})
	);
}

})();


//Ext.QuickTips.init();
//CMDBuild.InitHeader();
//
//CMDBuild.identifiers = {};
//
//var splash = new CMDBuild.Splash('splashScreen','splashScreen');
//splash.setText(CMDBuild.Translation.common.loading_mask.configuration);
//splash.show();
//
//CMDBuild.ConcurrentAjax.execute({
//	loadMask: false,
//	requests: [{
//		url: 'services/json/schema/setup/getconfiguration',
//		params: { name: 'cmdbuild' },
//		maskMsg: CMDBuild.Translation.common.loading_mask.configuration,
//		success: function(response, options, decoded) {
//			CMDBuild.Config.cmdbuild = decoded.data;
//		}
//	},{
//        url: 'services/json/schema/setup/getconfiguration',
//        params: { name: 'workflow' },
//        success: function(response, options, decoded) {
//            CMDBuild.Config.workflow = decoded.data;
//            CMDBuild.Config.workflow.enabled = ('true' == CMDBuild.Config.workflow.enabled);
//        }
//    },{
//        url: 'services/json/schema/setup/getconfiguration',
//        params: { name: 'gis' },
//        success: function(response, options, decoded) {
//            splash.setText(CMDBuild.Translation.common.loading_mask.classes);
//            CMDBuild.Config.gis = decoded.data;
//            CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);
//        }
//    }
//     ,{
//		url: "services/json/schema/modclass/getallclasses",
//		params: {
//			active: false
//		},
//		maskMsg: CMDBuild.Translation.common.loading_mask.classes,
//		success: function(response, options, decoded) {
//			splash.setText(CMDBuild.Translation.common.loading_mask.classes);
//			CMDBuild.Cache.setTables(decoded.classes);
//		}
// 		},{
//		url: "services/json/schema/modclass/getalldomains",
//		maskMsg: CMDBuild.Translation.common.loading_mask.domain,
//		success: function(response, options, decoded) {
//			splash.setText(CMDBuild.Translation.common.loading_mask.domain);
////			CMDBuild.Cache.setDomains(decoded.domains);
//		}
//	},{
//    	url: 'services/json/schema/modlookup/tree',
//    	maskMsg: CMDBuild.Translation.common.loading_mask.lookup,
//        success: function(response, options, decoded) {
//			CMDBuild.Cache.setTables(decoded);
//        }
//    },{
//    	url: 'services/json/schema/modreport/menutree',
//    	maskMsg: CMDBuild.Translation.common.loading_mask.report,
//        success: function(response, options, decoded) {
//    		splash.setText(CMDBuild.Translation.common.loading_mask.menu);
//    		CMDBuild.Cache.setTables(decoded);
//        }
//    },{
//    	url: 'services/json/schema/modsecurity/getgrouplist',
//    	maskMsg: CMDBuild.Translation.common.loading_mask.menu,
//        success: function(response, options, decoded) {
//    		CMDBuild.Cache.setTables(decoded.groups);
//    		splash.setText(CMDBuild.Translation.common.loading_mask.group);
//        }
//    }],
//	fn: function() {
//		displayViewport();
//	}
//});
//
//function displayViewport() {
//	var domainTree = new CMDBuild.administration.domain.CMDomainAccordion({
//		eventType: "domain",
//		controllerType: "CMDomainAccordionController"
//	});
//
//	CMDBuild.identifiers.accordion = {
//		domain: domainTree.id
//	};
//
//	var viewport = new CMDBuild.MainViewport({
//		colorsConst: CMDBuild.Constants.colors.gray,
//		controllerType: "AdminViewportController",
//		trees: [
//			new CMDBuild.Administration.ClassTree({
//				controllerType: "ClassTreePanelController"
//			}),
//			new CMDBuild.Administration.WorkflowTree({
//				eventType: "processclass",
//				controllerType: "WorkflowTreePanelController"
//			}),
//			domainTree,
//			new CMDBuild.TreePanel({
//				border: false,
//				rootVisible: false,
//				title: CMDBuild.Translation.administration.modLookup.lookupTypes,
//				root: CMDBuild.TreeUtility.getTree(CMDBuild.Constants.cachedTableType.lookuptype,
//						undefined, undefined, sorted=true),
//				eventType: CMDBuild.Constants.cachedTableType.lookuptype,
//				controllerType: "LookupTreePanelController"
//			}),
//			new CMDBuild.TreePanel({
//				border: false,
//				rootVisible: true,
//				title: CMDBuild.Translation.administration.modreport.title,
//				root: CMDBuild.TreeUtility.getTree(CMDBuild.Constants.cachedTableType.report,
//						undefined, undefined, sorted=true),
//				eventType: CMDBuild.Constants.cachedTableType.report
//			}),
//			new CMDBuild.Administration.MenuTree({
//				eventType: CMDBuild.Constants.cachedTableType.group,
//				controllerType: "MenuTreePanelController"
//			}),
//			new CMDBuild.Administration.SecurityTree({
//				eventType: CMDBuild.Constants.cachedTableType.group,
//				controllerType: "SecurityTreePanelController"
//			}),
//			new CMDBuild.Administration.GisTree({
//				controllerType: "GisTreePanelController"
//			}),
//			new CMDBuild.Administration.SetupTree()
//		],
//		modules: [
//			new CMDBuild.administration.domain.ModDomain(),
//			new CMDBuild.Administration.ModClass(),
//			new CMDBuild.Administration.ModWorkflow(),
//			new CMDBuild.Administration.ModLookup(),
//			new CMDBuild.Administration.ModReport(),
//			new CMDBuild.Administration.ModMenu(),
//			new CMDBuild.Administration.ModSecurity(),
//			new CMDBuild.Administration.ModUser(),
//			new CMDBuild.Administration.ModSetupGenericOption(),
//			new CMDBuild.Administration.ModSetupGraph(),
//			new CMDBuild.Administration.ModLegacydms(),
//			new CMDBuild.Administration.ModIcons(),
//			new CMDBuild.Administration.ModLayerOrder(),
//			new CMDBuild.Administration.ModGeoServer(),
//			new CMDBuild.Administration.ModExternalServices(),
//			new CMDBuild.Administration.ModSetupServer(),
//			new CMDBuild.Administration.ModSetupWorkflow(),
//			new CMDBuild.Administration.ModSetupEmail(),
//			new CMDBuild.Administration.ModSetupGis(),
//			new CMDBuild.UnconfiguredModPanel()
//		]
//	})
//	
//	new CMDBuild.administration.CMMainController(viewport);
//	
//	splash.hide();
//	
//	var creditsLink = Ext.get('cmdbuild_credits_link');
//	creditsLink.on('click', function(e) {
//		splash.showAsPopUp();
//	}, this);
//	
//	var instanceName = Ext.get('instance_name');
//	instanceName.dom.innerHTML = CMDBuild.Config.cmdbuild.instance_name || "";
//};
//});