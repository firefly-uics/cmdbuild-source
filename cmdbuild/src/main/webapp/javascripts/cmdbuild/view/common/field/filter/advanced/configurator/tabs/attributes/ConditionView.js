(function () {

	Ext.define('CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.ConditionView', {
		extend: 'Ext.form.FieldContainer',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.attributes.Condition}
		 */
		delegate: undefined,

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		defaultValueCondition: CMDBuild.core.constants.Proxy.EQUAL,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		fieldInputParameter: undefined,

		/**
		 * @property {'Ext.form.field.ComboBox}
		 */
		fieldOperator: undefined,

		/**
		 * @cfg {Array}
		 */
		fields: [],

		/**
		 * @property {Ext.container.Container}
		 */
		labelOr: undefined,

		/**
		 * @cfg {String}
		 */
		name: undefined,

		/**
		 * @cfg {Ext.data.ArrayStore}
		 */
		store: undefined,

		border: false,
		frame: false,

		layout: {
			type: 'hbox',
			pack: 'start',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {

			this.fields = Ext.isArray(this.fields) ? this.fields : [this.fields];

			// Error handling
				if (!Ext.isArray(this.fields) || Ext.isEmpty(this.fields))
					return _error('initComponent(): unmanaged field property', this, this.fields);
			// END: Error handling

			this.delegate = Ext.create('CMDBuild.controller.common.field.filter.advanced.configurator.tabs.attributes.Condition', {
				parentDelegate: this.parentDelegate,
				view: this
			});

			// Field margin fix
			Ext.Array.forEach(this.fields, function (field, i, allFields) {
				if (Ext.isObject(field) && !Ext.Object.isEmpty(field)) {
					field.disablePanelFunctions = true; // Avoid enable/disable errors
					field.setMargin('5 5 0 0');
					field.setDisabled(i != 0); // Enable only first field
				}
			}, this);

			Ext.apply(this, {
				items: this.delegate.cmfg('fieldFieldsetFilterConditionContainerBuildItems', [
					[
						Ext.create('Ext.container.Container', { // Avoid item stretch thats needs for labelOr field
							items: [
								Ext.create('CMDBuild.core.buttons.icon.Remove', {
									tooltip: CMDBuild.Translation.remove,
									margin: '5 5 0 0',
									scope: this,

									handler: function(button, e) {
										this.delegate.cmfg('fieldFilterAdvancedConfiguratorTabAttributesFieldsetConditionRemoveButtonClick', this);
									}
								})
							]
						}),
						Ext.create('Ext.container.Container', { // Avoid item stretch thats needs for labelOr field
							items: [
								this.fieldOperator = Ext.create('Ext.form.field.ComboBox', {
									valueField: CMDBuild.core.constants.Proxy.ID,
									displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
									allowBlank: false,
									forceSelection: true,
									margin: '5 5 0 0',
									width: 130,

									value: this.defaultValueCondition, // Default value

									store: this.store,
									queryMode: 'local',

									listeners: {
										scope: this,
										change: function (field, newValue, oldValue, eOpts) {
											this.delegate.cmfg('onFieldFieldsetFilterConditionContainerOperatorSelect', newValue);
										},
										select: function (field, records, eOpts) {
											this.delegate.cmfg('onFieldFieldsetFilterConditionContainerOperatorSelect', field.getValue());
										}
									}
								})
							]
						})
					],
					this.fields,
 					[
						Ext.create('Ext.container.Container', { // Avoid item stretch thats needs for labelOr field
							items: [
								this.fieldInputParameter = Ext.create('Ext.form.field.Checkbox', {
									boxLabel: CMDBuild.Translation.inputParameter,
									margin: '5 5 0 0',

									listeners: {
										scope: this,
										change: function (field, newValue, oldValue, eOpts) {
											this.delegate.cmfg('onFieldFieldsetFilterConditionContainerInputParameterChange', newValue);
										}
									}
								})
							]
						}),
						this.labelOr = Ext.create('Ext.container.Container', {
							hidden: true,

							layout: {
								type: 'vbox',
								align: 'center',
								pack: 'center'
							},

							items: [
								Ext.create('Ext.container.Container', { html: CMDBuild.Translation.or })
							]
						})
					]
				])
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Object}
		 */
		getValue: function () {
			return this.delegate.cmfg('fieldFieldsetFilterConditionContainerValueGet');
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 */
		setLabelOrVisible: function (state) {
			this.delegate.cmfg('fieldFieldsetFilterConditionContainerLabelOrVisibleSet', state);
		},

		/**
		 * @param {Object} value
		 *
		 * @returns {Void}
		 */
		setValue: function (value) {
			this.delegate.cmfg('fieldFieldsetFilterConditionContainerValueSet', value);
		}
	});

})();
