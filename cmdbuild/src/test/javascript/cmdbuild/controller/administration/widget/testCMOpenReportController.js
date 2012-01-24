(function() {

	TestCase("testCMOpenReportController", {

		setUp: function() {
			this.view = {
				on: function() {},
				forceFormatCheck: {
					setValue: function() {}
				},
				forceFormatOptions: {
					setDisabled: function() {},
					getValue: function() { return "asdf"; }
				},
				active: {
					setValue: function() {}
				},
				fillWithModel: function() {}
			};

			this.controller = new CMDBuild.controller.administration.widget.CMOpenReportController({
				view: this.view
			});
		},

		tearDown: function() {
			delete this.view;
			delete this.controller;
		},

		"test fillFormWithData call fillWithModel of the view if has a real model": function() {
			var fillWithModel = sinon.spy(this.view, "fillWithModel");
			this.controller.fillFormWithModel("asdf");
			assertFalse(fillWithModel.called);
			this.controller.fillFormWithModel({});
			assertFalse(fillWithModel.called);
			this.controller.fillFormWithModel(new CMDBuild.model.CMWidgetDefinitionModel());
			assertTrue(fillWithModel.called);

			fillWithModel.restore();
		},

		"test setDefaultValues": function() {
			var spy = sinon.spy(this.view.active, "setValue");
			this.controller.setDefaultValues();
			assertTrue(spy.calledWith(true));
			spy.restore();
		}

	});
})();