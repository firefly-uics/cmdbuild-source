(function() {
	var view, realCache, controller, treeNode, getPropertiesPanel;

	describe("CMDBuild.controller.administration.dashboard.CMModDashboardController", function() {

		beforeEach(function() {
			realCache = _CMCache;

			_CMCache = {
				getDashboardById: function() {
					return new CMDBuild.model.CMDashboard({
						id: 1,
						name: "Foo",
						description: "Cool dashboard for cool people",
					});
				}
			}

			_CMMainViewportController = { // have to be global
				deselectAccordionByName: function() {}
			};

			treeNode = {
				get: function() {return "foo"}
			};

			view = new CMDBuild.view.administration.dashboard.CMModDashboardInterface();
			view.on = Ext.emptyFn;

			getPropertiesPanel = spyOn(view, "getPropertiesPanel").andReturn(new CMDBuild.view.administration.dashboard.CMDashboardPropertiesPanelInterface());
			controller = new CMDBuild.controller.administration.dashboard.CMModDashboardController(view);
		});

		afterEach(function() {
			_CMCache = realCache;
			delete realCache;
			delete view;
			delete controller;
			delete treeNode;
			delete getPropertiesPanel;
			delete _CMMainViewportController;
		});

		it("Istantiate the subcontrolelrs", function() {
			expect(controller.propertiesPanelController).toBeDefined();
		});

		it("Take the dashboard from cache when is calld onViewOnFront", function() {
			var getDashboardById = spyOn(_CMCache, "getDashboardById").andCallThrough();

			controller.onViewOnFront(treeNode);
			expect(getDashboardById).toHaveBeenCalled();
			expect(controller.dashboard.get("name")).toEqual("Foo");
		});

		it("Say to the view to set his title when is calld onViewOnFront", function() {
			var setTitleSuffix = spyOn(view, "setTitleSuffix");

			controller.onViewOnFront(treeNode);
			expect(setTitleSuffix).toHaveBeenCalled();
		});

		it("Nofity to the subcontrollers that a dashboard was selected", function() {
			var dashboardWasSelected = spyOn(controller.propertiesPanelController, "dashboardWasSelected");

			controller.onViewOnFront(treeNode);
			expect(dashboardWasSelected).toHaveBeenCalled();
			var args = dashboardWasSelected.argsForCall[0];
			expect(args[0].get("name")).toEqual("Foo");
		});

		it("Prepare the view to add a dashboard", function() {
			var prepareForAdd = spyOn(controller.propertiesPanelController, "prepareForAdd"),
				deselectTree = spyOn(_CMMainViewportController, "deselectAccordionByName")

			controller.onAddButtonClick();

			expect(prepareForAdd).toHaveBeenCalled();
			expect(deselectTree).toHaveBeenCalledWith(view.cmName);
		});
	});
})();