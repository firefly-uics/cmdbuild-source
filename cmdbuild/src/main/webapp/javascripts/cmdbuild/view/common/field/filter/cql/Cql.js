(function() {

	Ext.define('CMDBuild.view.common.field.filter.cql.Cql', {
		extend: 'Ext.form.FieldContainer',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @property {CMDBuild.controller.common.field.filter.cql.Cql}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		buttonLabel: CMDBuild.Translation.editMetadata,

		/**
		 * @cfg {String}
		 */
		fieldName: CMDBuild.core.proxy.Constants.FILTER,

		/**
		 * @property {CMDBuild.core.buttons.Modify}
		 */
		metadataButton: undefined,

		/**
		 * @property {Ext.form.field.TextArea}
		 */
		textAreaField: undefined,

		considerAsFieldToDisable: true,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.controller.common.field.filter.cql.Cql', { view: this });

			Ext.apply(this, {
				items: [
					this.textAreaField = Ext.create('Ext.form.field.TextArea', {
						name: this.fieldName,
						vtype: 'cmdbcommentrelaxed'
					}),
					this.metadataButton = Ext.create('CMDBuild.core.buttons.Modify', {
						text: this.buttonLabel,
						maxWidth: this.buttonLabel.length * 10,
						scope: this,

						handler: function(button, e) {
							this.delegate.cmfg('onFieldFilterCqlMetadataButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @override
		 */
		disable: function() {
			this.delegate.cmfg('onFieldFilterCqlDisable');

			this.callParent(arguments);
		},

		/**
		 * @override
		 */
		enable: function() {
			this.delegate.cmfg('onFieldFilterCqlEnable');

			this.callParent(arguments);
		},

		/**
		 * @returns {Object}
		 */
		getValue: function() {
			return this.delegate.cmfg('onFieldFilterCqlGetValue');
		},

		/**
		 * @param {Boolean} state
		 *
		 * @override
		 */
		setDisabled: function(state) {
			this.delegate.cmfg('onFieldFilterCqlSetDisabled', state);

			this.callParent(arguments);
		},

		/**
		 * @param {Object} value
		 */
		setValue: function(value) {
			this.delegate.cmfg('onFieldFilterCqlSetValue', value);
		}
	});

})();