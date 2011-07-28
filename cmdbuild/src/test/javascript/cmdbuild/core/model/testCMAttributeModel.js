(function() {
	TestCase("testCMAttributeModel", {
		setUp: function() {},
		tearDown: function() {},
		"test instatiate a CMAttributeModel": function() {
			var conf = {
				active: true,
				description: "Foo",
				name: "Foo",
				unique: true,
				shownAsGridColumn: false,
				editingMode: "write",
				type: "STRING",
				stringLength: 20,
				notnull: true
			}
			
			var a = new CMDBuild.core.model.CMAttributeModel(conf);
		},
		"test bulidFromJson": function() {
			var json = {
				scale: 0,
				index: 1,
				precision: 0,
				inherited: true,
				len: 100,
				type: "STRING",
				meta: {},
				fieldmode_value: "Modificabile",
				isactive: true,
				idClass: 1604679,
				description: "Code",
				name: "Code",
				absoluteClassOrder: 10000,
				isnotnull: false,
				isbasedsp: true,
				fieldmode: "write",
				isunique: false,
				classOrderSign: 0
			}
			var a = CMDBuild.core.model.CMAttributeModel.buildFromJson(json);
			assertObject(a);
			assertEquals(json.name, a.getname());
			assertEquals(json.isnotnull, a.getnotnull());
		}
	});
})();