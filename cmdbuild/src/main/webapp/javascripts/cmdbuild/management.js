(function() {
	
	Ext.define("CMDBuild.app.Management", {
		statics: {
			init: function() {
				this.buildComponents();
				this.loadResources();
			},
			
			loadResources: function() {
				CMDBuild.ServiceProxy.configuration.readMainConfiguration({
					scope: this,
					success: function(response, options, decoded) {
						CMDBuild.Config.cmdbuild = decoded.data;
					}
				});

				CMDBuild.ServiceProxy.configuration.read({
					success: function(response, options,decoded) {
						CMDBuild.Config.dms = decoded.data;
					}
				},"legacydms");

				CMDBuild.ServiceProxy.report.read({
					scope: this,
					success: function(response, options, reports) {
						_CMCache.addReports(reports);
						this.reportAccordion.updateStore();
					}
				});

				CMDBuild.ServiceProxy.classes.read({
					params: {
						active: true
					},
					scope: this,
					success: function(response, options, decoded) {
						_CMCache.addClasses(decoded.classes);
						this.classesAccordion.updateStore();
					}
				});

				CMDBuild.ServiceProxy.menu.read({
					scope: this,
					success: function(response, options, decoded) {
						if (decoded.length > 0) {
							this.menuAccordion.updateStore(decoded);
						}
					}
				});

				CMDBuild.ServiceProxy.administration.domain.list({ //TODO change "administration"
					success: function(response, options, decoded) {
						_CMCache.addDomains(decoded.domains);
					}
				});

			},

			buildComponents: function() {

				this.cmAccordions = [
					this.menuAccordion = new CMDBuild.view.administraton.accordion.CMMenuAccordion(),
 					this.classesAccordion = new CMDBuild.view.common.classes.CMClassAccordion({
						title: "@@ Class"
					}),
					this.reportAccordion = new CMDBuild.view.common.report.CMReportAccordion(),
					this.utilitiesTree = new CMDBuild.administration.utilities.UtilitiesAccordion({
						title: "@@ Utilities"
					})
				];

				this.cmPanels = [
					new Ext.panel.Panel({}),
					
					new CMDBuild.view.common.report.CMReportGrid({
						cmName: "report",
						cmControllerType: CMDBuild.controller.management.report.CMModReportController
					}),
					
					this.changePasswordPanel = new CMDBuild.view.management.utilities.CMModChangePassword(),

					this.cardPanel = new CMDBuild.view.management.classes.CMModCard({
						cmControllerType: CMDBuild.controller.management.classes.CMModClassController
					}),
	
					this.bulkCardUpdates = new CMDBuild.view.management.utilites.CMModBulkCardUpdate({
						cmControllerType: CMDBuild.controller.management.utilities.CMModBulkUpdateController
					}),

					this.exportCSV = new CMDBuild.view.management.utilities.CMModExportCSV(),

					this.importCSV = new CMDBuild.view.management.utilities.CMModImportCSV({
						cmControllerType: CMDBuild.controller.management.utilities.CMModImportCSVController
					})
				];
				
				this.buildViewport();
			},
			
			buildViewport: function() {
				_CMMainViewportController = new CMDBuild.controller.CMMainViewportController(
					new CMDBuild.view.CMMainViewport({
						cmAccordions: this.cmAccordions,
						cmPanels: this.cmPanels
					})
				);
			}
		}
	});

	/*
	CMDBuild.ConcurrentAjax.execute({
		loadMask: false,
		requests: [{
                url: 'services/json/schema/setup/getconfiguration',
                params: {
                    name: 'cmdbuild'
                },
                maskMsg: CMDBuild.Translation.common.loading_mask.configuration,
                success: function(response, options,
                        decoded) {
                    CMDBuild.Config.cmdbuild = decoded.data;
                }
            },{
                url: 'services/json/schema/setup/getconfiguration',
                params: {
                    name: 'legacydms'
                },
                maskMsg: CMDBuild.Translation.common.loading_mask.configuration,
                success: function(response, options,
                        decoded) {
                    CMDBuild.Config.dms = decoded.data;
                }
            },{
                url: 'services/json/schema/setup/getconfiguration',
                params: {
                    name: 'graph'
                },
                maskMsg: CMDBuild.Translation.common.loading_mask.configuration,
                success: function(response, options,
                        decoded) {
                    CMDBuild.Config.graph = decoded.data;
                }
            },{
                url: 'services/json/schema/setup/getconfiguration',
                params: {
                    name: 'gis'
                },
                success: function(response, options,
                        decoded) {
                    CMDBuild.Config.gis = decoded.data;
                    CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);
                }
            },{
                url: 'services/json/schema/setup/getconfiguration',
                params: {
                    name: 'workflow'
                },
                maskMsg: CMDBuild.Translation.common.loading_mask.configuration,
                success: function(response, options,
                        decoded) {
                    CMDBuild.Config.workflow = decoded.data;
                }
            },{
                url: "services/json/schema/modclass/getallclasses",
                params: {
                    active: true
                },
                maskMsg: CMDBuild.Translation.common.loading_mask.classes,
                success: function(response, options, decoded) {
                    splash.setText(CMDBuild.Translation.common.loading_mask.classes);
                    CMDBuild.Cache.setTables(decoded.classes);
                }
            },{
                url: 'services/json/schema/modmenu/getgroupmenu',
                success: function(response, options, decoded) {
                    if (decoded.length > 0) {
                        var itemsMap = CMDBuild.TreeUtility.arrayToMap(decoded);
                        CMDBuild.Cache.menuTree = CMDBuild.TreeUtility.buildTree(itemsMap, "menu", addAttributes = true);
                    }
                }
            },{
                url: 'services/json/management/modreport/getreporttypestree',
                success: function(response, options, decoded) {
                    CMDBuild.Cache.setTables(decoded);
                }
            }],
        fn: function() {
            displayViewport();
        }
	});
	
	function displayViewport() {
		var wfView = new CMDBuild.Management.ModWorkflow();
		var wfController = new CMDBuild.Management.WFController(wfView);
		
		var viewport = new CMDBuild.MainViewport({
			id: "management_main_viewport",
			trees: (function() {
				var trees = [];
				var structure = CMDBuild.Structure;				
				for (var tree in structure) {
					var t = structure[tree].createTree();
					if (t) {
						trees.push(t);
					}
				}
				return trees;
			})(),
			modules: [
			    new Ext.Panel({
			    	bodyCfg: {
				        cls: 'empty_panel x-panel-body'
				    }
			    }),
				new CMDBuild.Management.ModCard(),
				wfView,
				new CMDBuild.Management.ModReport(),
				new CMDBuild.Management.ModBulkCardUpdate(),
				new CMDBuild.Management.ModChangePassword(),
				new CMDBuild.Management.ModImportCSV(),
				new CMDBuild.Management.ModExportCSV()
			]
		});
		
		(function() {
			splash.hide();
		}).defer(500);
		
		var creditsLink = Ext.get('cmdbuild_credits_link');
		creditsLink.on('click', function(e) {
			splash.showAsPopUp();
		}, this);
		
		var instanceName = Ext.get('instance_name');
		instanceName.dom.innerHTML = CMDBuild.Config.cmdbuild.instance_name || "";
		
	}
	

});
	*/

})();