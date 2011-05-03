(function() {
	TestCase("testCMDomainTree", {
		setUp: function() {},
		tearDown: function() {},
		"test constructor fail without conf": function() {
			try {
				var n = new CMDBuild.core.tree.CMDomainTree();
				fail("Istantiated a CMDomainTreeNode without conf");
			} catch (e) {
				assertEquals(CMDBuild.core.error.tree.NO_CONFIGURATION("CMDBuild.core.tree.CMDomainTree"), e);
			}
		},
		"test constructor fail for a wrong library": function() {
			try {
				var n = new CMDBuild.core.tree.CMDomainTree({
					NAME: "Foo"
				});
				fail("Istantiated a CMDomainTree but the passed library is not a CMDomainModelLibrary");
			} catch (e) {
				assertEquals(CMDBuild.core.error.tree.WRONG_CMMODEL_LIBRARY, e);
			}
		},
		"test tree for a empty library has no children": function() {
			var domainLibrary = new CMDBuild.core.model.CMDomainModelLibrary();
			var domainTree = CMDBuild.core.tree.CMDomainTree(domainLibrary);
			
			assertNotUndefined(domainTree);
			assertFalse(domainTree.hasChildNodes());
		},
		"test tree has a child": function() {
			var domainLibrary = new CMDBuild.core.model.CMDomainModelLibrary();
			var domain = CMDBuild.core.model.CMDomainModel.buildFromJSON(getADomain());
			domainLibrary.add(domain);
			var domainTree = CMDBuild.core.tree.CMDomainTree(domainLibrary);
			assertTrue(domainTree.hasChildNodes());
			assertSame(domain, domainTree.firstChild.getCMModel());
		}
	});
	
	function getADomain() {
		return {
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
		};
	}
	
})();