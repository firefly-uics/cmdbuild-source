(function() {
	var list,
		server;

	describe('CMDBuild.view.common.field.CMGroupSelectionList', function() {

		beforeEach(function() {
			list = new CMDBuild.view.common.field.CMGroupSelectionList();
			server = CMDBuild.test.CMServer.create({
				this.store  = new Ext.data.Store( {
					model: "CMGroupModelForList",
					proxy : {
						type : "ajax",
						url : 'services/json/management/modreport/getgroups',
						reader : {
							type : "json",
							root : "rows"
						}
					},
					autoLoad : false
				});
			});

			// return a fake gropus list
			server.bindUrl("services/json/management/modreport/getgroups", function(params) {
				return {
					"success":true,
					"rows":[
						{"id":1,"description":"SuperUser"},
						{"id":2,"description":"Helpdesk"}
					]
				}
			});
		});

		afterEach(function() {
			server.restore();
			delete list;
		});

		it('sets value even if not in store', function() {
			list.setValue([7]);
			expect(list.getValue().toEqual([7]);
		});

		it('store load clean selection', function() {
			list.setValue([1]);
			list.store.load();
			expect(list.getValue().toEqual([]);
		}
	});
})();
