(function () {

	Ext.define('CMDBuild.core.fieldManager.builders.Integer', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Ext.grid.column.Column or Object}
		 *
		 * @override
		 *
		 * NOTE: cannot implement Ext.grid.column.Number because don't recognize not anglosaxon number formats
		 */
		buildColumn: function (parameters) {
			return this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN) ? {} : Ext.create('Ext.grid.column.Column', {
				dataIndex: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				disabled: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				editor: parameters.withEditor ? this.buildEditor() : null,
				hidden: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.SHOW_COLUMN),
				renderer: this.rendererColumn,
				scope: this,
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)),
				width: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME).length * 9
			});
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		buildEditor: function () {
			return this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN) ? {} : {
				xtype: 'numberfield',
				allowBlank: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				allowDecimals: false,
				disabled: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				hideTrigger: true, // Hides selecting arrows
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				readOnly: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				allowDecimals: false
			};
		},

		/**
		 * @returns {Ext.form.field.Number}
		 *
		 * @override
		 */
		buildField: function () {
			return Ext.create('Ext.form.field.Number', {
				allowBlank: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				allowDecimals: false,
				disabled: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
					|| this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME)
				),
				hidden: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN),
				hideTrigger: true, // Hides selecting arrows
				labelAlign: 'right',
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_SMALL,
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				readOnly: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE)
			});
		},

		/**
		 * @returns {CMDBuild.core.fieldManager.fieldset.FilterConditionView}
		 *
		 * @override
		 */
		buildFilterCondition: function () {
			return Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView', {
				fields: [
					Ext.create('Ext.form.field.Number', {
						allowDecimals: false,
						hideTrigger: true, // Hides selecting arrows
						width: CMDBuild.core.constants.FieldWidths.STANDARD_SMALL
					}),
					Ext.create('Ext.form.field.Number', {
						allowDecimals: false,
						hideTrigger: true, // Hides selecting arrows
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
			return { name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME), type: 'int', useNull: true };
		}
	});

})();
