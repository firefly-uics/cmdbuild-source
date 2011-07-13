(function() {
	var menuAccordion = new CMDBuild.view.administraton.accordion.CMMenuAccordion(),
		reportAccordion = new CMDBuild.view.common.report.CMReportAccordion(),	
		classesAccordion = new CMDBuild.view.common.classes.CMClassAccordion({
			title: CMDBuild.Translation.administration.modClass.tree_title
		}),
		utilitiesTree = new CMDBuild.administration.utilities.UtilitiesAccordion({
			title: CMDBuild.Translation.management.modutilities.title
		});

	Ext.define("CMDBuild.app.Management", {
		statics: {
			init: function() {
				this.buildComponents();
			},

			buildComponents: function() {

				this.cmAccordions = [
					this.menuAccordion = menuAccordion,
 					this.classesAccordion = classesAccordion,
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
	
					this.bulkCardUpdates = new CMDBuild.view.management.utilites.CMModBulkCardUpdate({
						cmControllerType: CMDBuild.controller.management.utilities.CMModBulkUpdateController
					}),

					this.exportCSV = new CMDBuild.view.management.utilities.CMModExportCSV(),

					this.importCSV = new CMDBuild.view.management.utilities.CMModImportCSV({
						cmControllerType: CMDBuild.controller.management.utilities.CMModImportCSVController
					})
				];

				_CMMainViewportController = new CMDBuild.controller.CMMainViewportController(
					new CMDBuild.view.CMMainViewport({
						cmAccordions: this.cmAccordions,
						cmPanels: this.cmPanels
					}).showSplash()
				);

				this.loadResources();
			},
			
			loadResources: function() {
				var dangling = 6,
					me = this;
				
				CMDBuild.ServiceProxy.configuration.readMainConfiguration({
					scope: this,
					success: function(response, options, decoded) {
						CMDBuild.Config.cmdbuild = decoded.data;
						_CMMainViewportController.setInstanceName(CMDBuild.Config.cmdbuild.instance_name);
					},
					callback: callback
				});

				CMDBuild.ServiceProxy.configuration.read({
					success: function(response, options,decoded) {
						CMDBuild.Config.dms = decoded.data;
					},
					callback: callback
				},"legacydms");

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
					success: function(response, options, decoded) {
						_CMCache.addDomains(decoded.domains);
					},
					callback: callback
				});

				function callback() {
					if (--dangling == 0) {
						_CMMainViewportController.viewport.hideSplash();
					}
				}

			}
		}
	});
/*
	var creditsLink = Ext.get('cmdbuild_credits_link');
	creditsLink.on('click', function(e) {
		splash.showAsPopUp();
	}, this);
*/

})();