(function () {

	Ext.define('CMDBuild.view.administration.taskManager.task.generic.Step5View', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.taskManager.task.generic.Step5}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.taskManager.task.common.field.report.ReportView}
		 */
		fieldReport: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		fieldsetReport: undefined,

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
					this.fieldsetReport = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.report,
						checkboxName: CMDBuild.core.constants.Proxy.REPORT_ACTIVE,
						checkboxToggle: true,
						checkboxUncheckedValue: false,
						checkboxValue: true,
						collapsed: true,
						collapsible: true,
						toggleOnTitleClick: true,
						overflowY: 'auto',

						items: [
							this.fieldReport = Ext.create('CMDBuild.view.administration.taskManager.task.common.field.report.ReportView', {
								config: {
									labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 10
								}
							})
						],

						listeners: {
							scope: this,
							expand: function (field, eOpts) {
								this.delegate.cmfg('onTaskManagerFormTaskGenericStep5FieldsetReportExpand');
							}
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
