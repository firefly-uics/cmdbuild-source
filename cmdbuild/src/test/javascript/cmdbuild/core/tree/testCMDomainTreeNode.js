(function() {
	TestCase("testCMDomainTreeNode", {
		setUp: function() {},
		tearDown: function() {},
		"test constructor fail without conf": function() {
			try {
				var n = new CMDBuild.core.tree.CMDomainTreeNode();
				fail("Istantiated a CMDomainTreeNode without conf");
			} catch (e) {
				assertEquals(CMDBuild.core.error.tree.NO_CONFIGURATION("CMDBuild.core.tree.CMDomainTreeNode"), e);
			}
		},
		"test constructor fail if passed model is not a CMDomainModel": function() {
			try {
				var n = new CMDBuild.core.tree.CMDomainTreeNode({
					NAME: "Foo"
				});
				fail("Istantiated a CMDomainTreeNode but the passed model is not a CMDomainModel");
			} catch (e) {
				assertEquals(CMDBuild.core.error.tree.WRONG_CMDOMAIN_MODEL, e);
			}
		},
		"test a right instantiation": function() {
			var domain = CMDBuild.core.model.CMDomainModel.buildFromJSON(getADomain());
			var n = CMDBuild.core.tree.CMDomainTreeNode(domain);
			
			assertSame(domain, n.getCMModel());
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