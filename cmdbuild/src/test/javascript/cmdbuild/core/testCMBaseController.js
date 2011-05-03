(function() {
	TestCase("testCMBaseController", {
		setUp: function() {},
		tearDown: function() {},
		"test fail instantiate without view": function() {
			try {
				var c = new CMDBuild.core.CMBaseController();
				fail("A controller was instantiate vithout view")
			} catch (e) {
				assertEquals(CMDBuild.core.error.controller.NO_VIEW(CMDBuild.core.CMBaseController.NAME), e);
			}
		},
		"test constructor add the listener methods": function() {
			var c = new CMDBuild.core.CMBaseController({
				view: {},
				listeners: {
					onA: function(){},
					onB: function(){}
				}
			});
			
			assertFunction(c.onA);
			assertFunction(c.onB);
		},
		"test subclasses definition": function() {
			var C1 = function(conf) {
				this.m = function() {};
				C1.superclass.constructor.call(this, conf);
			}

			Ext.extend(C1, CMDBuild.core.CMBaseController);

			var c1 = new C1({
				view: {},
				listeners: {
					onA: function(){}
				}
			});

			assertFunction(c1.onA);
			assertFunction(c1.m);
		}
	});
})();