(function() {
	TestCase("testCMOpenReportDefinitionForm", {

		setUp: function() {
			this.form = new CMDBuild.view.administration.widget.form.CMOpenReportDefinitionForm();
		},

		tearDown: function() {
			delete this.form;
		},

		"test fillWithModel": function() {
			var def = getEidgetDefinition();
			var spy = sinon.spy(this.form, "fillPresetWithData");
			var model = new CMDBuild.model.CMWidgetDefinitionModel(def);

			this.form.fillWithModel(model);
			assertEquals(def.buttonLabel, this.form.buttonLabel.getValue());
			assertEquals(true, this.form.forceFormatCheck.getValue());
			assertEquals(def.forceFormat, this.form.forceFormatOptions.getValue());
			assertEquals(def.reportCode, this.form.reportCode.getValue());
			assertEquals(def.active, this.form.active.getValue());

			assertTrue(spy.calledWith(def.preset));
			spy.restore;
		},

		"test getWidgetDefinition": function() {
			var def = getEidgetDefinition();
			var model = new CMDBuild.model.CMWidgetDefinitionModel(def);

			this.form.fillWithModel(model);
			var def1 = this.form.getWidgetDefinition();

			assertEquals(def.type, def1.type);
			assertEquals(def.buttonLabel, def1.buttonLabel);
			assertEquals(def.forceFormat, def1.forceFormat);
			assertEquals(def.reportCode, def1.reportCode);
			assertEquals(def.active, def1.active);
			assertEquals(def.preset[0].data, def1.preset[0]);
		},

		"test disable/enable also the grid": function() {
			this.form.disableNonFieldElements();
			assertTrue(this.form.presetGrid.disabled);
			this.form.enableNonFieldElements();
			assertFalse(this.form.presetGrid.disabled);
		}
	});

	function getEidgetDefinition() {
		var preset = [{
			name: "pippo",
			description: "PIPPO",
			value: "asdf"
		}];

		var def = {
			id: 1,
			type: "REPORT",
			buttonLabel: "label",
			forceFormat: "CSV",
			reportCode: 1123,
			active: true,
			preset: preset
		};

		return def;
	}

	TestCase("testCMOpenReportDefinitioPresetGrid", {
		setUp: function() {
			this.grid = new CMDBuild.view.administration.widget.form.CMOpenReportDefinitionPresetGrid();
		},
	
		tearDown: function() {
			delete this.grid;
		},

		"test start with no record": function() {
			assertEquals(0, this.grid.count());
		},

		"test add nothing if data is undefined": function() {
			this.grid.fillWithData();
			assertEquals(0, this.grid.count());
		},

		"test add a record": function() {
			var data = [],
				model = CMDBuild.model.CMReportAttribute,
				attrConf = {};

			attrConf[model._FIELDS.name] = "asdf";
			attrConf[model._FIELDS.description] = "asdf";
			data.push(attrConf);

			this.grid.fillWithData(data);
			assertEquals(1, this.grid.count());
		},

		"test add a record two times": function() {
			var data = [],
				model = CMDBuild.model.CMReportAttribute,
				attrConf = {};

			attrConf[model._FIELDS.name] = "asdf";
			attrConf[model._FIELDS.description] = "asdf";
			data.push(attrConf);

			this.grid.fillWithData(data);
			assertEquals(1, this.grid.count());
			this.grid.fillWithData(data);
			assertEquals(1, this.grid.count());
		},

		"test add 2 records two times": function() {
			var data = getDataWithTwoRecord();

			this.grid.fillWithData(data);
			assertEquals(2, this.grid.count());
			this.grid.fillWithData(data);
			assertEquals(2, this.grid.count());
		},

		"test getData": function() {
			var fields = CMDBuild.model.CMReportAttribute._FIELDS;
			var data = getDataWithTwoRecord();
			this.grid.fillWithData(data);

			var data1 = this.grid.getData();
			assertEquals(2, data1.length);
			// Ext transform the array of {} in an array of Model objects, so access
			// to the data with the get method
			assertEquals(data[0].get(fields.name), data1[0][fields.name]);
			assertEquals(data[0].get(fields.description), data1[0][fields.description]);
			assertEquals(data[0].get(fields.value), data1[0][fields.value]);

			assertEquals(data[1].get(fields.name), data1[1][fields.name]);
			assertEquals(data[1].get(fields.description), data1[1][fields.description]);
			assertEquals(data[1].get(fields.value), data1[1][fields.value]);
		}
	});

	function getDataWithTwoRecord() {
		var data = [],
		model = CMDBuild.model.CMReportAttribute,
		attrConf = {}, attrConf1 = {};

		attrConf[model._FIELDS.name] = "asdf";
		attrConf[model._FIELDS.description] = "asdf";
		attrConf[model._FIELDS.value] = "asdf";
		data.push(attrConf);
	
		attrConf1[model._FIELDS.name] = "ffff";
		attrConf1[model._FIELDS.description] = "ffff";
		attrConf1[model._FIELDS.value] = "ffff";
		data.push(attrConf1);

		return data;
	}
})();