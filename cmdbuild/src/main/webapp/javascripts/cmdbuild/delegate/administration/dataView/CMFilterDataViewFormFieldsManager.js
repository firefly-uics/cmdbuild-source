(function() {
	
	function getStoredFilter(store, filter) {
		var storedFilter = null;
		var recordIndex = store.findBy(function(record) {
			return (filter.getName() == record.getName()) && (filter.dirty == record.dirty);
		});
		if (recordIndex >= 0) {
			storedFilter = store.getAt(recordIndex);
		}

		return storedFilter;
	}
})();
Ext.define("CMDBuild.delegate.administration.common.dataview.CMFilterDataViewFormFieldsManager", {
	extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseFormFiledsManager",

	mixins: {
		delegable: "CMDBuild.core.CMDelegable"
	},

	constructor: function() {
		this.mixins.delegable.constructor.call(this,
				"CMDBuild.delegate.administration.common.dataview.CMFilterDataViewFormDelegate");

		this.callParent(arguments);
	},

	/**
	 * @return {array} an array of Ext.component to use as form items
	 */
	build: function() {
		var me = this;

		var fields = this.callParent(arguments);

		this.classes = new CMDBuild.field.ErasableCombo({
			fieldLabel: CMDBuild.Translation.targetClass,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name: 'class',
			valueField: 'name',
			displayField: 'description',
			editable: false,
			store: _CMCache.getClassesAndProcessesAndDahboardsStore(),
			queryMode: 'local',
			listeners: {
				select: function(combo, records, options) {
					var className = null;
					if (Ext.isArray(records) 
							&& records.length > 0) {
						var record = records[0];
						className = record.get(me.classes.valueField);
					}

					me.callDelegates("onFilterDataViewFormBuilderClassSelected", [me, className]);
				}
			}
		});

		var newCustomFilterButton = Ext.button.Button({
			text: CMDBuild.Translation.addFilter,
			iconCls: "add",
			handler: function() {
				me.callDelegates("onFilterDataViewFormBuilderAddFilterButtonClick", me);
			}
		});

		this.filterStore = _CMProxy.Filter.newSystemStore();

		this.filterGrid = new Ext.grid.Panel({
			title: CMDBuild.Translation.availableFilters,
			minHeight: 200,
			autoScroll: true,
			store: this.filterStore,
			columns: [{
				header: CMDBuild.Translation.name,
				dataIndex: "name",
				flex: 1
			}, {
				header: CMDBuild.Translation.description_,
				dataIndex: "description",
				flex: 1
			}],
			tbar: [newCustomFilterButton],
			bbar: new Ext.toolbar.Paging({
				store: this.filterStore,
				displayInfo: true,
				displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of+' {2}',
				emptyMsg: CMDBuild.Translation.common.display_topic_none
			}),
			listeners: {
				select: function(grid, record, index, option) {
					me.callDelegates("onFilterDataViewFormBuilderFilterSelected", [me, grid, record]);
				}
			},

			considerAsFieldToDisable: true // to be disabled as a field (see CMFormFunctions)
		});

		fields.push(this.classes);
		fields.push(this.filterGrid);

		return fields;
	},

	/**
	 * 
	 * @param {Ext.data.Model} record
	 * the record to use to fill the field values
	 */
	// override
	loadRecord: function(record) {
		this.reset();
		this.name.setValue(record.get(_CMProxy.parameter.NAME));
		this.description.setValue(record.get(_CMProxy.parameter.DESCRIPTION));

		var className = record.get(_CMProxy.parameter.CLASS_NAME);
		this.classes.setValue(className);
		// the set value programmatic does not fire the select
		// event, so call the delegates manually
		this.callDelegates("onFilterDataViewFormBuilderClassSelected", [this, className]);
	},

	/**
	 * clear the values of his fields
	 */
	// override
	reset: function() {
		this.name.reset();
		this.description.reset();
		this.classes.reset();
	},

	selectFilter: function(filter) {
		var sm = this.filterGrid.getSelectionModel();
		var recordIndex = this.filterStore.findBy(function(record) {
			return filter.getId() == record.getId();
		});

		if (sm && recordIndex >= 0) {
			sm.select(recordIndex);
		}
	}
});
