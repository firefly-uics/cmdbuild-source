(function () {

	Ext.define('CMDBuild.core.fieldManager.builders.text.Plain', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.core.fieldManager.builders.text.Text}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Ext.grid.column.Column or Object}
		 *
		 * @override
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
				text: this.applyMandatoryLabelFlag(this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION))
			});
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		buildEditor: function () {
			return this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN) ? {} : {
				xtype: 'textfield',
				allowBlank: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				disabled: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				readOnly: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE)
			};
		},

		/**
		 * @returns {Ext.form.field.TextArea}
		 *
		 * @override
		 */
		buildField: function () {
			return Ext.create('Ext.form.field.TextArea', {
				allowBlank: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				disabled: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
					|| this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME)
				),
				hidden: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN),
				labelAlign: 'right',
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_BIG,
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				readOnly: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				resizable: true
			});
		},

		/**
		 * @returns {CMDBuild.view.common.field.display.Text}
		 *
		 * @override
		 */
		buildFieldReadOnly: function () {
			return Ext.create('CMDBuild.view.common.field.display.Text', {
				allowBlank: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
					|| this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME)
				),
				hidden: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN),
				labelAlign: 'right',
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_BIG,
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME)
			});
		},

		/**
		 * @returns {CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView}
		 *
		 * @override
		 */
		buildFilterCondition: function () {
			return Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView', {
				defaultValueCondition: CMDBuild.core.constants.Proxy.CONTAIN,
				fields: [
					Ext.create('Ext.form.field.TextArea', {
						resizable: true,
						width: CMDBuild.core.constants.FieldWidths.STANDARD_BIG
					})
				],
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				store: Ext.create('Ext.data.ArrayStore', {
					fields: [CMDBuild.core.constants.Proxy.ID, CMDBuild.core.constants.Proxy.DESCRIPTION],
					data: [
						['isnotnull', CMDBuild.Translation.isNotNull],
						['isnull', CMDBuild.Translation.isNull],
						['notbegin', CMDBuild.Translation.doesNotBeginWith],
						['notcontain', CMDBuild.Translation.doesNotContain],
						['notend', CMDBuild.Translation.doesNotEndWith],
						['notequal', CMDBuild.Translation.different],
						[CMDBuild.core.constants.Proxy.BEGIN, CMDBuild.Translation.beginsWith],
						[CMDBuild.core.constants.Proxy.CONTAIN, CMDBuild.Translation.contains],
						[CMDBuild.core.constants.Proxy.END, CMDBuild.Translation.endsWith],
						[CMDBuild.core.constants.Proxy.EQUAL, CMDBuild.Translation.equals]
					],
					sorters: [
						{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
					]
				})
			});
		}
	});

})();
