(function() {
	var filterStore = new Ext.data.Store({
		model: "CMDBuild.model.CMFilterModel",
		data: [{
			name: "Filtro Classe 1",
			description: "Bla bla classe 1",
			entryType: "Classe1",
			configuration: {
				attribute: {
					and: [{
						or: [{
							simple: {
								attribute: "Code",
								operator: "contain",
								value: ["01"]
							}
						},{
							simple: {
								attribute: "Code",
								operator: "contain",
								value: ["02"]
							}
						}]
					}, {
						simple: {
							attribute: "Description",
							operator: "contain",
							value: ["The"]
						}
					}] 
				}
			}
		}, {
			name: "Filtro Classe 2",
			description: "Bla bla bla classe 2",
			entryType: "Classe2",
			configuration: {
				attribute: {
					simple: {
						attribute: "Code",
						operator: "contain",
						value: ["02"]
					}
				}
			}
		}]
	});

	Ext.define("CMDBuild.cache.CMChcheFilterFunctions", {

		getAvailableFilterStore: function(entryTypeName) {
			return filterStore;
		},

		filterStoreByEntryTypeName: function(entryTypeName) {
			var filterBy = entryTypeName || "";

			filterStore.clearFilter();
			filterStore.filter("entryType", filterBy);
		},

		/**
		 * Add the given filter to the store,
		 * if already exists a filter with the same
		 * name, remove the current filter and then add
		 * the newest
		 * 
		 * @param {CMDBuild.model.CMFilterModel} filter
		 * the filter to add
		 * 
		 * @param {boolean} atFirst
		 * if true, add this filter as firs of the store
		 */
		addFilter: function(filter, atFirst) {
			if (Ext.getClassName(filter) == "CMDBuild.model.CMFilterModel") {
				removeFilterIfAlreadyExists(this, filter);

				var store = this.getAvailableFilterStore();
				if (atFirst) {
					store.insert(0, filter);
				} else {
					store.add(filter);
				}
			}
		},

		updateFilter: function(filter) {
			if (Ext.getClassName(filter) == "CMDBuild.model.CMFilterModel") {
				var storedFilter = getStoredFilter(this, filter);
				if (storedFilter) {
					filterStore.remove(storedFilter);
					filterStore.add(filter);
				}
			}
		},

		removeFilter: function(filter) {
			if (Ext.getClassName(filter) == "CMDBuild.model.CMFilterModel") {
				var storedFilter = getStoredFilter(this, filter);
				if (storedFilter) {
					filterStore.remove(storedFilter);
				}
			}
		},

		setFilterApplied: function(filter, applied) {
			var storedFilter = getStoredFilter(this, filter);
			if (storedFilter) {
				storedFilter.setApplied(applied);
			}
		}
	});

	function removeFilterIfAlreadyExists(me, filter) {
		var s = me.getAvailableFilterStore();
		var storedFilter = getStoredFilter(me, filter);

		if (storedFilter) {
			s.remove(storedFilter);
		}
	}

	function getStoredFilter(me, filter) {
		var storedFilter = null;
		var s = me.getAvailableFilterStore();
		var recordIndex = s.findBy(function(record) {
			return (filter.getName() == record.getName()) && (filter.dirty == record.dirty);
		});
		if (recordIndex >= 0) {
			storedFilter = s.getAt(recordIndex);
		}

		return storedFilter;
	}

	function getFilterByName(me, name, checkDirty) {
		var s = me.getAvailableFilterStore();
		return s.findRecord("name", name);
	}
})();