(function() {
	var menuAccordion = new CMDBuild.view.administraton.accordion.CMMenuAccordion({
			cmControllerType: CMDBuild.controller.management.menu.CMMenuAccordionController
		}),
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

								CMDBuild.ServiceProxy.configuration.read({
									scope: this,
									success: function(response, options,decoded) {
										CMDBuild.Config.graph = decoded.data;
									},
									callback: function() {
										this.buildComponents();
									}
								},"graph");

							}
						});
					}
				});
			},

			buildComponents: function() {
				var disabled = CMDBuild.Runtime.DisabledModules;
				this.cmAccordions = [
					this.menuAccordion = menuAccordion
				];

				this.cmPanels = [
					new Ext.panel.Panel({}),
					this.cardPanel = new CMDBuild.view.management.classes.CMModCard({
						cmControllerType: CMDBuild.controller.management.classes.CMModClassController
					}),
					this.processPanel = new CMDBuild.view.management.workflow.CMModProcess({
						cmControllerType: CMDBuild.controller.management.workflow.CMModWorkflowController
					}),
					this.reportPanel = new CMDBuild.view.common.report.CMReportGrid({
						cmName: "report",
						cmControllerType: CMDBuild.controller.management.report.CMModReportController
					})
				];

				if (!disabled[classesAccordion.cmName]) {
					this.classesAccordion = classesAccordion;
					this.cmAccordions.push(this.classesAccordion);
				}

				if (!disabled[processAccordion.cmName]) {
					this.processAccordion = processAccordion;
					this.cmAccordions.push(this.processAccordion);
				}

				if (!disabled[reportAccordion.cmName]) {
					this.reportAccordion = reportAccordion;
					this.cmAccordions.push(this.reportAccordion);
				}

				this.utilitiesTree = utilitiesTree;
				if (this.utilitiesTree.getRootNode().childNodes.length > 0) {
					this.cmAccordions.push(this.utilitiesTree);
				}

				for (var moduleName in this.utilitiesTree.submodules) {
					var cmName = this.utilitiesTree.getSubmoduleCMName(moduleName);
					if (!disabled[cmName]) {
						addUtilitySubpanel(cmName, this.cmPanels);
					}
				}

				CMDBuild.view.CMMainViewport.showSplash();
				this.loadResources();
			},

			loadResources: function() {
				var dangling = 5,
					me = this;

				function callback() {
					if (--dangling == 0) {
						hideIfEmpty(processAccordion);
						hideIfEmpty(reportAccordion);
						hideIfEmpty(menuAccordion);
						hideIfEmpty(classesAccordion);

						_CMMainViewportController = new CMDBuild.controller.CMMainViewportController(
							new CMDBuild.view.CMMainViewport({
								cmAccordions: me.cmAccordions,
								cmPanels: me.cmPanels
							})
						);
						_CMMainViewportController.selectStartingClass();
						_CMMainViewportController.setInstanceName(CMDBuild.Config.cmdbuild.instance_name);

						CMDBuild.view.CMMainViewport.hideSplash();
					}
				}

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

				CMDBuild.ServiceProxy.configuration.read({
					success: function(response, options,decoded) {
						CMDBuild.Config.dms = decoded.data;
					},
					callback: callback
				},"dms");

				CMDBuild.ServiceProxy.report.getTypesTree({
					scope: this,
					success: function(response, options, reports) {
						_CMCache.addReports(reports);
						reportAccordion.updateStore();
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

		if (typeof builders[cmName] == "function") {
			panels.push(builders[cmName]());
		}
	}
})();