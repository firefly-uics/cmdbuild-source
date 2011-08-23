(function() {
	var menuAccordion = new CMDBuild.view.administraton.accordion.CMMenuAccordion(),
		reportAccordion = new CMDBuild.view.common.report.CMReportAccordion(),	
		classesAccordion = new CMDBuild.view.common.classes.CMClassAccordion({
			title: CMDBuild.Translation.administration.modClass.tree_title
		}),
		utilitiesTree = new CMDBuild.administration.utilities.UtilitiesAccordion({
			title: CMDBuild.Translation.management.modutilities.title
		}),
		processAccordion = new CMDBuild.view.administraton.accordion.CMProcessAccordion({
			rootVisible: true
		});

	Ext.define("CMDBuild.app.Management", {
		statics: {
			init: function() {
				CMDBuild.ServiceProxy.configuration.readMainConfiguration({
					scope: this,
					success: function(response, options, decoded) {
						CMDBuild.Config.cmdbuild = decoded.data;

						CMDBuild.ServiceProxy.configuration.readGisConfiguration({
							scope: this,
							success: function(response, options, decoded) {
								CMDBuild.Config.gis = decoded.data;
								CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);
							},
							callback: function() {
								this.buildComponents();
							}
						});
					}
				});
			},

			buildComponents: function() {
				this.cmAccordions = [
					this.menuAccordion = menuAccordion,
 					this.classesAccordion = classesAccordion,
 					this.processAccordion = processAccordion,
					this.reportAccordion = reportAccordion,
					this.utilitiesTree = utilitiesTree
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

					this.processPanel = new CMDBuild.view.management.workflow.CMModProcess({
						cmControllerType: CMDBuild.controller.management.workflow.CMModWorkflowController
					}),

					this.bulkCardUpdates = new CMDBuild.view.management.utilites.CMModBulkCardUpdate({
						cmControllerType: CMDBuild.controller.management.utilities.CMModBulkUpdateController
					}),

					this.exportCSV = new CMDBuild.view.management.utilities.CMModExportCSV(),

					this.importCSV = new CMDBuild.view.management.utilities.CMModImportCSV({
						cmControllerType: CMDBuild.controller.management.utilities.CMModImportCSVController
					})
				];

				CMDBuild.view.CMMainViewport.showSplash();
				this.loadResources();
			},
			
			loadResources: function() {
				var dangling = 6,
					me = this;

				function callback() {
					if (--dangling == 0) {
						_CMMainViewportController = new CMDBuild.controller.CMMainViewportController(
							new CMDBuild.view.CMMainViewport({
								cmAccordions: me.cmAccordions,
								cmPanels: me.cmPanels
							})
						);
						_CMMainViewportController.selectStartingClass();
						_CMMainViewportController.setInstanceName(CMDBuild.Config.cmdbuild.instance_name);

						if (processAccordion.isEmpty()) {
							processAccordion.hide();
						}

						CMDBuild.view.CMMainViewport.hideSplash();
					}
				}

				CMDBuild.ServiceProxy.configuration.read({
					success: function(response, options,decoded) {
						CMDBuild.Config.dms = decoded.data;
					},
					callback: callback
				},"dms");
				
				CMDBuild.ServiceProxy.configuration.read({
					success: function(response, options,decoded) {
						CMDBuild.Config.graph = decoded.data;
					},
					callback: callback
				},"graph");

				CMDBuild.ServiceProxy.report.read({
					scope: this,
					success: function(response, options, reports) {
						_CMCache.addReports(reports);
						reportAccordion.updateStore();
					},
					callback: callback
				});

				CMDBuild.ServiceProxy.classes.read({
					params: {
						active: true
					},
					scope: this,
					success: function(response, options, decoded) {
						_CMCache.addClasses(decoded.classes);
						classesAccordion.updateStore();
						processAccordion.updateStore();
					},
					callback: callback
				});

				CMDBuild.ServiceProxy.menu.read({
					scope: this,
					success: function(response, options, decoded) {
						if (decoded.length > 0) {
							menuAccordion.updateStore(decoded);
						}
					},
					callback: callback
				});

				CMDBuild.ServiceProxy.administration.domain.list({ //TODO change "administration"
					params: {
						active: true
					},
					success: function(response, options, decoded) {
						_CMCache.addDomains(decoded.domains);
					},
					callback: callback
				});
			}
		}
	});
})();