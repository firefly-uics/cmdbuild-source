(function() {

	Ext.define('CMDBuild.view.administration.group.privileges.filterWindow.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		border: false,
		frame: false,
		title: CMDBuild.Translation.columnsPrivileges,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.proxy.Constants.NAME,
						text: CMDBuild.Translation.name,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.proxy.Constants.DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						flex: 1
					},
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: CMDBuild.core.proxy.Constants.NONE,
						text: CMDBuild.Translation.none,
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						listeners: {
							scope: this,
							checkchange: function(column, rowIndex, checked, eOpts) {
								if (!checked) {
									return;
								}

								var model = this.store.getAt(rowIndex);
								if (model) {
									model.setPrivilege(column.dataIndex);
								}
							}
						}
					}),
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: CMDBuild.core.proxy.Constants.READ,
						text: CMDBuild.Translation.read,
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						listeners: {
							scope: this,
							checkchange: function(column, rowIndex, checked, eOpts) {
								if (!checked) {
									return;
								}

								var model = this.store.getAt(rowIndex);
								if (model) {
									model.setPrivilege(column.dataIndex);
								}
							}
						}
					}),
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: CMDBuild.core.proxy.Constants.WRITE,
						text: CMDBuild.Translation.write,
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						listeners: {
							scope: this,
							checkchange: function(column, rowIndex, checked, eOpts) {
								if (!checked) {
									return;
								}

								var model = this.store.getAt(rowIndex);
								if (model) {
									model.setPrivilege(column.dataIndex);
								}
							}
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();