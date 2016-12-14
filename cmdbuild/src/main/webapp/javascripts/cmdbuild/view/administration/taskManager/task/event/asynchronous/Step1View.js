(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.event.asynchronous.Step1View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.event.Asynchronous'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.event.asynchronous.Step1}
		 */
		delegate: undefined,

		/**
		 * @proeprty {Ext.form.field.ComboBox}
		 */
		fieldEntryType: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,
		overflowY: 'auto',

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
					Ext.create('Ext.form.field.Display', {
						name: CMDBuild.core.constants.Proxy.TYPE,
						fieldLabel: CMDBuild.Translation.type,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						value: CMDBuild.Translation.eventAsynchronous
					}),
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
						allowBlank: false
					}),
					Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.constants.Proxy.ACTIVE,
						fieldLabel: CMDBuild.Translation.startOnSave,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
					}),
					this.fieldEntryType = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.CLASS_NAME,
						fieldLabel: CMDBuild.Translation.classLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						displayField: CMDBuild.core.constants.Proxy.TEXT,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						allowBlank: false,
						forceSelection: true,

						store: CMDBuild.proxy.administration.taskManager.task.event.Asynchronous.getStoreEntryTypes(),
						queryMode: 'local',

						listeners: {
							scope: this,
							select: function (combo, records, options) {
								this.delegate.cmfg('onTaskManagerFormTaskEventAsynchronousEntryTypeSelected');
							}
						}
					}),
					Ext.create('Ext.form.field.Hidden', { name: CMDBuild.core.constants.Proxy.ID })
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (view, eOpts) {
				this.delegate.cmfg('onTaskManagerFormTaskEventAsynchronousStep1Show');
			}
		}
	});

})();
