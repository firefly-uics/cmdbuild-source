(function() {

	describe("CMDBUild.cache.CMCacheDashboardFunctions", function() {

		it("Starts with no dashboards", function() {
			var dashboards = _CMCache.getDashboards();
			expect(dashboards).toEqual({});
		});

		it("Return null if has no dashboard for a given id", function() {
			var d = _CMCache.getDashboardById(1);
			expect(d).toBeNull();
		});

		it("Retrieves added dashboard", function() {
			_CMCache.addDashboard(fooDashboardConfig());

			var d = _CMCache.getDashboardById(1);
			expect(d).toBeDefined();
			expect(d.get("name")).toEqual("foo");
		});

		it("Retrieves a dashboard adding an array of dashboards", function() {
			_CMCache.addDashboards([{
				id: 1,
				name: "foo",
				description: "Amazing dashboard for amazing people",
				charts: []
			}, {
				id: 2,
				name: "bar",
				description: "Cool dashboard for cool people",
				charts: []
			}]);

			var d = _CMCache.getDashboardById(2);
			expect(d).toBeDefined();
			expect(d.get("name")).toEqual("bar");
		});

		it("Is able to modify a dashboard", function() {
			_CMCache.addDashboards([{
				id: 1,
				name: "foo",
				description: "Amazing dashboard for amazing people",
				charts: [],
				groups: []
			}, {
				id: 2,
				name: "bar",
				description: "Cool dashboard for cool people",
				charts: []
			}]);

			_CMCache.modifyDashboard({
				id: 1,
				name: "Bar",
				description: "Bar",
				charts: [],
				groups: [0,1]
			});

			var d = _CMCache.getDashboardById(1);
			expect(d).toBeDefined();
			expect(d.getName()).toEqual("Bar");
			expect(d.getDescription()).toEqual("Bar");
			expect(d.getGroups()).toEqual([0,1]);
		});

		it("Fire an event to notify listeners of a new dashboard", function() {
			var callback = jasmine.createSpy();
			var dashboardConfig = fooDashboardConfig();

			_CMCache.on(_CMCache.DASHBOARD_EVENTS.add, callback, this);
			_CMCache.addDashboard(dashboardConfig);

			expect(callback).toHaveBeenCalled();
			var args = callback.argsForCall[0];
			expect(args[0].data).toEqual(dashboardConfig);
		})

		it("Remove a dashboard of a given id", function() {
			_CMCache.addDashboard(fooDashboardConfig());
			var cachedData = _CMCache.getDashboardById(1);
			expect(cachedData).toBeDefined();

			_CMCache.removeDashboardWithId(1);
			var cachedData = _CMCache.getDashboardById(1);
			expect(cachedData).toBeNull();
		});

		it("Fire an event to notify listeners of a removed dashboard", function() {
			var callback = jasmine.createSpy();

			_CMCache.addDashboard(fooDashboardConfig());
			_CMCache.on(_CMCache.DASHBOARD_EVENTS.remove, callback, this);
			_CMCache.removeDashboardWithId(1)

			expect(callback).toHaveBeenCalled();
			var args = callback.argsForCall[0];
			expect(args[0]).toEqual(1);
		});

		it("Fire an event to notify listeners of a modified dashboard", function() {
			var onModify = jasmine.createSpy("onModify");

			_CMCache.addDashboard(fooDashboardConfig());
			_CMCache.on(_CMCache.DASHBOARD_EVENTS.modify, onModify, this);
			_CMCache.modifyDashboard({
				id: 1,
				name: "Bar",
				description: "Bar"
			});

			expect(onModify).toHaveBeenCalled();
			var arg = onModify.mostRecentCall.args[0];
			expect(arg.getName()).toEqual("Bar");
			expect(arg.getDescription()).toEqual("Bar");
		});
	});

	function fooDashboardConfig() {
		return {
			id: 1,
			name: "foo",
			description: "Amazing dashboard for amazing people",
			charts: []
		};
	}
})();