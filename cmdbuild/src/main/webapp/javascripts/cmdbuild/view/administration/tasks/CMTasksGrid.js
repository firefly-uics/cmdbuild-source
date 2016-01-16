(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.CMTasksGrid', {
		extend: 'Ext.grid.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Mixed} Task specific controller
		 */
		delegate: undefined,

		border: false,
		cls: 'cmborderbottom',
		frame: false,

		initComponent: function() {
			this.gridColumns = [
				{
					dataIndex: CMDBuild.core.constants.Proxy.ID,
					hidden: true
				},
				{
					dataIndex: CMDBuild.core.constants.Proxy.TYPE,
					text: tr.type,
					flex: 1,
					scope: this,

					renderer: function(value, metaData, record) {
						return this.typeGridColumnRenderer(value, metaData, record);
					}
				},
				{
					text: CMDBuild.Translation.description_,
					dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
					flex: 4
				},
				Ext.create('Ext.ux.grid.column.Active', {
					dataIndex: CMDBuild.core.constants.Proxy.ACTIVE,
					text: CMDBuild.Translation.active,
					iconAltTextActive: tr.running,
					iconAltTextNotActive: tr.stopped,
					width: 60,
					align: 'center',
					hideable: false,
					menuDisabled: true,
					fixed: true
				}),
				Ext.create('Ext.grid.column.Action', {
					align: 'center',
					width: 25,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,

					items: [
						Ext.create('CMDBuild.core.buttons.iconized.Start', {
							text: null,
							tooltip: tr.startLabel,
							scope: this,

							isDisabled: function(grid, rowIndex, colIndex, item, record) {
								return record.get(CMDBuild.core.constants.Proxy.ACTIVE);
							},

							handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
								this.delegate.cmOn('onStartButtonClick', record);
							}
						})
					]
				}),
				Ext.create('Ext.grid.column.Action', {
					align: 'center',
					width: 25,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,

					items: [
						Ext.create('CMDBuild.core.buttons.iconized.Stop', {
							text: null,
							tooltip: tr.stopLabel,
							scope: this,

							isDisabled: function(grid, rowIndex, colIndex, item, record) {
								return !record.get(CMDBuild.core.constants.Proxy.ACTIVE);
							},

							handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
								this.delegate.cmOn('onStopButtonClick', record);
							}
						})
					]
				})
			];

			Ext.apply(this, {
				columns: this.gridColumns
			});

			this.callParent(arguments);
		},


		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmOn('onItemDoubleClick');
			},

			select: function(model, record, index, eOpts) {
				this.delegate.cmOn('onRowSelected');
			}
		},

		/**
		 * Rendering task type translating with local language data
		 *
		 * @param {Mixed} value
		 * @param {Object} metaData
		 * @param {Object} record
		 */
		typeGridColumnRenderer: function(value, metaData, record) {
			if (typeof value == 'string') {
				if (this.delegate.correctTaskTypeCheck(value)) {
					var splittedType = value.split('_');
					value = '';

					for (var i = 0; i < splittedType.length; i++) {
						if (i == 0) {
							value += eval('CMDBuild.Translation.administration.tasks.tasksTypes.' + splittedType[i]);
						} else {
							value += ' ' + eval('CMDBuild.Translation.administration.tasks.tasksTypes.' + splittedType[0] + 'Types.' + splittedType[i]).toLowerCase();
						}
					}
				}
			}

			return value;
		}
	});

})();