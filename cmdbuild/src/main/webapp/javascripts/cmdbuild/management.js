(function() {
	var menuAccordion = new CMDBuild.view.administration.accordion.CMMenuAccordion({
			cmControllerType: CMDBuild.controller.management.menu.CMMenuAccordionController
		}),
		reportAccordion = new CMDBuild.view.common.report.CMReportAccordion(),	
		classesAccordion = new CMDBuild.view.common.classes.CMClassAccordion({
			title: CMDBuild.Translation.administration.modClass.tree_title
		}),
		utilitiesTree = new CMDBuild.administration.utilities.UtilitiesAccordion({
			title: CMDBuild.Translation.management.modutilities.title
		}),
		processAccordion = new CMDBuild.view.administration.accordion.CMProcessAccordion({
			rootVisible: true
		});

	Ext.define("CMDBuild.app.Management", {
		statics: {
			init: function() {
				var me = this,
					cb = function() {
						me.buildComponents();
					}

				CMDBuild.ServiceProxy.configuration.readMainConfiguration({
					success: function(response, options, decoded) {
						CMDBuild.Config.cmdbuild = decoded.data;

						CMDBuild.ServiceProxy.configuration.readGisConfiguration({
							success: function(response, options, decoded) {
								CMDBuild.Config.gis = decoded.data;
								CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);

								CMDBuild.ServiceProxy.configuration.read({
									success: function(response, options,decoded) {
										CMDBuild.Config.graph = decoded.data;

										CMDBuild.ServiceProxy.configuration.readWFConfiguration({
											success : function(response, options, decoded) {
												CMDBuild.Config.workflow = decoded.data;
											},
											callback: cb
										});
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
						cmControllerType: CMDBuild.controller.management.classes.CMModCardController
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
				var me = this,
					reqBarrier = new CMDBuild.Utils.CMRequestBarrier(function callback() {
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

						// Do a separate request for the widgets because, at this time
						// it is not possible serialize them with the classes
						CMDBuild.ServiceProxy.CMWidgetConfiguration.groupedByEntryType({
							scope: this,
							success: function(response, options, decoded) {
								// a day I'll can do a request to have only the active, now the cache
								// discards the inactive if the flag onlyActive is true
								_CMCache.addWidgetToEntryTypes(decoded.response, onlyActive = true);
							},
							callback: reqBarrier.getCallback()
						});
					},
					callback: reqBarrier.getCallback()
				});

				CMDBuild.ServiceProxy.configuration.read({
					success: function(response, options,decoded) {
						CMDBuild.Config.dms = decoded.data;
					},
					callback: reqBarrier.getCallback
				},"dms");

				CMDBuild.ServiceProxy.report.getTypesTree({
					scope: this,
					success: function(response, options, reports) {
						_CMCache.addReports(reports);
						reportAccordion.updateStore();
					},
					callback: reqBarrier.getCallback()
				});

				CMDBuild.ServiceProxy.menu.read({
					scope: this,
					success: function(response, options, decoded) {
						if (decoded.length > 0) {
							menuAccordion.updateStore(decoded);
						}
					},
					callback: reqBarrier.getCallback()
				});

				CMDBuild.ServiceProxy.administration.domain.list({ //TODO change "administration"
					params: {
						active: true
					},
					success: function(response, options, decoded) {
						_CMCache.addDomains(decoded.domains);
					},
					callback: reqBarrier.getCallback()
				});

				reqBarrier.start();
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
