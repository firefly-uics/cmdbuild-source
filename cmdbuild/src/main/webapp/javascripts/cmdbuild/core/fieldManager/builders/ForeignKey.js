(function () {

	/**
	 * Specific field attributes:
	 * 		- {String} targetClass: target class name
	 * 		- {String} filter: filter to apply
	 */
	Ext.define('CMDBuild.core.fieldManager.builders.ForeignKey', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.model.common.attributes.ForeignKeyStore'
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
//			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;
//
//			return Ext.create('Ext.grid.column.Column', {
//				dataIndex: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
//				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
//				editor: withEditor ? this.buildEditor() : null,
//				flex: 1,
//				sortable: true,
//				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION))
//			});
		},

		/**
		 * @returns {Object}
		 */
		buildEditor: function() {
//			return {
//				xtype: 'textfield',
//				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.MANDATORY),
//				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
//				enforceMaxLength: true,
//				maxLength: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.LENGTH),
//				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
//				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE)
//			};
		},

		/**
		 * @returns {Ext.form.field.Text}
		 *
		 * TODO: waiting for refactor (SearchableCombo)
		 */
		buildField: function() {
			return Ext.create('CMDBuild.view.common.field.SearchableCombo', {
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
				triggerAction: 'all', // TODO: waiting for refactor (SearchableCombo)
				valueField: 'Id',

				store: this.getStore(),
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
		getStore: function() {
			var baseParams = {};
			baseParams[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.TARGET_CLASS);

			if (!this.cmfg('attributeModelIsEmpty', CMDBuild.core.proxy.CMProxyConstants.FILTER)) {
				baseParams[CMDBuild.core.proxy.CMProxyConstants.FILTER] = Ext.encode({ CQL: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.FILTER) });
			} else {
				baseParams['NoFilter'] = true;
			}

			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				baseParams: baseParams, // Retro-compatibility
				model: 'CMDBuild.model.common.attributes.ForeignKeyStore',
				pageSize: parseInt(CMDBuild.Config.cmdbuild.referencecombolimit),
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.card.getListShort,
					reader: {
						type: 'json',
						root: 'rows',
						totalProperty: 'results'
					},
					extraParams: baseParams
				},
				sorters: [
					{ property: 'Description', direction: 'ASC' }
				]
			});
		}
	});

})();