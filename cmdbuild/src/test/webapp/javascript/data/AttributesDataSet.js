Ext.define("CMDBuild.test.data.AttributesDataSet", {
	statics : {
		getAttributesFor : function(classId) {
			function attribute(conf) {
				var baseObj = {
					"index" : 1,
					"inherited" : false,
					"type" : "TEXT",
					"meta" : {},
					"fieldmode_value" : "Editable",
					"isactive" : true,
					"description" : "",
					"name" : "",
					"absoluteClassOrder" : 10000,
					"isnotnull" : false,
					"isbasedsp" : true,
					"fieldmode" : "write",
					"isunique" : false,
					"classOrderSign" : 0
				};

				return Ext.apply(baseObj, conf);
			}
			var attributes = {};

			// C1: C11, C12
			attributes[1112] = function() {
				return [attribute({
					idclass: 1112,
					index: 1,
					name: "C11",
					description: "C11"
				}),attribute({
					idclass: 1112,
					index: 2,
					name: "C12",
					description: "C12"
				})];
			};

				// C2: C11, C12, C21
			attributes[1113] = function() {
				var base = attributes[1112]();
				return base.concat([attribute({
					idclass: 1113,
					index: 3,
					name: "C21",
					description: "C21"
				})]);
			};

			// C3: C31, C32
			attributes[1114] = function() {
				return [attribute({
					idclass: 1114,
					index: 1,
					name: "C31",
					description: "C31"
				}),attribute({
					idclass: 1114,
					index: 2,
					name: "C32",
					description: "C32"
				})];
			};

			// C4: C41
			attributes[1115] = function() {
				var base = attributes[1114]();
				return base.concat([attribute({
					idclass: 1115,
					index: 3,
					name: "C41",
					description: "C41"
				})]);
			};

			// C5: C51
			attributes[1116] = function() {
				var base = attributes[1114]();
				return base.concat([attribute({
					idclass: 1116,
					index: 3,
					name: "C51",
					description: "C51"
				})]);
			};

			return attributes[classId]();
		}
	}
});