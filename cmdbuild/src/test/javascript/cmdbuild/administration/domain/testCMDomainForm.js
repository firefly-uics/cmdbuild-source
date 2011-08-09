(function() {
	TestCase("testCMDomainForm", {
		setUp: function() {
			this.form = new CMDBuild.administration.domain.CMDomainForm();
		},
		tearDown: function() {
			delete this.form;
		},
		"test fillWithModel fail if the model is not a CMDomainModel": function() {
			try {
				this.form.fillWithModel({NAME: "Foo"});
				fail("the fillWithModel must for non CMDomainModel")
			} catch (e) {
				assertEquals(CMDBuild.core.error.form.WRONG_MODEL(CMDBuild.core.model.CMDomainModel.NAME), e);
			}
		},
		"test fillWithModel for a right model": function() {
			var domain = CMDBuild.core.model.CMDomainModel.buildFromJSON(getJSONDomain());
			var ds = domain.STRUCTURE;
			this.form.fillWithModel(domain);
			var basicForm = this.form.getForm();
			
			function assertValuesMatch(name) {
				var formValue = basicForm.findField(name).getValue();
				assertEquals(domain["get"+name](), formValue);
			}
			
			assertValuesMatch(ds.id.name);
			assertValuesMatch(ds.active.name);
			assertValuesMatch(ds.directDescription.name);
			assertValuesMatch(ds.reverseDescription.name);
			assertValuesMatch(ds.description.name);
			assertValuesMatch(ds.isMasterDetail.name);
			assertValuesMatch(ds.cardinality.name);
			assertValuesMatch(ds.name.name);
			assertValuesMatch(ds.idClass1.name);
			assertValuesMatch(ds.idClass2.name);
		}
	});

	function getJSONDomain() {
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