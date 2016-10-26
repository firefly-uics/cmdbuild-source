(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.email.step1.FilterFieldset', {
		extend: 'Ext.form.FieldSet',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.Email'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.email.Step1}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		fieldFilterType: undefined,

		/**
		 * @property {Ext.container.Container}
		 */
		wrapper: undefined,

		overflowY: 'auto',
		title: CMDBuild.Translation.filter,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.fieldFilterType = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.FILTER_TYPE,
						fieldLabel: CMDBuild.Translation.type,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						valueField: CMDBuild.core.constants.Proxy.VALUE,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG - 10,
						value: CMDBuild.core.constants.Proxy.NONE, // Default value
						forceSelection: true,
						editable: false,

						store: CMDBuild.proxy.administration.taskManager.task.Email.getStoreFilterType(),
						queryMode: 'local',

						listeners: {
							scope: this,
							change: function (field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onTaskManagerFormTaskEmailStep1FieldTypeComboChange');
							}
						}
					}),
					this.wrapper = Ext.create('Ext.container.Container', {
						layout: 'card',

						items: [
							Ext.create('Ext.container.Container'), // None item
							Ext.create('Ext.container.Container', { // Regex item
								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: [
									Ext.create('CMDBuild.view.administration.taskManager.task.common.field.stringList.StringListView', {
										name: CMDBuild.core.constants.Proxy.FILTER_FROM_ADDRESS,
										fieldLabel: CMDBuild.Translation.sender,
										maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG - 10,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10
									}),
									Ext.create('CMDBuild.view.administration.taskManager.task.common.field.stringList.StringListView', {
										name: CMDBuild.core.constants.Proxy.FILTER_SUBJECT,
										fieldLabel: CMDBuild.Translation.subject,
										maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG - 10,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10
									})
								]
							}),
							Ext.create('Ext.container.Container', { // Function item
								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: [
									Ext.create('Ext.form.field.ComboBox', {
										name: CMDBuild.core.constants.Proxy.FILTER_FUNCTION,
										fieldLabel: CMDBuild.Translation.functionLabel,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10,
										maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG - 10,
										valueField: CMDBuild.core.constants.Proxy.NAME,
										displayField: CMDBuild.core.constants.Proxy.NAME,
										editable: false,

										store: CMDBuild.proxy.administration.taskManager.task.Email.getStoreFunctions(),
										queryMode: 'local'
									})
								]
							})
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
