(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.CMTasksGrid', {
		extend: 'Ext.grid.Panel',

		/**
		 * @property {Mixed} Task specific controller
		 */
		delegate: undefined,

		border: false,
		frame: false,
		cls: 'cmborderbottom',

		initComponent: function() {
			this.gridColumns = [
				{
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.ID,
					hidden: true
				},
				{
					text: tr.type,
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.TYPE,
					flex: 1,
					scope: this,
					renderer: function(value, metaData, record) {
						return this.typeGridColumnRenderer(value, metaData, record);
					}
				},
				{
					text: CMDBuild.Translation.description_,
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
					flex: 4
				},
				{
					text: CMDBuild.Translation.active,
					width: 60,
					align: 'center',
					dataIndex: CMDBuild.core.proxy.CMProxyConstants.ACTIVE,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					scope: this,
					renderer: function(value, metaData, record) {
						return this.activeGridColumnRenderer(value, metaData, record);
					}
				},
				{
					xtype: 'actioncolumn',
					align: 'center',
					width: 25,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					items: [
						{
							icon: 'images/icons/control_play.png',
							tooltip: tr.startLabel,
							scope: this,
							handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
								this.delegate.cmOn('onStartButtonClick', record);
							},
							isDisabled: function(grid, rowIndex, colIndex, item, record) {
								return record.get(CMDBuild.core.proxy.CMProxyConstants.ACTIVE);
							}
						}
					]
				},
				{
					xtype: 'actioncolumn',
					align: 'center',
					width: 25,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					items: [
						{
							icon: 'images/icons/control_stop.png',
							tooltip: tr.stopLabel,
							scope: this,
							handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
								this.delegate.cmOn('onStopButtonClick', record);
							},
							isDisabled: function(grid, rowIndex, colIndex, item, record) {
								return !record.get(CMDBuild.core.proxy.CMProxyConstants.ACTIVE);
							}
						}
					]
				}
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
		 * Used to render active value to add icon in grid
		 *
		 * @param {Mixed} value
		 * @param {Object} metaData
		 * @param {Object} record
		 */
		activeGridColumnRenderer: function(value, metaData, record) {
			return value ? '<img src="images/icons/accept.png" alt="' + tr.running + '" />' : '<img src="images/icons/cancel.png" alt="' + tr.stopped + '" />';
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