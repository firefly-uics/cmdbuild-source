(function() {
	var view,
		controller,
		chartTypeStrategy;

	describe('CMDashboardChartConfigurationFormController', function() {

		beforeEach(function() {
			view = jasmine.createSpyObj("CMDashboardChartConfigurationFormSpy", [
				"fillFieldsWith",
				"showFieldsWithName",
				"disableFields",
				"enableFields",
				"hideOutputFields",
				"cleanFields",
				"setDelegate",
				"getFieldsValue",
				"showDataSourceInputFields",
				"setSingleSerieFieldAvailableData",
				"getDataSourceConfiguration",
				"fillDataSourcePanel"
			]);

			controller = new CMDBuild.controller.administration.dashboard
				.CMDashboardChartConfigurationFormController(view);

			chartTypeStrategy = new CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategy();
		});

		afterEach(function() {
			delete view;
			delete controller;
			delete chartTypeStrategy;
		});

		it('init well the view', function() {
			controller.initView();

			expect(view.cleanFields).toHaveBeenCalled();
			expect(view.disableFields).toHaveBeenCalled();
			expect(view.hideOutputFields).toHaveBeenCalled();
		});

		it('is able to prepare the form to add a chart', function() {
			controller.prepareForAdd();

			expect(view.cleanFields).toHaveBeenCalled();
			expect(view.hideOutputFields).toHaveBeenCalled();
			expect(view.enableFields).toHaveBeenCalled();
		});

		it('is able to prepare the form to a selected chart', function() {
			var showChartFields = spyOn(chartTypeStrategy, "showChartFields");
			var fillFieldsForChart = spyOn(chartTypeStrategy, "fillFieldsForChart");
			var c = aChart();

			controller.setChartTypeStrategy(chartTypeStrategy);
			controller.prepareForChart(c);

			expect(view.cleanFields).toHaveBeenCalled();
			expect(view.fillFieldsWith).toHaveBeenCalled();
			expect(view.disableFields).toHaveBeenCalled();
			expect(view.hideOutputFields).toHaveBeenCalled();
			expect(fillFieldsForChart).toHaveBeenCalledWith(c);

			var data = view.fillFieldsWith.argsForCall[0][0];
			expect(data.name).toEqual(c.getName());
			expect(data.description).toEqual(c.getDescription());
			expect(data.active).toEqual(c.isActive());
			expect(data.autoLoad).toEqual(c.isAutoload());
		});

		it('is able to enable the fields to modify a chart', function() {
			controller.prepareForModify();
			expect(view.enableFields).toHaveBeenCalledWith(onlyMutable=true);
		});

		it('is able to read to retrieve the values from the form', function() {
			var extractInterestedValues = spyOn(chartTypeStrategy, "extractInterestedValues");
			view.getFieldsValue.andReturn({
				id: 2,
				active: true,
				autoload: true,
				name: "Chart foo",
				description: "Description of Foo"
			});
			controller.setChartTypeStrategy(chartTypeStrategy);
			controller.getFormData();
			expect(view.getFieldsValue).toHaveBeenCalled();
			expect(extractInterestedValues).toHaveBeenCalled();
		})

		// typeStrategy setting

		it('throws an exception if try to set a strategy that is not istance of CMDashboardChartConfigurationFormControllerTypeStrategy', function() {
			var s = {};
			var expectedError = Ext.String.format(CMDBuild.IS_NOT_CONFORM_TO_INTERFACE, s, "CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategy");

			expect(function() {
				controller.setChartTypeStrategy(s);
			}).toThrow(expectedError);
		});

		it('set the chart type strategy', function() {
			var showChartFields = spyOn(chartTypeStrategy, "showChartFields"),
				setChartDataSourceName = spyOn(chartTypeStrategy, "setChartDataSourceName");

			expect(controller.chartTypeStrategy).toBeNull();

			controller.setChartTypeStrategy(chartTypeStrategy);
			expect(controller.chartTypeStrategy).toBe(chartTypeStrategy);
			expect(showChartFields).toHaveBeenCalled();
			expect(setChartDataSourceName).toHaveBeenCalled();
		});

		// view delegate

		it('onTypeChanged istantiate the strategy', function() {
			controller.onTypeChanged("gauge");
			expect(Ext.getClassName(controller.chartTypeStrategy)).toEqual("CMDBuild.controller.administration.dashboard.charts.CMChartGaugeStrategy");
			view.hideOutputFields.reset();

			controller.onTypeChanged(undefined);
			expect(controller.chartTypeStrategy).toBeNull();
			expect(view.hideOutputFields).toHaveBeenCalled();
		});

		it('onDataSourceChanged', function() {
			var dsName = "cm_datasource_1",
				input = [
					{name: "input1", type: "date"},
					{name: "input2", type: "integer"},
					{name: "input3", type: "string"}
				],
				getDataSourceInput = spyOn(_CMCache, "getDataSourceInput").andReturn(input);

			controller.onDataSourceChanged(dsName);

			expect(getDataSourceInput).toHaveBeenCalledWith(dsName);
			expect(view.showDataSourceInputFields).toHaveBeenCalledWith(input);
		});
	});

	function aChart(config) {
		config = Ext.apply({
			id: 2,
			active: true,
			autoload: true,
			name: "Chart foo",
			description: "Description of Foo"
		}, config);

		return new CMDBuild.model.CMDashboardChart(config);
	}

})();