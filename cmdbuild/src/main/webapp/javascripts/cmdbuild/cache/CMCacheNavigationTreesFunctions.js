(function() {

	var navigationTrees = null;
	Ext.define("CMDBUild.cache.CMCacheNavigationTreesFunctions", {

		observers: [],
		lastEntry: "",
		listNavigationTrees: function(p) {
			p.method = "GET";
			var appSuccess = p.success;
			p.success = function(response, options, decoded) {
				navigationTrees = decoded.response;
				appSuccess(response, options, decoded);
			};
			p.url = CMDBuild.core.proxy.Index.navigationTrees.readAll;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		registerOnNavigationTrees: function(observer) {
			this.observers.push(observer);
		},

		//private
		refreshObserversNavigationTrees: function() {
			for (var i = 0; i < this.observers.length; i++) {
				this.observers[i].refresh();
			}
		},

		getNavigationTrees: function() {
			var nt = [];
			for (nav in navigationTrees) {
				// This "if" is for not have to change all the Gis module
				// that is actually based on the cabled name of the tree of navigation
				if (navigationTrees[nav].name != "gisnavigation") {
					nt.push(navigationTrees[nav]);
				}
			}
			return {
				data: nt,
				lastEntry: this.lastEntry
			};
		},

		saveNavigationTrees: function(formData, success) {
			CMDBuild.core.proxy.NavigationTree.update({
				params: formData,
				success: function(operation, request, decoded) {
					CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerUpdateStore', { identifier: 'navigationtree' });

					success(operation, request, decoded);
				}
			});
		},

		createNavigationTrees: function(formData, success) {
			var me = this;
			this.lastEntry = formData.name;
			CMDBuild.core.proxy.NavigationTree.create({
				params: formData,
				success: function(operation, request, decoded) {
					CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerUpdateStore', { identifier: 'navigationtree' });

					me.listNavigationTrees({
						success: function() {
							me.refreshObserversNavigationTrees();
							success(operation, request, decoded);
						},
						callback:  Ext.emptyFn
					});
				}
			});
		},

		readNavigationTrees: function(me, name, success) {
			CMDBuild.core.proxy.NavigationTree.read({
				params: {
					name: name
				},
				success: function(operation, request, decoded) {
					me.tree = Ext.JSON.decode(decoded.response);
					success(me, me.tree);
				}
			});
		},

		removeNavigationTrees: function(name, success) {
			var me = this;
			this.lastEntry = "";
			CMDBuild.core.proxy.NavigationTree.remove({
				params: {
					name: name
				},
				success: function(operation, request, decoded) {
					CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionDeselect', 'navigationtree');
					CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerUpdateStore', { identifier: 'navigationtree' });

					me.listNavigationTrees({
						success: function() {
							me.refreshObserversNavigationTrees();
						},
						callback:  Ext.emptyFn
					});
				}
			});
		},

	});

})();