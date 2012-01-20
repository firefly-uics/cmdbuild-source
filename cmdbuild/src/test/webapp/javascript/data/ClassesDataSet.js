Ext.define("CMDBuild.test.data.ClassesDataSet", {
	statics : {
		getClassesForCache : function() {
			return [ {
				"id" : 1111,
				"text" : "Class",
				"superclass" : true,
				"selectable" : false,
				"name" : "Class",
				"tableType" : "standard",
				"active" : true,
				"priv_write" : true,
				"priv_create" : false,
				"type" : "class",
				"meta" : {
					"geoAttributes" : [],
					"runtime.username" : "m.valenti",
					"runtime.groupname" : "SupportoCMDBuild",
					"runtime.privileges" : "WRITE"
				}
			}, {
				"id" : 1112,
				"parent": 1111,
				"text" : "C1",
				"superclass" : true,
				"selectable" : true,
				"name" : "C1",
				"tableType" : "standard",
				"active" : true,
				"priv_write" : true,
				"priv_create" : false,
				"type" : "class",
				"meta" : {
					"geoAttributes" : [],
					"runtime.username" : "m.valenti",
					"runtime.groupname" : "SupportoCMDBuild",
					"runtime.privileges" : "WRITE"
				}
			}, {
				"id" : 1113,
				"parent": 1112,
				"text" : "C2",
				"superclass" : false,
				"selectable" : true,
				"name" : "C2",
				"tableType" : "standard",
				"active" : true,
				"priv_write" : true,
				"priv_create" : true,
				"type" : "class",
				"meta" : {
					"geoAttributes" : [],
					"runtime.username" : "m.valenti",
					"runtime.groupname" : "SupportoCMDBuild",
					"runtime.privileges" : "WRITE"
				}
			},{
				"id" : 1114,
				"parent": 1111,
				"text" : "C3",
				"superclass" : true,
				"selectable" : true,
				"name" : "C3",
				"tableType" : "standard",
				"active" : true,
				"priv_write" : true,
				"priv_create" : false,
				"type" : "class",
				"meta" : {
					"geoAttributes" : [],
					"runtime.username" : "m.valenti",
					"runtime.groupname" : "SupportoCMDBuild",
					"runtime.privileges" : "WRITE"
				}
			},{
				"id" : 1115,
				"parent": 1114,
				"text" : "C4",
				"superclass" : false,
				"selectable" : true,
				"name" : "C4",
				"tableType" : "standard",
				"active" : true,
				"priv_write" : true,
				"priv_create" : true,
				"type" : "class",
				"meta" : {
					"geoAttributes" : [],
					"runtime.username" : "m.valenti",
					"runtime.groupname" : "SupportoCMDBuild",
					"runtime.privileges" : "WRITE"
				}
			},{
				"id" : 1116,
				"parent": 1114,
				"text" : "C5",
				"superclass" : false,
				"selectable" : true,
				"name" : "C5",
				"tableType" : "standard",
				"active" : true,
				"priv_write" : true,
				"priv_create" : true,
				"type" : "class",
				"meta" : {
					"geoAttributes" : [],
					"runtime.username" : "m.valenti",
					"runtime.groupname" : "SupportoCMDBuild",
					"runtime.privileges" : "WRITE"
				}
			}];
		}
	}
});