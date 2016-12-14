(function () {

	/**
	 * Specific field attributes:
	 * 		- {String} lookupType
	 */
	Ext.define('CMDBuild.core.fieldManager.builders.Lookup', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.comboBox.Reference'
		],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		// TODO: implementation of CMDBuild.field.MultiLevelLookupPanel of old reference field and FieldManager

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Ext.grid.column.Column or Object}
		 *
		 * @override
		 */
		buildColumn: function (parameters) {
//			return this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN) ? {} : Ext.create('Ext.grid.column.Column', {
//				dataIndex: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
//				disabled: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
//				editor: parameters.withEditor ? this.buildEditor() : null,
//				hidden: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.SHOW_COLUMN),
//				renderer: this.rendererColumn,
//				scope: this,
//				sortable: true,
//				text: this.applyMandatoryLabelFlag(this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION))
//			});
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		buildEditor: function () {
//			return Ext.create('CMDBuild.view.common.field.comboBox.lookup.Lookup', {
//				allowBlank: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
//				attributeModel: this.cmfg('fieldManagerAttributeModelGet'),
//				disabled: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
//				displayField: 'Description',
//				fieldLabel: this.applyMandatoryLabelFlag(
//					this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
//					|| this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME)
//				),
//				hidden: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN),
//				labelAlign: 'right',
//				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
//				maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM,
//				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
//				readOnly: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
//				valueField: 'Id'
//			});
		},

		/**
		 * @returns {CMDBuild.view.common.field.comboBox.lookup.Lookup}
		 *
		 * @override
		 */
		buildField: function () {
//			return Ext.create('CMDBuild.view.common.field.comboBox.lookup.Lookup', {
//				allowBlank: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
//				attributeModel: this.cmfg('fieldManagerAttributeModelGet'),
//				disabled: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
//				displayField: 'Description',
//				fieldLabel: this.applyMandatoryLabelFlag(
//					this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
//					|| this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME)
//				),
//				hidden: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN),
//				labelAlign: 'right',
//				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
//				maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM,
//				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
//				readOnly: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
//				valueField: 'Id'
//			});
		},

		/**
		 * @returns {CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView}
		 *
		 * @override
		 */
		buildFilterCondition: function () { // TODO: multilevel lookups (CMDBuild.WidgetBuilders.LookupAttribute.prototype.genericBuildFieldsetForFilter)
			return Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView', {
				parentDelegate: this.parentDelegate,
				fields: [
					Ext.create('CMDBuild.view.common.field.comboBox.lookup.Lookup', {
						attributeModel: this.cmfg('fieldManagerAttributeModelGet'),
						displayField: 'Description',
						valueField: 'Id',
						width: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM
					})
				],
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				store: Ext.create('Ext.data.ArrayStore', { // TODO: check
					fields: [CMDBuild.core.constants.Proxy.ID, CMDBuild.core.constants.Proxy.DESCRIPTION],
					data: [
						['isnotnull', CMDBuild.Translation.isNotNull],
						['isnull', CMDBuild.Translation.isNull],
						['notequal', CMDBuild.Translation.different],
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
