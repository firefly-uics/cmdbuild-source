(function () {

	/**
	 * Specific field attributes:
	 * 		- {String} filter: filter to apply
	 * 		- {String} targetClass: target class name
	 */
	Ext.define('CMDBuild.core.fieldManager.builders.ForeignKey', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.ForeignKey'
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
			return this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN) ? {} : Ext.create('CMDBuild.view.common.field.comboBox.Searchable', {
				allowBlank: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				attributeModel: this.cmfg('fieldManagerAttributeModelGet'),
				disabled: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				displayField: 'Description',
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				plugins: Ext.create('CMDBuild.core.plugin.SetValueOnLoad'),
				readOnly: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				valueField: 'Id',

				store: this.buildFieldStore(),
				queryMode: 'local',

				templateResolver: this.cmfg('fieldManagerTemplateResolverBuild', [CMDBuild.core.constants.Proxy.FILTER]),
				resolveTemplates: this.cmfg('fieldManagerTemplateResolverResolveFunctionGet'),

				listeners: {
					scope: this,
					added: function (field, container, pos, eOpts) {
						field.resolveTemplates();
					}
				}
			});
		},

		/**
		 * @returns {Ext.form.field.Text}
		 *
		 * @override
		 */
		buildField: function () {
			return Ext.create('CMDBuild.view.common.field.comboBox.Searchable', {
				allowBlank: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				attributeModel: this.cmfg('fieldManagerAttributeModelGet'),
				disabled: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				displayField: 'Description',
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
					|| this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME)
				),
				hidden: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN),
				labelAlign: 'right',
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_BIG,
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				plugins: Ext.create('CMDBuild.core.plugin.SetValueOnLoad'),
				readOnly: !this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				valueField: 'Id',

				store: this.buildFieldStore(),
				queryMode: 'local',

				templateResolver: this.cmfg('fieldManagerTemplateResolverBuild', [CMDBuild.core.constants.Proxy.FILTER]),
				resolveTemplates: this.cmfg('fieldManagerTemplateResolverResolveFunctionGet'),

				listeners: {
					scope: this,
					added: function (field, container, pos, eOpts) {
						field.resolveTemplates();
					}
				}
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 *
		 * @private
		 */
		buildFieldStore: function () {
			var extraParams = {};
			extraParams[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.TARGET_CLASS);

			if (!this.cmfg('fieldManagerAttributeModelIsEmpty', CMDBuild.core.constants.Proxy.FILTER))
				extraParams[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode({ CQL: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.FILTER) });

			return CMDBuild.proxy.common.field.ForeignKey.getStore({ extraParams: extraParams });
		},

		/**
		 * @returns {CMDBuild.core.fieldManager.fieldset.FilterConditionView}
		 *
		 * @override
		 */
		buildFilterCondition: function () {
			return Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView', {
				fields: [
					Ext.create('CMDBuild.view.common.field.comboBox.Searchable', {
						attributeModel: this.cmfg('fieldManagerAttributeModelGet'),
						displayField: 'Description',
						plugins: Ext.create('CMDBuild.core.plugin.SetValueOnLoad'),
						valueField: 'Id',
						width: CMDBuild.core.constants.FieldWidths.STANDARD_BIG,

						store: this.buildFieldStore(),
						queryMode: 'local',

						templateResolver: this.cmfg('fieldManagerTemplateResolverBuild', [CMDBuild.core.constants.Proxy.FILTER]),
						resolveTemplates: this.cmfg('fieldManagerTemplateResolverResolveFunctionGet'),

						listeners: {
							scope: this,
							added: function (field, container, pos, eOpts) {
								field.resolveTemplates();
							}
						}
					})
				],
				name: this.cmfg('fieldManagerAttributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				store: Ext.create('Ext.data.ArrayStore', {
					fields: [CMDBuild.core.constants.Proxy.ID, CMDBuild.core.constants.Proxy.DESCRIPTION],
					data: [
						['isnotnull', CMDBuild.Translation.isNotNull],
						['isnull', CMDBuild.Translation.isNull],
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
