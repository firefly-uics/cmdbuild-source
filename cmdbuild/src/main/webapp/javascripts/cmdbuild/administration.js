(function() {

	var lookupAccordion = new CMDBuild.view.administraton.accordion.CMLookupAccordion({
		cmControllerType: CMDBuild.controller.accordion.CMLookupAccordionController
	});

	var classesAccordion = new CMDBuild.view.administraton.accordion.CMClassAccordion({
		cmControllerType: CMDBuild.controller.accordion.CMClassAccordionController
	});

	var groupsAccordion = new CMDBuild.view.administraton.accordion.CMGroupsAccordion({
		cmControllerType: CMDBuild.controller.accordion.CMGroupAccordionController
	});
	
	var menuAccordion = new CMDBuild.view.administraton.accordion.CMMenuAccordion({
		cmControllerType: CMDBuild.controller.accordion.CMMenuAccordionController
	});

	var domainAccordion = new CMDBuild.view.administraton.accordion.CMDomainAccordion({
		cmControllerType: CMDBuild.controller.accordion.CMDomainAccordionController
	});

	var reportAccordion = new CMDBuild.view.common.report.CMReportAccordion();

	var processAccordion = new CMDBuild.view.administraton.accordion.CMProcessAccordion({
		cmControllerType: CMDBuild.controller.accordion.CMProcessAccordionController
	});

Ext.define("CMDBuild.app.Administration", {
	statics: {
		init: function() {

			CMDBuild.ServiceProxy.lookup.readAllTypes({
				success: function(response, options, decoded) {
					_CMCache.addLookupTypes(decoded);
					lookupAccordion.updateStore();
				}
			});

			CMDBuild.ServiceProxy.classes.read({
				params: {
					active: false
				},
				success: function(response, options, decoded) {
					_CMCache.addClasses(decoded.classes);
					classesAccordion.updateStore();
					processAccordion.updateStore();
				}
			});

			CMDBuild.ServiceProxy.group.read({
				success: function(response, options, decoded) {
					_CMCache.addGroups(decoded.groups);
					groupsAccordion.updateStore();
					menuAccordion.updateStore()
				}
			});

			CMDBuild.ServiceProxy.report.read({
				success: function(response, options, reports) {
					_CMCache.addReports(reports);
					reportAccordion.updateStore();
				}
			});

			CMDBuild.ServiceProxy.administration.domain.list({
				success: function(response, options, decoded) {
					_CMCache.addDomains(decoded.domains);
					domainAccordion.updateStore();
				}
			});

			CMDBuild.ServiceProxy.configuration.readMainConfiguration({
				success: function(response, options, decoded) {
					CMDBuild.Config.cmdbuild = decoded.data;

					CMDBuild.ServiceProxy.configuration.readWFConfiguration({
						success: function(response, options, decoded) {
							CMDBuild.Config.workflow = decoded.data;
							CMDBuild.Config.workflow.enabled = ('true' == CMDBuild.Config.workflow.enabled);
							
							processAccordion.setDisabled(!CMDBuild.Config.workflow.enabled);
							
							CMDBuild.ServiceProxy.configuration.readGisConfiguration({
								success: function(response, options, decoded) {
									CMDBuild.Config.gis = decoded.data;
									CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);
									
									renderThemAll();
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
					classesAccordion,
					processAccordion,
					domainAccordion,
					lookupAccordion,
					reportAccordion,
					menuAccordion,
					groupsAccordion,
					new CMDBuild.view.administraton.accordion.CMGISAccordion(),
					new CMDBuild.view.administraton.accordion.CMConfigurationAccordion()
				],

				cmPanels: [

					new Ext.Panel({
						cls: 'empty_panel x-panel-body'
					}),

					new CMDBuild.view.administration.workflow.CMModProcess({
						cmControllerType: controllerNS.administration.workflow.CMModProcessController
					}),

					new CMDBuild.view.administration.report.CMModReport({
						cmControllerType: controllerNS.administration.report.CMModReportController
					}),

					new CMDBuild.view.administration.group.CMModGroup({
						cmControllerType: controllerNS.administration.group.CMModGroupsController
					}),
					
					new CMDBuild.view.administration.domain.CMModDomain({
						cmControllerType: controllerNS.administration.domain.CMModDomainController
					}),
					
					new CMDBuild.Administration.ModMenu({
						cmControllerType: controllerNS.administration.menu.CMModMenuController
					}),

					new CMDBuild.view.administration.user.CMModUser({
						cmControllerType: controllerNS.administration.user.CMModUserController
					}),

					new CMDBuild.view.administration.classes.CMModClass({
						cmControllerType: controllerNS.administration.classes.CMModClassController
					}),

					new CMDBuild.Administration.ModLookup({
						cmControllerType: controllerNS.administration.lookup.CMModLookupController
					}),
					new CMDBuild.Administration.ModIcons(),
					new CMDBuild.Administration.ModExternalServices(),
					
					new CMDBuild.view.administration.gis.CMModGeoServer({
						cmControllerType: controllerNS.administration.gis.CMModGeoServerController
					}),

					new CMDBuild.Administration.ModLayerOrder({
						cmControllerType: controllerNS.administration.gis.CMModLayerOrderController
					}),
					
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
	_CMMainViewportController.setInstanceName(CMDBuild.Config.cmdbuild.instance_name);
}

})();

//	var creditsLink = Ext.get('cmdbuild_credits_link');
//	creditsLink.on('click', function(e) {
//		splash.showAsPopUp();
//	}, this);
//	
//	var instanceName = Ext.get('instance_name');
//	instanceName.dom.innerHTML = CMDBuild.Config.cmdbuild.instance_name || "";