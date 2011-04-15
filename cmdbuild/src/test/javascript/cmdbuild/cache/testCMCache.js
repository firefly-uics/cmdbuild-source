(function() {
	TestCase("testCMCache", {
		setUp: function() {
			this._cmCache = new CMDBuild.cache.CMCache();
		},
		tearDown: function() {
			delete this._cmCache;
			CMDomainModelLibrary.clear();
		},
		"test thet setTable fill the CMDomainLibrary with the relative domains": function() {
			assertEquals(0, CMDomainModelLibrary.count());
			this._cmCache.setTables(getAllClasses());
			assertEquals(3, CMDomainModelLibrary.count());
		}
	});
	
	function getAllClasses() {
		return [ {
			type: "class",
			text : "Posto di lavoro",
			parent: 1585763,
			priv_write: true,
			priv_create: true,
			meta: {
				geoAttributes: [],
				"runtime.username": "admin",
				"runtime.groupname": "SuperUser",
				"runtime.privileges": "WRITE"
			},
			id: 1586051,
			superclass: false,
			selectable: true,
			name: "PDL",
			active: true,
			tableType: "standard",
			domains : [ {
				class1id : 1585805,
				priv_write : true,
				inherited : false,
				classType : "class",
				priv_create : true,
				class2id : 1586051,
				meta : {
					"runtime.username" : "admin",
					"runtime.groupname" : "SuperUser",
					"runtime.privileges" : "WRITE"
				},
				idDomain : 1585882,
				class1 : "Dipendente",
				md : false,
				description : "Assegnazione",
				class2 : "Posto di lavoro",
				name : "Assegnazione",
				descrdir : "utilizza",
				descrinv : "utilizzato da",
				active : true,
				origName : "Assegnazione",
				cardinality : "N:N"
			}, {
				class1id : 1586051,
				priv_write : true,
				inherited : false,
				classType : "class",
				priv_create : true,
				class2id : 1585780,
				meta : {
					"runtime.username" : "admin",
					"runtime.groupname" : "SuperUser",
					"runtime.privileges" : "WRITE"
				},
				idDomain : 1585893,
				class1 : "Posto di lavoro",
				md : false,
				description : "ComposizionePDL",
				class2 : "Item",
				name : "ComposizionePDL",
				descrdir : "comprende",
				descrinv : "fa parte di",
				active : true,
				origName : "ComposizionePDL",
				cardinality : "1:N"
			}, {
				class1id : 1586203,
				priv_write : true,
				inherited : false,
				classType : "class",
				priv_create : true,
				class2id : 1586051,
				meta : {
					"runtime.username" : "admin",
					"runtime.groupname" : "SuperUser",
					"runtime.privileges" : "WRITE"
				},
				idDomain : 1585970,
				class1 : "Stanza",
				md : false,
				description : "Stanza PDL",
				class2 : "Posto di lavoro",
				name : "StanzaPDL",
				descrdir : "contiene",
				descrinv : "si trova in",
				active : true,
				origName : "StanzaPDL",
				cardinality : "1:N"
			} ]
		} ];
	}
})();