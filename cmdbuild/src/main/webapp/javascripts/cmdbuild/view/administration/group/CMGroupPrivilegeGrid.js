(function() {

	var tr = CMDBuild.Translation.administration.modsecurity.privilege;
	var ACTION_SET_PRIVILEGE_FILTER = "action-filter-set";
	var ACTION_REMOVE_PRIVILEGE_FILTER = "action-filter-remove";
	var parameter = _CMProxy.parameter;

	Ext.define("CMDBuild.view.administration.group.CMGroupPrivilegeGrid", {
		extend: "Ext.grid.Panel",
		alias: "privilegegrid",
		enableDragDrop: false,

		mixins: {
			cmFilterWindowDelegate: "CMDBuild.view.management.common.filter.CMFilterWindowDelegate",
			cmFilterChooserWindowDelegate: "CMDBuild.delegate.common.field.CMFilterChooserWindowDelegate"
		},

		// configuration
		/**
		 * true to add a column to set
		 * the relative privilege to NONE
		 */
		withPermissionNone: true,

		/**
		 * true to add a column to set
		 * the relative privilege to READ
		 */
		withPermissionRead: true,

		/**
		 * true to add a column to set
		 * the relative privilege to WRITE
		 */
		withPermissionWrite: true,

		/**
		 * add a button to set the
		 * privileges filter
		 */
		withFilterEditor: false,

		/**
		 * the URL to call to notify
		 * the server of the click
		 */
		actionURL: undefined,

		// configuration

		initComponent: function() {
			this.recordToChange = null;

			this.columns = [{
				hideable: false,
				header: CMDBuild.Translation.description_,
				dataIndex: 'privilegedObjectDescription',
				flex: 1,
				sortable: true
			}];

			buildCheckColumn(this, 'none_privilege', this.withPermissionNone);
			buildCheckColumn(this, 'read_privilege', this.withPermissionRead);
			buildCheckColumn(this, 'write_privilege', this.withPermissionWrite);

			var setPrivilegeTranslation = CMDBuild.Translation.row_and_column_privileges;
			var removePrivilegeTranslation = CMDBuild.Translation.clear_row_and_colun_privilege;

			if (this.withFilterEditor) {
				this.columns.push({
					header: '&nbsp',
					fixed: true, 
					sortable: false, 
					align: 'center',
					tdCls: 'grid-button',
					menuDisabled: true,
					hideable: false,
					renderer: function() {
						return '<img class="' 
							+ ACTION_SET_PRIVILEGE_FILTER
							+ '" src="images/icons/privilege_filter.png"'
							+ '" title="' + setPrivilegeTranslation + '"/>' +
						'<img class="'
							+ ACTION_REMOVE_PRIVILEGE_FILTER
							+ '" src="images/icons/privilege_filter_remove.png"'
							+ '" title="' + removePrivilegeTranslation + '"/>';
					}
				});
			}

			this.viewConfig = {
				forceFit: true
			};

			this.plugins = [ // 
				Ext.create('Ext.grid.plugin.CellEditing', { //
					clicksToEdit: 1 //
				}) //
			];

			this.frame = false;
			this.border = false;

			this.callParent(arguments);

			this.callBacks = {};
			this.callBacks[ACTION_SET_PRIVILEGE_FILTER] = onSetPrivilegeFilterClick;
			this.callBacks[ACTION_REMOVE_PRIVILEGE_FILTER] = onRemovePrivilegeFilterClick;

			this.on('beforeitemclick', cellclickHandler, this);
		},

		loadStoreForGroup: function(group) {
			this.currentGroup = group.get("id") || -1;
			this.loadStore();
		},

		loadStore: function() {
			if (this.currentGroup && this.currentGroup > 0) {
				this.getStore().load({
					params: {
						groupId: this.currentGroup
					}
				});
			}
		},

		clickPrivileges: function(cell, recordIndex, checked) {
			var me = this;
			this.recordToChange = this.store.getAt(recordIndex);
			if (me.actionURL) {
				CMDBuild.Ajax.request({
					url: me.actionURL,
					params: {
						privilege_mode: cell.dataIndex,
						groupId: me.recordToChange.getGroupId(),
						privilegedObjectId: me.recordToChange.getPrivilegedObjectId()
					},
					callback: function() {
						me.loadStore();
					}
				});
			}
		},

		// as cmFilterWindowDelegate

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowApplyButtonClick: Ext.emptyFn,

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowSaveButtonClick: function(filterWindow) {
			// BUSINNESS RULE: The user could not save the privileges if the filter
			// has some runtime parameter
			var filter = filterWindow.getFilter();
			var runtimeParameters = filter.getRuntimeParameters();
			var calculatedParameters = filter.getCalculatedParameters();

			if (runtimeParameters && runtimeParameters.length > 0) {
				CMDBuild.Msg.error(//
					CMDBuild.Translation.error, //
					CMDBuild.Translation.itIsNotAllowedFilterWithRuntimeParams, //
					false //
				);

				return;
			} else if (calculatedParameters && calculatedParameters.length > 0) {
				CMDBuild.Msg.error(//
					CMDBuild.Translation.error, //
					CMDBuild.Translation.itIsNotAllowedFilterWithCalculatedParams, //
					false //
				);

				return;
			}

			var params = {};
			params[parameter.PRIVILEGED_OBJ_ID] = filterWindow.group.getPrivilegedObjectId();
			params[parameter.GROUP_ID] = filterWindow.group.getGroupId();
			var attributesPrivileges = filterWindow.getAttributePrivileges();
			params[parameter.ATTRIBUTES] = Ext.encode(attributesPrivileges);
			params[parameter.FILTER] = Ext.encode(filter.getConfiguration());

			_CMProxy.group.setRowAndColumnPrivileges({
				params: params,
				success: function() {
					filterWindow.group.setPrivilegeFilter(params[parameter.FILTER]);
					filterWindow.group.setAttributePrivileges(attributesPrivileges);
					filterWindow.destroy();
				}
			});

		},

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowSaveAndApplyButtonClick: Ext.emptyFn,

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowAbortButtonClick: function(filterWindow) {
			filterWindow.destroy();
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
			filterWindow.setFilter(filter);
		}
	});

	// scope this
	function onSetPrivilegeFilterClick(model) {

		var className = model.get("privilegedObjectName");
		var entryType = _CMCache.getEntryTypeByName(className);
		var filterConfiguration = model.getPrivilegeFilter() || "{}";

		var filter = new CMDBuild.model.CMFilterModel({
			entryType: className,
			local: true,
			name: "",
			configuration: Ext.decode(filterConfiguration)
		});

		var me = this;
		var parameterNames = CMDBuild.ServiceProxy.parameter;
		var params = {};
		params[parameterNames.ACTIVE] = false; // all the attributes
		params[parameterNames.CLASS_NAME] = entryType.getName();

		CMDBuild.ServiceProxy.attributes.read({
			params: params,
			success: function success(response, options, result) {
				var attributes = result.attributes;
	
				var filterWindow = new CMDBuild.view.administration.group.CMPrivilegeWindow({
					filter: filter,
					attributes: attributes,
					className: className,
					group: model
				});
	
				filterWindow.addDelegate(me);
				filterWindow.show();
			}
		});
	}

	// scope this
	function onRemovePrivilegeFilterClick(model) {
		var me = this;
		Ext.Msg.show({
			title: CMDBuild.Translation.attention,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					var params = {};

					params[parameter.PRIVILEGED_OBJ_ID] = model.getPrivilegedObjectId();
					params[parameter.GROUP_ID] = model.getGroupId();

					_CMProxy.group.setRowAndColumnPrivileges({
						params: params,
						success: function() {
							me.loadStore();
						}
					});
				}
			}
		});

	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className; 

		if (this.callBacks[className]) {
			this.callBacks[className].call(this, model);
		}
	}

	function buildCheckColumn(me, dataIndex, condition) {
		if (condition) {
			var checkColumn = new Ext.ux.CheckColumn({
				header: tr[dataIndex],
				align: "center",
				dataIndex: dataIndex,
				width: 70,
				fixed: true
			});
			me.columns.push(checkColumn);
			me.mon(checkColumn, "checkchange", me.clickPrivileges, me);
		}
	}
})();