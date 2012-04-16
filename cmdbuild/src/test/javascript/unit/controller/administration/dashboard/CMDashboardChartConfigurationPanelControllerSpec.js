(function() {
	var view,
		controller,
		formSubController,
		gridSubController;

	describe('CMDashboardChartConfigurationPanelController', function() {

		beforeEach(function() {
			view = jasmine.createSpyObj(
				"CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationPanelInterface", [
				"enableTBarButtons",
				"disableTBarButtons",
				"enableButtons",
				"disableButtons",
				"setDelegate",
				"getFormPanel",
				"getGridPanel"
			]);

			formSubController = jasmine.createSpyObj("formSubController", [
				"initView",
				"prepareForAdd",
				"prepareForChart",
				"prepareForModify",
				"getFormData"
			]);

			gridSubController = jasmine.createSpyObj("gridSubController", [
				"loadCharts",
				"setDelegate",
				"clearSelection"
			]);

			proxy = jasmine.createSpyObj("proxySpy", [
				"remove",
				"add",
				"modify"
			]);

			controller = new CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationPanelController(view, formSubController, gridSubController, proxy);
		});

		afterEach(function() {
			delete view;
			delete controller;
			delete formSubController;
			delete gridSubController;
		});

		it('prepare to display the new selection', function() {
			var d = aDashboard();
			controller.dashboardWasSelected(d);

			expect(controller.dashboard).toBe(d);
			expect(view.enableTBarButtons).toHaveBeenCalledWith(onlyAdd=true);
			expect(view.disableButtons).toHaveBeenCalled();
			expect(formSubController.initView).toHaveBeenCalledWith(d);
			expect(gridSubController.loadCharts).toHaveBeenCalledWith(d.getCharts());
		});

		it('respond to chart selection', function() {
			var d = aDashboard();
			var c = d.getCharts()[0];
			controller.chartWasSelected(c);

			expect(controller.chart).toBe(c);
			expect(formSubController.prepareForChart).toHaveBeenCalledWith(c);
			expect(view.disableButtons).toHaveBeenCalled();
			expect(view.enableTBarButtons).toHaveBeenCalled();
		});

		it('prepare to modify a chart', function() {
			controller.onModifyButtonClick();

			expect(view.disableTBarButtons).toHaveBeenCalled();
			expect(view.enableButtons).toHaveBeenCalled();
			expect(formSubController.prepareForModify).toHaveBeenCalled();
		});

		it('prepare to add a chart', function() {
			selectAChart();
			controller.onAddButtonClick();

			expect(controller.chart).toBeNull();
			expect(formSubController.prepareForAdd).toHaveBeenCalled();
			expect(gridSubController.clearSelection).toHaveBeenCalled();
			expect(view.disableTBarButtons).toHaveBeenCalled();
			expect(view.enableButtons).toHaveBeenCalled();
		});

		it('is able to remove a chart', function() {
			selectAChart();
			controller.onRemoveButtonClick();

			expect(proxy.remove).toHaveBeenCalled();
			var params = proxy.remove.argsForCall[0];
			expect(params[0]).toBe(1); // dashboardId
			expect(params[1]).toBe(2); // chartId

			expect(view.disableButtons).toHaveBeenCalled();
			expect(view.disableTBarButtons).toHaveBeenCalled();
		});


		it('is able to add a chart', function() {
			var formData = {
				name: "Chart foo",
				description: "Description of Foo",
				active: true,
				autoLoad: false,
				id: 2
			};
			formSubController.getFormData.andReturn(formData);

			controller.dashboardWasSelected(aDashboard());
			controller.onSaveButtonClick();

			expect(view.disableButtons).toHaveBeenCalled();
			expect(view.disableTBarButtons).toHaveBeenCalled();
			expect(proxy.add).toHaveBeenCalled();
			var params = proxy.add.argsForCall[0];
			expect(params[0]).toBe(1); // dashboardId
			expect(params[1]).toBe(formData);
		});

		it ('is able to modify a chart', function() {
			selectAChart();
			var formData = {
				name: "Chart foo oo",
				description: "Description of foo oo",
				active:false,
				autoLoad:true,
				id: 2
			};

			formSubController.getFormData.andReturn(formData);

			controller.onSaveButtonClick();
			expect(view.disableButtons).toHaveBeenCalled();
			expect(view.disableTBarButtons).toHaveBeenCalled();
			expect(proxy.modify).toHaveBeenCalled();
			var params = proxy.modify.argsForCall[0];
			expect(params[0]).toBe(1); // dashboardId
			expect(params[1]).toBe(2); // chartId
			expect(params[2]).toBe(formData);
		});

		// TODO: clicco su save
		// chiedo i dati al controller del form
		// disabilito il form
		// disabilito i bottoni (tutti)
		// faccio la richiesta
		// se ok dico al controller della griglia di:
			// se era modifica di aggiornare la riga e riselezionarla
			// se era add di aggiungerla e selezionarla

		it('prepare to abort the editing', function() {
			controller.onAddButtonClick();
			controller.onAbortButtonClick();

			formSubController.initView.reset();
			view.enableTBarButtons.reset();

			controller.onAbortButtonClick();

			expect(formSubController.initView).toHaveBeenCalled();
			expect(view.disableButtons).toHaveBeenCalled();
			expect(view.enableTBarButtons).toHaveBeenCalledWith(onlyAdd=true);

			selectAChart();
			controller.onModifyButtonClick();
			controller.onAbortButtonClick();

			view.disableButtons.reset();
			view.enableTBarButtons.reset();
			formSubController.prepareForChart.reset();

			controller.onAbortButtonClick();

			expect(formSubController.prepareForChart).toHaveBeenCalledWith(controller.chart);
			expect(view.disableButtons).toHaveBeenCalled();
			expect(view.enableTBarButtons).toHaveBeenCalledWith(onlyAdd=false);
		});
	});

	function selectAChart() {
		var d = aDashboard();
		var c = d.getCharts()[0];
		controller.dashboardWasSelected(d);
		controller.chartWasSelected(c);
	}

	function aDashboard() {

		return new CMDBuild.model.CMDashboard({
			id: 1,
			name: "Foo",
			description: "Cool dashboard for cool people",
			groups: [1],
			charts: [
				new CMDBuild.model.CMDashboardChart({
					name: "Chart foo",
					description: "Description of Foo",
					active: true,
					autoLoad: false,
					id: 2
				}),
				new CMDBuild.model.CMDashboardChart({
					name: "Chart bar",
					description: "Description of Bar",
					active:false,
					autoLoad:true,
					id: 3
				})
			]
		});
	}
})();
