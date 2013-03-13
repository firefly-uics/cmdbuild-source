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
})();