(function() {
	var view,
		realDataSourceStoreFunction,
		delegate;

	describe('CMDashboardChartConfigurationForm', function() {

		beforeEach(function() {
			view = new CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationForm({
				renderTo: Ext.getBody()
			});

			delegate = new CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationFormDelegate();

			realDataSourceStoreFunction = _CMCache.getAvailableDataSourcesStore;

			_CMCache.getAvailableDataSourcesStore = function() {
				return new Ext.data.SimpleStore({
					model : "CMDBuild.model.CMChartDataSource",
					data : [{
						name: "cm_datasource_1",
						input: [{
							name: "in11",type: "integer"
						},{
							name: "in12",type: "string"
						},{
							name: "in13", type: "date"
						}],
						output: [{
							name: "out11", type: "integer"
						},{
							name: "out12", type: "string"
						},{
							name: "out13", type: "date"
						}]
					}]
				})
			}

			this.addMatchers({
				toBeEnabled : function(expected) {
					return !this.actual.disabled;
				},
				toBeHidden : function(expected) {
					return this.actual.getEl().dom.style.display == "none";
				}
			});
		});

		afterEach(function() {
			delete view;
			delete delegate;

			_CMCache.getAvailableDataSourcesStore = realDataSourceStoreFunction;
		});

		it('starts with all the fields disabled', function() {
			expectAllTheFieldsAreDisabled();
		});

		it('is able to enable the fields', function() {
			view.enableFields();

			expect(view.nameField).toBeEnabled();
			expectAllMutableFieldsAreEnabled();
		});

		it('is able to enable only the mutable fields', function() {
			view.enableFields(onlyMutable=true);

			expectAllMutableFieldsAreEnabled();
			expect(view.nameField).not.toBeEnabled();
		});

		it('is able to disable the fields', function() {
			view.enableFields();
			view.disableFields();
	
			expectAllTheFieldsAreDisabled();
		});

		it('starts with the fields empty', function() {
			expectAllTheFieldsAreEmpty();
		});

		it('is able to fill the fields', function() {
			var data = {
				name: "Foo",
				description: "Bar",
				active: true,
				autoLoad: true,
				dataSource: "cm_datasource_1",
				type: "PIE",
				maximum: 100,
				minimum: 1,
				steps: 20,
				fgcolor: "#FFFFFF",
				bgcolor: "#FFFFFF"
			};

			view.fillFieldsWith(data);

			expect(view.nameField.getValue()).toEqual(data.name);
			expect(view.descriptionArea.getValue()).toEqual(data.description);
			expect(view.activeCheck.getValue()).toEqual(data.active);
			expect(view.autoLoadCheck.getValue()).toEqual(data.autoLoad);
			expect(view.dataSourcePanel.dataSourceCombo.getValue()).toEqual(data.dataSource);
			expect(view.typeField.getValue()).toEqual(data.type);
			expect(view.maximumField.getValue()).toEqual(data.maximum);
			expect(view.minimumField.getValue()).toEqual(data.minimum);
			expect(view.stepsField.getValue()).toEqual(data.steps);
			expect(view.fgColorField.getValue()).toEqual(data.fgcolor);
			expect(view.bgColorField.getValue()).toEqual(data.bgcolor);
		});

		it('is able to take the values from fields', function() {
			var data = {
				name: "Foo",
				description: "Bar",
				active: true,
				autoLoad: true,
				dataSource: "cm_datasource_1",
				type: "PIE",
				maximum: 100,
				minimum: 1,
				steps: 20,
				fgcolor: "#FFFFFF",
				bgcolor: "#FFFFFF",
				singleSerieField : null
			};

			view.fillFieldsWith(data);
			var out = view.getFieldsValue();

			expect(data).toEqual(out);
		});

		it('is able to reset the values of fields', function() {
			var data = {
				name: "Foo",
				description: "Bar",
				active: true,
				autoLoad: true,
				dataSource: "cm_datasource_1",
				type: "PIE",
				maximum: 100,
				minimum: 1,
				steps: 20,
				fgcolor: "#FFFFFF",
				bgcolor: "#FFFFFF"
			};

			view.fillFieldsWith(data);
			view.cleanFields();

			expectAllTheFieldsAreEmpty();
		});

		it('starts with the type specific fields hidden ', function() {
			expectSpecificFieldsHidden();
		});

		it('is able to hide fields by name', function() {
			expect(view.nameField).not.toBeHidden();
			expect(view.descriptionArea).not.toBeHidden();
			expect(view.activeCheck).not.toBeHidden();
			expect(view.dataSourcePanel.dataSourceCombo).not.toBeHidden();

			view.hideFieldsWithName("name");
			view.hideFieldsWithName(["description", "active"]);

			expect(view.nameField).toBeHidden();
			expect(view.descriptionArea).toBeHidden();
			expect(view.activeCheck).toBeHidden();

			expect(view.dataSourcePanel.dataSourceCombo).not.toBeHidden();
		});

		it ('is able to hide the outPutConfigurationItem', function() {
			view.showFieldsWithName(["minimum", "maximum"]);
			view.hideOutputFields();

			expectSpecificFieldsHidden();
		});

		it ('is able to show fields by name', function () {
			view.hideFieldsWithName(["description", "active", "minimum"]);
			view.showFieldsWithName(["description", "active", "minimum"]);

			expect(view.nameField).not.toBeHidden();
			expect(view.descriptionArea).not.toBeHidden();
			expect(view.activeCheck).not.toBeHidden();
			expect(view.minimumField).not.toBeHidden();
			expect(view.stepsField).toBeHidden();
		});

		it ('is able to load the data for the field to map the single output chart', function() {
			var availableFields = [['foo'],['bar']];
			expect(view.singleSerieField.store.data.length).toBe(0);
			view.setSingleSerieFieldAvailableData(availableFields);
			expect(view.singleSerieField.store.data.length).toBe(2);
		});

		// delegate
		it('throw exception if pass to setDelegate a non conform object', function() {
			delegate = new Object();
			assertException("The view must throw exception for non conform object on setDelegate",
				function() {
					view.setDelegate(delegate);
				});
		});

		it('is able to set the delegate', function() {
			expect(view.delegate).toBeUndefined();
			view.setDelegate(delegate);
			expect(view.delegate).toEqual(delegate);
		});

		it('call the delegate when change the type of chart', function() {
			var onTypeChanged = spyOn(delegate, "onTypeChanged");
			view.setDelegate(delegate);
			view.fillFieldsWith({
				type: "gauge"
			});

			expect(onTypeChanged).toHaveBeenCalledWith("gauge");
			onTypeChanged.reset();

			// when select a item from the combo
			// it pass as value a model object.
			// I want only the value field
			var record = view.typeField.store.first();
			var value = record.get("value");
			view.typeField.select(record);
			expect(onTypeChanged).toHaveBeenCalledWith(value);

			// call also for undefined
			onTypeChanged.reset();
			view.typeField.setValue(undefined);
			expect(onTypeChanged).toHaveBeenCalledWith(undefined);
		});

		it('call the delegate when change the data source of chart', function() {
			var onDataSourceChanged = spyOn(delegate, "onDataSourceChanged");
			view.setDelegate(delegate);

			view.fillFieldsWith({
				dataSource: "cm_datasource_1"
			});

			expect(onDataSourceChanged).toHaveBeenCalled();
			var args = onDataSourceChanged.argsForCall[0];
			expect(args[0]).toBe("cm_datasource_1");
		});
	});

	function expectSpecificFieldsHidden() {
		expect(view.maximumField).toBeHidden();
		expect(view.minimumField).toBeHidden();
		expect(view.stepsField).toBeHidden();
		expect(view.fgColorField).toBeHidden();
		expect(view.bgColorField).toBeHidden();
		expect(view.singleSerieField).toBeHidden();
	};

	function expectAllMutableFieldsAreEnabled() {
		expect(view.descriptionArea).toBeEnabled();
		expect(view.activeCheck).toBeEnabled();
		expect(view.autoLoadCheck).toBeEnabled();
		expect(view.dataSourcePanel.dataSourceCombo).toBeEnabled();
		expect(view.typeField).toBeEnabled();
		expect(view.maximumField).toBeEnabled();
		expect(view.minimumField).toBeEnabled();
		expect(view.stepsField).toBeEnabled();
		expect(view.fgColorField).toBeEnabled();
		expect(view.bgColorField).toBeEnabled();
	}

	function expectAllTheFieldsAreDisabled() {
		expect(view.nameField).not.toBeEnabled();
		expect(view.descriptionArea).not.toBeEnabled();
		expect(view.activeCheck).not.toBeEnabled();
		expect(view.autoLoadCheck).not.toBeEnabled();
		expect(view.dataSourcePanel.dataSourceCombo).not.toBeEnabled();
		expect(view.typeField).not.toBeEnabled();
		expect(view.maximumField).not.toBeEnabled();
		expect(view.minimumField).not.toBeEnabled();
		expect(view.stepsField).not.toBeEnabled();
		expect(view.fgColorField).not.toBeEnabled();
		expect(view.bgColorField).not.toBeEnabled();
	}

	function expectAllTheFieldsAreEmpty() {
		expect(view.nameField.getValue()).toEqual("");
		expect(view.descriptionArea.getValue()).toEqual("");
		expect(view.activeCheck.getValue()).toEqual(false);
		expect(view.autoLoadCheck.getValue()).toEqual(false);
		expect(view.dataSourcePanel.dataSourceCombo.getValue()).toEqual(null);
		expect(view.typeField.getValue()).toEqual(null);
		expect(view.maximumField.getValue()).toEqual(null);
		expect(view.minimumField.getValue()).toEqual(null);
		expect(view.stepsField.getValue()).toEqual(null);
		expect(view.fgColorField.getValue()).toEqual(undefined);
		expect(view.bgColorField.getValue()).toEqual(undefined);
	}
})();
