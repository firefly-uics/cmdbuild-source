(function() {

	Ext.define('CMDBuild.view.administration.widget.form.CreateModifyCardPanel', {
		extend: 'CMDBuild.view.administration.widget.form.AbstractWidgetDefinitionPanel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.widgets.CreateModifyCard'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.form.CreateModifyCard}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.TextArea}
		 */
		filter: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		targetClass: undefined,

		/**
		 * @returns {Array}
		 *
		 * @override
		 */
		widgetDefinitionFormBasePropertiesGet: function() {
			return Ext.Array.push(this.callParent(arguments), [
				Ext.create('Ext.form.field.Checkbox', {
					name: CMDBuild.core.constants.Proxy.READ_ONLY,
					fieldLabel: CMDBuild.Translation.readOnly,
					labelWidth: CMDBuild.LABEL_WIDTH
				}),
				this.targetClass = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.core.constants.Proxy.TARGET_CLASS,
					fieldLabel: CMDBuild.Translation.targetClass,
					labelWidth: CMDBuild.LABEL_WIDTH,
					maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
					valueField: CMDBuild.core.constants.Proxy.NAME,
					displayField: CMDBuild.core.constants.Proxy.TEXT, // TODO: waiting for refactor (rename description)
					editable: false,
					forceSelection: true,

					store: CMDBuild.core.proxy.widgets.CreateModifyCard.getStoreTargetClass(),
					queryMode: 'local'
				})
			]);
		},

		/**
		 * @returns {Array}
		 *
		 * @override
		 */
		widgetDefinitionFormCustomPropertiesGet: function() {
			return [
				Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.additionalProperties,
					flex: 1,

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

					items: [
						this.filter = Ext.create('Ext.form.field.TextArea', {
							name: CMDBuild.core.constants.Proxy.FILTER,
							fieldLabel: CMDBuild.Translation.cardCqlSelector,
							labelWidth: CMDBuild.LABEL_WIDTH,
							maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH
						})
					]
				})
			];
		}
	});

})();