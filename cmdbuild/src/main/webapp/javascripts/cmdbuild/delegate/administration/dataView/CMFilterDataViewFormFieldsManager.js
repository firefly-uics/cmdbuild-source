(function() {
	Ext.define("CMDBuild.delegate.common.field.CMFilterChooserWindowDelegate", {
		/**
		 * @params {CMDBuild.view.common.field.CMFilterChooserWindow}
		 * filterWindow the window that call the delegate
		 * @params {Ext.data.Model} filter
		 * the selected record
		 */
		onCMFilterChooserWindowRecordSelect: function(filterWindow, filter) {}
	});

	Ext.define("CMDBuild.view.common.field.CMFilterChooserWindow", {
		extend: "CMDBuild.PopupWindow",

		// configuration
		store: undefined,
		// configuration

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.delegate.common.field.CMFilterChooserWindowDelegate");

			this.callParent(arguments);
		},

		initComponent: function() {
			var me = this;
			var store = _CMProxy.Filter.newSystemStore();
			var grid = new Ext.grid.Panel({
				autoScroll: true,
				store: store,
				border: false,
				frame: false,
				columns: [{
					header: CMDBuild.Translation.name,
					dataIndex: "name",
					flex: 1
				}, {
					header: CMDBuild.Translation.description_,
					dataIndex: "description",
					flex: 1
				}],
				bbar: new Ext.toolbar.Paging({
					store: store,
					displayInfo: true,
					displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of+' {2}',
					emptyMsg: CMDBuild.Translation.common.display_topic_none
				}),
				listeners: {
					itemdblclick: function(grid, record, item, index, e, eOpts) {
						me.callDelegates("onCMFilterChooserWindowRecordSelect", [me, record]);
					}
				}
			});

			this.title = CMDBuild.Translation.availableFilters;
			this.items = [grid];
			this.buttonAlign = "center",
			this.buttons = [{
				text: "@@ Save",
				handler: function() {
					var selection = grid.getSelectionModel().getSelection();
					if (selection.length > 0) {
						me.callDelegates("onCMFilterChooserWindowRecordSelect", [me, selection[0]]);
					}

					me.destroy();
				}
			},{
				text: "@@ Abort",
				handler: function() {
					me.destroy();
				}
			}];

			this.callParent(arguments);
		}
	});

	var SET = "@@ Selezionato";
	var UNSET = "@@ Non Selezionato";

	/**
	 * @class CMDBuild.view.common.field.CMFilterChooser
	 */
	Ext.define("CMDBuild.view.common.field.CMFilterChooser", {
		extend: "Ext.form.FieldContainer",

		mixins: {
			filterChooserWindowDelegate: "CMDBuild.delegate.common.field.CMFilterChooserWindowDelegate",
			filterWindow: "CMDBuild.view.management.common.filter.CMFilterWindowDelegate"
		},

		// configuration
		/**
		 * Used to loads the right attributes when click to the
		 * button to add a new filter
		 */
		className: null,

		/**
		 * the filter selected
		 */
		filter: null,

		/**
		 * @see CMDBUild.view.common.CMFormFunctions.enableFields
		 * @see CMDBUild.view.common.CMFormFunctions.disableFields
		 */
		considerAsFieldToDisable: true,
		// configuration

		// override
		initComponent: function() {
			var me = this;

			this.label = new Ext.form.field.Display({
				value: SET,
				disabledCls: "cmdb-displayfield-disabled"
			});

			this.layout = "hbox",
			this.items = [
				this.label,
				{
					xtype: 'splitter'
			},{
				xtype: 'button',
				tooltip: "@@ Select a filter to the existings",
				iconCls: "add",
				scope: me,
				handler: me.onTrigger1Click
			}, {
				xtype: 'button',
				tooltip: "@@ Deselect filter",
				iconCls: "delete",
				scope: me,
				handler: me.onTrigger2Click
			}, {
				xtype: 'button',
				tooltip: "@@ Edit selected filter filter",
				iconCls: "modify",
				scope: me,
				handler: me.onTrigger3Click
			}];

			this.callParent(arguments);
		},

		/**
		 * open a window to select an existing filter
		 */
		// override
		onTriggerClick: showFilterChooser,
		onTrigger1Click: showFilterChooser,

		/**
		 * open a window to create a new filter
		 */
		// override
		onTrigger2Click: function() {
			this.reset();
		},

		/**
		 * open a window to show the configuration
		 * of the current filter
		 */
		// override
		onTrigger3Click: function() {
			var me = this;
			var filter, className;

			if (this.filter) {
				filter = this.filter;
				className = this.filter.getEntryType();
			} else {
				if (this.className) {
					className = this.className;
				} else {
					// if here there are we have neither filter
					// and className, so return because is not
					// possible to add a new filter and is not
					// possible to update a filter
					return;
				}

				filter = new CMDBuild.model.CMFilterModel({
					entryType: className,
					local: true,
					name: CMDBuild.Translation.management.findfilter.newfilter + " " + _CMUtils.nextId()
				});
			}

			var entryType = _CMCache.getEntryTypeByName(className);

			_CMCache.getAttributeList(entryType.getId(), function(attributes) {

				var filterWindow = new CMDBuild.view.common.filter.CMFilterConfigurationWindow({
					filter: filter,
					attributes: attributes,
					className: className
				});

				filterWindow.addDelegate(me);
				filterWindow.show();

			});
		},

		setClassName: function(className) {
			this.className = className;
		},

		setFilter: function(filter) {
			this.filter = filter;
			if (filter == null) {
				this.label.setValue(UNSET);
			} else {
				this.label.setValue(SET);
			}
		},

		reset: function() {
			this.setFilter(null);
		},

		getFilter: function() {
			return this.filter;
		},

		disable: function() {
			this.items.each(function(i) {
				i.disable();
			});
		},

		enable: function() {
			this.items.each(function(i) {
				i.enable();
			});
		},

		// as filterChooserWindowDelegate

		/**
		 * @params {CMDBuild.view.common.field.CMFilterChooserWindow}
		 * filterWindow the window that call the delegate
		 * @params {Ext.data.Model} filter
		 * the selected record
		 */
		// override
		onCMFilterChooserWindowRecordSelect: function(filterWindow, filter) {
			this.setFilter(filter);
			filterWindow.destroy();
		},

		// as filterWindowDelegate

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 * @params {CMDBuild.model.CMFilterModel} filter
		 * The filter to save
		 */
		onCMFilterWindowSaveButtonClick: function(filterWindow, filter) {
			this.onCMFilterChooserWindowRecordSelect(filterWindow, filter);
		},

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowAbortButtonClick: function(filterWindow) {
			filterWindow.destroy();
		}
	});

	function showFilterChooser() {
		var chooserWindow = new CMDBuild.view.common.field.CMFilterChooserWindow({
			store: this.store
		}).show();

		chooserWindow.addDelegate(this);
	}




	var FILTER = _CMProxy.parameter.FILTER;
	var SOURCE_CLASS_NAME = _CMProxy.parameter.SOURCE_CLASS_NAME;

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
				name: SOURCE_CLASS_NAME,
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

			this.filterChooser = new CMDBuild.view.common.field.CMFilterChooser({
				fieldLabel: "@@ Filter",
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: FILTER
			});

			fields.push(this.classes);
			fields.push(this.filterChooser);

			return fields;
		},

		setFilterChooserClassName: function(className) {
			this.filterChooser.setClassName(className);
		},

		/**
		 * 
		 * @param {Ext.data.Model} record
		 * the record to use to fill the field values
		 */
		// override
		loadRecord: function(record) {
			this.callParent(arguments);
			var filterConfiguration = record.get(FILTER);
			var className = record.get(SOURCE_CLASS_NAME);

			this.filterChooser.setFilter(new CMDBuild.model.CMFilterModel({
				configuration: filterConfiguration,
				entryType: className
			}));

			this.classes.setValue(className);
			// the set value programmatic does not fire the select
			// event, so call the delegates manually
			this.callDelegates("onFilterDataViewFormBuilderClassSelected", [this, className]);
		},

		/**
		 * @return {object} values
		 * a key/value map with the values of the fields
		 */
		// override
		getValues: function() {
			var values = this.callParent(arguments);

			values[SOURCE_CLASS_NAME] = this.classes.getValue();
			var filter = this.filterChooser.getFilter();
			if (filter) {
				values[FILTER] = Ext.encode(filter.getConfiguration());
			}

			return values;
		},

		/**
		 * clear the values of his fields
		 */
		// override
		reset: function() {
			this.callParent(arguments);
			
			this.classes.reset();
			this.filterChooser.reset();
		}
	});
})();
