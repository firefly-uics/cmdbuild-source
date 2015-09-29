(function() {

	Ext.define('CMDBuild.controller.management.dataView.CMModCardController', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.dataView.Sql'
		],

		/**
		 * @cfg {CMDBuild.controller.management.dataView.DataView}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onButtonPrintClick',
			'onDataViewSqlGridSelect',
			'onDataViewSqlViewSelected = onDataViewViewSelected'
		],

		/**
		 * @property {CMDBuild.view.management.dataView.sql.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.dataView.sql.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.management.dataView.sql.SqlView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.dataView.DataView} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.dataView.sql.SqlView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;

			if (!Ext.isEmpty(_CMUIState))
				_CMUIState.addDelegate(this);
		},

		/**
		 * @returns {Array} visibleColumns
		 */
		getVisibleColumns: function() {
			var visibleColumns = [];

			Ext.Array.forEach(this.grid.columns, function(column, i, allColumns) {
				if (!column.isHidden() && !Ext.isEmpty(column.dataIndex))
					visibleColumns.push(column.dataIndex);
			}, this);

			return visibleColumns;
		},

		/**
		 * @param {String} format
		 */
		onButtonPrintClick: function(format) {
			if (!Ext.isEmpty(format)) {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES] = Ext.encode(this.getVisibleColumns());
				params[CMDBuild.core.proxy.CMProxyConstants.FUNCTION] = this.cmfg('dataViewSelectedGet', CMDBuild.core.proxy.CMProxyConstants.SOURCE_FUNCTION);
				params[CMDBuild.core.proxy.CMProxyConstants.SORT] = Ext.encode(this.grid.getStore().getSorters());
				params[CMDBuild.core.proxy.CMProxyConstants.TYPE] = format;

				Ext.create('CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow', {
					format: format,
					mode: 'dataViewSql',
					parameters: params
				});
			}
		},

		onDataViewSqlGridSelect: function() {
			var record = this.grid.getSelectionModel().getSelection()[0];

			this.form.cardPanel.removeAll();

			record.fields.each(function(field) {
				var name = field.name;

				if (!Ext.Array.contains([CMDBuild.core.proxy.CMProxyConstants.ID], name)) { // Filters id attribute
					var value = record.get(name);

					if (
						!Ext.isEmpty(value)
						&& Ext.isObject(value)
						&& Ext.isFunction(value.toString)
					) {
						value = value.toString();
					}

					this.form.cardPanel.add(
						Ext.create('CMDBuild.view.common.field.CMDisplayField', {
							disabled: false,
							fieldLabel: field.name,
							labelAlign: 'right',
							labelWidth: CMDBuild.LABEL_WIDTH,
							name: field.name,
							submitValue: false,
							style: {
								overflow: 'hidden'
							},
							value: value,
							maxWidth: CMDBuild.BIG_FIELD_WIDTH
						})
					);
				}
			}, this);
		},

		/**
		 * TODO: waiting for refactor (avoid cache)
		 */
		onDataViewSqlViewSelected: function() {
			if (!this.cmfg('dataViewSelectedIsEmpty')) {
				this.cmfg('dataViewSelectedSet', {
					propertyName: CMDBuild.core.proxy.CMProxyConstants.INPUT,
					value: _CMCache.getDataSourceInput(this.cmfg('dataViewSelectedGet', CMDBuild.core.proxy.CMProxyConstants.SOURCE_FUNCTION))
				});

				this.cmfg('dataViewSelectedSet', {
					propertyName: CMDBuild.core.proxy.CMProxyConstants.OUTPUT,
					value: _CMCache.getDataSourceOutput(this.cmfg('dataViewSelectedGet', CMDBuild.core.proxy.CMProxyConstants.SOURCE_FUNCTION))
				});

				var columns = [];
				var store = CMDBuild.core.proxy.dataView.Sql.getStore({
					fields: this.cmfg('dataViewSelectedGet', CMDBuild.core.proxy.CMProxyConstants.OUTPUT)
				});

				Ext.Array.forEach(this.cmfg('dataViewSelectedGet', CMDBuild.core.proxy.CMProxyConstants.OUTPUT), function(columnObject, i, allColumnObjects) {
					columns.push({
						text: columnObject[CMDBuild.core.proxy.CMProxyConstants.NAME],
						dataIndex: columnObject[CMDBuild.core.proxy.CMProxyConstants.NAME],
						flex: 1
					});
				}, this);

				this.grid.reconfigure(store, columns);

				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.FUNCTION] = this.cmfg('dataViewSelectedGet', CMDBuild.core.proxy.CMProxyConstants.SOURCE_FUNCTION);

				this.grid.getStore().load({
					params: params,
					scope: this,
					callback: function(records, operation, success) {
						// Store load errors manage
						if (!success) {
							CMDBuild.core.Message.error(null, {
								text: CMDBuild.Translation.errors.unknown_error,
								detail: operation.error
							});
						}

						if (!this.grid.getSelectionModel().hasSelection())
							this.grid.getSelectionModel().select(0, true);
					}
				});
			}
		},

		onFullScreenChangeToFormOnly: function() {
			Ext.suspendLayouts();

			this.grid.hide();
			this.grid.region = '';

			this.form.show();
			this.form.region = 'center';

			Ext.resumeLayouts(true);
		},

		onFullScreenChangeToGridOnly: function() {
			Ext.suspendLayouts();

			this.form.hide();
			this.form.region = '';

			this.grid.show();
			this.grid.region = 'center';

			Ext.resumeLayouts(true);
		},

		onFullScreenChangeToOff: function() {
			Ext.suspendLayouts();
			this.form.show();
			this.form.region = 'south';

			this.grid.show();
			this.grid.region = 'center';

			Ext.resumeLayouts(true);
		}
	});

})();