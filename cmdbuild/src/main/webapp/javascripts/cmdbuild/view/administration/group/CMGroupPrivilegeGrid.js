(function() {

	var tr = CMDBuild.Translation.administration.modsecurity.privilege;

	Ext.define("CMDBuild.view.administration.group.CMGroupPrivilegeGrid", {
		extend: "Ext.grid.Panel",
		alias: "privilegegrid",
		enableDragDrop: false,

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
		}
	});

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
			checkColumn.on("checkchange", me.clickPrivileges, me);
		}
	}
})();