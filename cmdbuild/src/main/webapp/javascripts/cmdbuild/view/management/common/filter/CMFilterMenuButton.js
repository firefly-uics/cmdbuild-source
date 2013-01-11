(function() {

	// Constants to identify the icons that the user
	// could click, and call the right callback
	var ACTION_CSS_CLASS = {
		saveFilter: "action-filter-save",
		modifyFilter: "action-filter-modify",
		removeFilter: "action-filter-remove",
		cloneFilter: "action-filter-clone"
	};

	var FILTER_BUTTON_LABEL = CMDBuild.Translation.management.findfilter.set_filter;
	var CLEAR_FILTER_BUTTON_LABEL = CMDBuild.Translation.management.findfilter.set_filter;

	var TOOLTIP = {
		save: CMDBuild.Translation.common.buttons.save,
		modify: CMDBuild.Translation.common.buttons.modify,
		clone: CMDBuild.Translation.common.buttons.clone,
		remove: CMDBuild.Translation.common.buttons.remove
	};

	var ICONS_PATH = {
		save: "images/icons/disk.png",
		save_disabled: "images/icons/disk_disabled.png",
		modify: "images/icons/modify.png",
		clone: "images/icons/clone.png",
		remove: "images/icons/cross.png"
	};

	Ext.define("CMDBuild.view.management.common.filter.CMFilterMenuButtonDelegate", {
		/**
		 * Called by the CMFilterMenuButton when click
		 * to the clear button
		 * 
		 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
		 * the button that calls the delegate
		 */
		onFilterMenuButtonClearActionClick: Ext.emptyFn,

		/**
		 * Called by the CMFilterMenuButton when click
		 * to the new button
		 * 
		 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
		 * the button that calls the delegate
		 */
		onFilterMenuButtonNewActionClick: Ext.emptyFn,

		/**
		 * Called by the CMFilterMenuButton when click
		 * to on the apply icon on a row of the picker
		 * 
		 * @param {object} filter, the filter to apply
		 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
		 * the button that calls the delegate
		 */
		onFilterMenuButtonApplyActionClick: Ext.emptyFn,

		/**
		 * Called by the CMFilterMenuButton when click
		 * to on the modify icon on a row of the picker
		 * 
		 * @param {object} filter, the filter to modify
		 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
		 * the button that calls the delegate
		 */
		onFilterMenuButtonModifyActionClick: Ext.emptyFn,

		/**
		 * Called by the CMFilterMenuButton when click
		 * to on the save icon on a row of the picker
		 * 
		 * @param {object} filter, the filter to save
		 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
		 * the button that calls the delegate
		 */
		onFilterMenuButtonSaveActionClick: Ext.emptyFn,

		/**
		 * Called by the CMFilterMenuButton when click
		 * to on the modify icon on a row of the picker
		 * 
		 * @param {object} filter, the filter to modify
		 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
		 * the button that calls the delegate
		 */
		onFilterMenuButtonCloneActionClick: Ext.emptyFn,

		/**
		 * Called by the CMFilterMenuButton when click
		 * to on the remove icon on a row of the picker
		 * 
		 * @param {object} filter, the filter to remove
 		 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
		 * the button that calls the delegate
		 */
		onFilterMenuButtonRemoveActionClick: Ext.emptyFn,
	});

	Ext.define("CMDBuild.view.management.common.filter.CMFilterMenuButton", {
		extend: "Ext.container.ButtonGroup",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
					"CMDBuild.view.management.common.filter.CMFilterMenuButtonDelegate");

			this.callParent(arguments);
		},

		initComponent: function() {
			this.picker = null;

			var me = this;

			this.showListButton = new Ext.button.Button({
				text: FILTER_BUTTON_LABEL,
				iconCls: 'find',
				enableToggle: true,
				toggleHandler: function(button, state) {
					showPicker(me, button, state);
				}
			});

			this.clearButton = new Ext.button.Button({
				text: CLEAR_FILTER_BUTTON_LABEL,
				iconCls: "clear_filter",
				disabled: true,
				handler: function() {
					me.callDelegates("onFilterMenuButtonClearActionClick", me);
				}
			});

			this.items = [this.showListButton, this.clearButton];

			this.callParent(arguments);

			this.on("move", function(button, x, y) {
				this.showListButton.toggle(false);
			}, this);
		},

		updatePickerPosition: function(position) {
			var box = position || this.getBox();
			if (this.picker != null) {
				this.picker.setPosition(box.x + 5, (box.y + box.height));
			}
		},

		setFilterButtonLabel: function(label) {
			var text = FILTER_BUTTON_LABEL;
			if (label) {
				text = Ext.String.ellipsis(label, 20);
			}

			this.showListButton.setText(text);
		},

		deselectPicker: function() {
			this.picker.deselect();
		},

		selectAppliedFilter: function() {
			this.picker.selectAppliedFilter();
		},

		enableClearFilterButton: function() {
			this.clearButton.enable();
		},

		disableClearFilterButton: function() {
			this.clearButton.disable();
		}
	});

	function showPicker(me, button, state) {
		if (me.picker == null) {
			me.picker = new CMDBuild.view.management.common.filter.CMFilterMenuButtonPicker({
				listeners: {
					beforeitemclick: function beforeitemclick(grid, model, htmlelement, rowIndex, event, opt) {
						var cssClassName = event.target.className;
						var callBacks = {};

						callBacks[ACTION_CSS_CLASS.saveFilter] = "onFilterMenuButtonSaveActionClick";
						callBacks[ACTION_CSS_CLASS.modifyFilter] = "onFilterMenuButtonModifyActionClick";
						callBacks[ACTION_CSS_CLASS.removeFilter] = "onFilterMenuButtonRemoveActionClick";
						callBacks[ACTION_CSS_CLASS.cloneFilter] = "onFilterMenuButtonCloneActionClick";

						if (typeof callBacks[cssClassName] == "string") {
							me.callDelegates(callBacks[cssClassName], [me, model.copy()]);
							me.deselectPicker();
						} else {
							me.callDelegates("onFilterMenuButtonApplyActionClick", [me, model.copy()]);
						}
					},

					addFilter: function() {
						me.callDelegates("onFilterMenuButtonNewActionClick", me);
					}
				}
			});
		}

		if (state) {
			me.updatePickerPosition();
			me.selectAppliedFilter();
			me.picker.show();
		} else {
			me.picker.hide();
		}
	}

	/*
	 * This component is shown when toggle the main button of the CMFilterMenuButton
	 * Probably could be better to use a floating panel instead a window
	 */
	Ext.define("CMDBuild.view.management.common.filter.CMFilterMenuButtonPicker", {

		extend: "Ext.window.Window",

		baseCls: "", // To remove the blue rounded border of the ExtJS window style

		initComponent: function() {
			this.closable = false;
			this.draggable = false;
			this.resizable = false;
			this.expandOnShow = true;
			this.frame = false;
			this.border = false;
			this.rowTemplate = '<p class="filterMenuButtonGrid-name">{0}</p><p class="filterMenuButtonGrid-description">{1}</p>';

			this.cls = "filterMenuButtonGrid";

			var me = this;
			this.grid = new Ext.grid.Panel({
				maxHeight: 150,
				width: 300,
				autoScroll: true,
				hideHeaders: true,
				store: _CMCache.getAvailableFilterStore(),
				tbar: [{
					text: CMDBuild.Translation.management.findfilter.add,
					iconCls: "add",
					handler: function() {
						me.fireEvent("addFilter");
					}
				}],
				columns: [{
					renderer: function(value, metadata, record, rowIndex, colIndex, store, view) {
						var description = Ext.String.ellipsis(record.getDescription(), 50);
						var name = Ext.String.ellipsis(record.getName(), 45);
						if (record.dirty) {
							name += "*";
						}

						return Ext.String.format(me.rowTemplate, name, description);
					},
					flex: 1,
					menuDisabled: true,
					hideable: false
				}, {
					width: 100,
					fixed: true, 
					sortable: false, 
					renderer: function(value, metadata, record, rowIndex, colIndex, store, view) {
						var template = '<img style="cursor:pointer" title="{0}" class="{1}" src="{2}"/>';
						var saveIcon = record.dirty ? ICONS_PATH.save : ICONS_PATH.save_disabled;
						return Ext.String.format(template, TOOLTIP.save, ACTION_CSS_CLASS.saveFilter, saveIcon) +
							Ext.String.format(template, TOOLTIP.modify, ACTION_CSS_CLASS.modifyFilter, ICONS_PATH.modify) +
							Ext.String.format(template, TOOLTIP.clone, ACTION_CSS_CLASS.cloneFilter, ICONS_PATH.clone) +
							Ext.String.format(template, TOOLTIP.remove, ACTION_CSS_CLASS.removeFilter, ICONS_PATH.remove);
					},
					align: 'center',
					menuDisabled: true,
					hideable: false
				}]
			});

			this.relayEvents(this.grid, ['beforeitemclick', 'select']);
			this.items = [this.grid];

			this.callParent(arguments);
		},

		deselect: function() {
			this.grid.getSelectionModel().deselectAll();
		},

		selectAppliedFilter: function() {
			this.deselect();
			var appliedFilter = this.grid.getStore().findRecord("applied", true);
			if (appliedFilter != null) {
				this.grid.getSelectionModel().select(appliedFilter);
			}
		}
	});

})();