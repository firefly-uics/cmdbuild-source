(function () {

	/**
	 * Specific field attributes:
	 * 		- {String} filter: filter to apply
	 * 		- {String} targetClass: target class name
	 */
	Ext.define('CMDBuild.core.fieldManager.builders.ForeignKey', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.common.field.ForeignKey'
		],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Boolean} withEditor
		 *
		 * @returns {Ext.grid.column.Column}
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			return Ext.create('Ext.grid.column.Column', {
				dataIndex: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				editor: withEditor ? this.buildEditor() : null,
				flex: 1,
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION))
			});
		},

		/**
		 * @returns {Object}
		 */
		buildEditor: function() {
			return Ext.create('CMDBuild.view.common.field.comboBox.Searchable', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.MANDATORY),
				attributeModel: this.cmfg('attributeModelGet'),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				displayField: 'Description',
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				plugins: new CMDBuild.SetValueOnLoadPlugin(),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				valueField: 'Id',

				store: this.buildFieldStore(),
				queryMode: 'local',

				templateResolver: this.cmfg('templateResolverBuild', [CMDBuild.core.proxy.CMProxyConstants.FILTER]),
				resolveTemplates: this.cmfg('templateResolverGetResolveFunction'),

				listeners: {
					scope: this,
					added: function(field, container, pos, eOpts) {
						field.resolveTemplates();
					}
				}
			});
		},

		/**
		 * @returns {Ext.form.field.Text}
		 */
		buildField: function() {
			return Ext.create('CMDBuild.view.common.field.comboBox.Searchable', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.MANDATORY),
				attributeModel: this.cmfg('attributeModelGet'),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				displayField: 'Description',
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME)
				),
				labelAlign: 'right',
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.BIG_FIELD_WIDTH,
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				plugins: new CMDBuild.SetValueOnLoadPlugin(),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				valueField: 'Id',

				store: this.buildFieldStore(),
				queryMode: 'local',

				templateResolver: this.cmfg('templateResolverBuild', [CMDBuild.core.proxy.CMProxyConstants.FILTER]),
				resolveTemplates: this.cmfg('templateResolverGetResolveFunction'),

				listeners: {
					scope: this,
					added: function(field, container, pos, eOpts) {
						field.resolveTemplates();
					}
				}
			});
		},

		/**
		 * @returns {Ext.data.Store}
		 */
		buildFieldStore: function() {
			var extraParams = {};
			extraParams[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.TARGET_CLASS);

			if (!this.cmfg('attributeModelIsEmpty', CMDBuild.core.proxy.CMProxyConstants.FILTER))
				extraParams[CMDBuild.core.proxy.CMProxyConstants.FILTER] = Ext.encode({ CQL: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.FILTER) });

			return CMDBuild.core.proxy.common.field.ForeignKey.getStore({ extraParams: extraParams });
		}
	});

})();