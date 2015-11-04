(function () {

	Ext.define('CMDBuild.core.fieldManager.builders.text.HtmlEditor', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Boolean} withEditor
		 *
		 * @returns {Ext.grid.column.Column or Object}
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			return this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.HIDDEN) ? {} : Ext.create('Ext.grid.column.Column', {
				dataIndex: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				editor: withEditor ? this.buildEditor() : null,
				flex: 1,
				hidden: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.SHOW_COLUMN),
				renderer: 'stripTags',
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION))
			});
		},

		/**
		 * @returns {CMDBuild.view.common.field.CMHtmlEditorField}
		 */
		buildEditor: function() {
			return this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.HIDDEN) ? {} : Ext.create('CMDBuild.view.common.field.CMHtmlEditorField', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE)
			});
		},

		/**
		 * @returns {CMDBuild.view.common.field.CMHtmlEditorField}
		 */
		buildField: function() {
			return Ext.create('CMDBuild.view.common.field.CMHtmlEditorField', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME)
				),
				hidden: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.HIDDEN),
				labelAlign: 'right',
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.HTML_EDITOR_WIDTH,
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE)
			});
		}
	});

})();