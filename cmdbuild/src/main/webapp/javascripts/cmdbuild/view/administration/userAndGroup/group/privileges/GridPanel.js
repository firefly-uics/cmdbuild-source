(function() {

	Ext.define('CMDBuild.view.administration.userAndGroup.group.privileges.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.privileges.Grid}
		 */
		delegate: undefined,

		/**
		 * @cfg {Boolean}
		 */
		enableCRUDRead: false,

		/**
		 * @cfg {Boolean}
		 */
		enableCRUDWrite: false,

		/**
		 * @cfg {Boolean}
		 */
		enablePrivilegesAndUi: false,

		border: false,
		disableSelection: true,
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					Ext.create('Ext.grid.column.Column', {
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						flex: 1,
					}),
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: 'none_privilege',
						text: CMDBuild.Translation.none,
						width: 60,
						align: 'center',
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						listeners: {
							scope: this,
							beforecheckchange: function(column, rowIndex, checked, eOpts) { // CheckColumn cannot be unchecked
								return checked;
							},
							checkchange: function(column, rowIndex, checked, eOpts) {
								this.delegate.cmfg('onUserAndGroupGroupPrivilegesGridSetPrivilege', {
									rowIndex: rowIndex,
									privilege: 'none_privilege'
								});
							}
						}
					}),
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: 'read_privilege',
						text: CMDBuild.Translation.read,
						width: 60,
						align: 'center',
						hidden: !this.enableCRUDRead,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						listeners: {
							scope: this,
							beforecheckchange: function(column, rowIndex, checked, eOpts) { // CheckColumn cannot be unchecked
								return checked;
							},
							checkchange: function(column, rowIndex, checked, eOpts) {
								this.delegate.cmfg('onUserAndGroupGroupPrivilegesGridSetPrivilege', {
									rowIndex: rowIndex,
									privilege: 'read_privilege'
								});
							}
						}
					}),
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: 'write_privilege',
						text: CMDBuild.Translation.write,
						width: 60,
						align: 'center',
						hidden: !this.enableCRUDWrite,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						listeners: {
							scope: this,
							beforecheckchange: function(column, rowIndex, checked, eOpts) { // CheckColumn cannot be unchecked
								return checked;
							},
							checkchange: function(column, rowIndex, checked, eOpts) {
								this.delegate.cmfg('onUserAndGroupGroupPrivilegesGridSetPrivilege', {
									rowIndex: rowIndex,
									privilege: 'write_privilege'
								});
							}
						}
					}),
					Ext.create('Ext.grid.column.Action', {
						align: 'center',
						hidden: !this.enablePrivilegesAndUi,
						width: 75,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.filter.Set', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.rowAndColumnPrivileges,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onUserAndGroupGroupPrivilegesGridSetFilterClick', record);
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.filter.Clear', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.clearRowAndColumnPrivilege,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onUserAndGroupGroupPrivilegesGridRemoveFilterClick', record);
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.UserInterface', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.uiConfigurationForGroups,
								scope: this,

								isDisabled: function(grid, rowIndex, colIndex, item, record) {
									return !_CMCache.isClassById(record.get(CMDBuild.core.constants.Proxy.ID));
								},

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onUserAndGroupGroupPrivilegesGridUIConfigurationButtonClick', record);
								}
							})
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onUserAndGroupGroupPrivilegesGridTabShow');
			}
		}
	});

})();