(function () {

	Ext.define('CMDBuild.core.fieldManager.builders.Date', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: [
			'CMDBuild.core.configurations.DataFormat',
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Number}
		 */
		headerWidth: 60,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Ext.grid.column.Date or Object}
		 *
		 * @override
		 */
		buildColumn: function (parameters) {
			return this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN) ? {} : Ext.create('Ext.grid.column.Date', {
				dataIndex: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				disabled: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				editor: parameters.withEditor ? this.buildEditor() : null,
				format: CMDBuild.core.configurations.DataFormat.getDate(),
				hidden: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.SHOW_COLUMN),
				renderer: this.rendererColumn,
				scope: this,
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)),
				width: this.headerWidth
			});
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		buildEditor: function () {
			return this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN) ? {} : {
				xtype: 'datefield',
				allowBlank: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				disabled: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				format: CMDBuild.core.configurations.DataFormat.getDate(),
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				readOnly: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE)
			};
		},

		/**
		 * @returns {Ext.form.field.Date}
		 *
		 * @override
		 */
		buildField: function () {
			return Ext.create('Ext.form.field.Date', {
				allowBlank: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				disabled: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
					|| this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME)
				),
				format: CMDBuild.core.configurations.DataFormat.getDate(),
				hidden: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN),
				labelAlign: 'right',
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_SMALL,
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				readOnly: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE)
			});
		},

		/**
		 * @returns {CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView}
		 *
		 * @override
		 */
		buildFilterCondition: function () {
			return Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView', {
				parentDelegate: this.parentDelegate,
				fields: [
					Ext.create('Ext.form.field.Date', {
						format: CMDBuild.core.configurations.DataFormat.getDate(),
						width: CMDBuild.core.constants.FieldWidths.STANDARD_SMALL
					}),
					Ext.create('Ext.form.field.Date', {
						format: CMDBuild.core.configurations.DataFormat.getDate(),
						width: CMDBuild.core.constants.FieldWidths.STANDARD_SMALL
					})
				],
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				store: Ext.create('Ext.data.ArrayStore', {
					fields: [CMDBuild.core.constants.Proxy.ID, CMDBuild.core.constants.Proxy.DESCRIPTION],
					data: [
						['isnotnull', CMDBuild.Translation.isNotNull],
						['isnull', CMDBuild.Translation.isNull],
						['notequal', CMDBuild.Translation.different],
						[CMDBuild.core.constants.Proxy.BETWEEN, CMDBuild.Translation.between],
						[CMDBuild.core.constants.Proxy.EQUAL, CMDBuild.Translation.equals],
						[CMDBuild.core.constants.Proxy.GREATER, CMDBuild.Translation.greaterThan],
						[CMDBuild.core.constants.Proxy.LESS, CMDBuild.Translation.lessThan]
					],
					sorters: [
						{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
					]
				})
			});
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		buildStoreField: function () {
			return {
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				type: 'date',
				dateFormat: CMDBuild.core.configurations.DataFormat.getDate(),
				convert: function (value, record) { // Converter to standardize input date format
					var dateObject = new Date(value);

					if (!Ext.isEmpty(value) && dateObject != 'Invalid Date')
						return Ext.util.Format.date(dateObject, CMDBuild.core.configurations.DataFormat.getDate());

					return value;
				}
			};
		},

		/**
		 * Override to implement date formatter method
		 *
		 * @param {Object} value
		 * @param {Object} metadata
		 * @param {Ext.data.Model} record
		 * @param {Number} rowIndex
		 * @param {Number} colIndex
		 * @param {Ext.data.Store} store
		 * @param {Ext.view.View} view
		 *
		 * @returns {String}
		 *
		 * @override
		 */
		rendererColumn: function (value, metadata, record, rowIndex, colIndex, store, view) {
			this.callParent(arguments);

			if (Ext.isDate(value))
				return Ext.util.Format.date(value, CMDBuild.core.configurations.DataFormat.getDate());

			return value;
		}
	});

})();
