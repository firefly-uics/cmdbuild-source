(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.event.synchronous.Step1View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.taskManager.task.event.Synchronous'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.event.synchronous.Step1}
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
						value: CMDBuild.Translation.eventSynchronous
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
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.PHASE,
						fieldLabel: CMDBuild.Translation.phase,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						valueField: CMDBuild.core.constants.Proxy.VALUE,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						forceSelection: true,
						editable: false,

						store: CMDBuild.proxy.administration.taskManager.task.event.Synchronous.getStorePhases(),
						queryMode: 'local'
					}),
					Ext.create('CMDBuild.view.common.field.multiselect.Group', {
						name: CMDBuild.core.constants.Proxy.GROUPS,
						fieldLabel: CMDBuild.Translation.groupsToApply,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						height: 300
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

						store: CMDBuild.proxy.administration.taskManager.task.event.Synchronous.getStoreEntryTypes(),
						queryMode: 'local',

						listeners: {
							scope: this,
							select: function (field, records, options) {
								this.delegate.cmfg('onTaskManagerFormTaskEventSynchronousEntryTypeSelected');
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
				this.delegate.cmfg('onTaskManagerFormTaskEventSynchronousStep1Show');
			}
		}
	});

})();
