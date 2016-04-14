Ext.require(['CMDBuild.proxy.utility.ExportCsv']);

Ext.define("CMDBuild.view.management.utilities.CMModExportCSV", {
	extend: "Ext.panel.Panel",
	cmName: 'exportcsv',
	layout: 'border',
	hideMode:  'offsets',
	frame: true,
	border: false,
	translation: CMDBuild.Translation.management.modutilities.csv,

	initComponent: function() {

		this.exportBtn = Ext.create('CMDBuild.core.buttons.text.Export', {
			scope: this,
			handler: function (button, e){
				var params = this.form.getValues();
				CMDBuild.proxy.utility.ExportCsv.download({
					form: this.form.getForm(),
					params: params
				});
			}
		});

		this.classList = new CMDBuild.view.common.field.CMBaseCombo({
			store: _CMCache.getClassesStore(),
			fieldLabel : this.translation.selectaclass,
			queryMode: 'local',
			name : CMDBuild.core.constants.Proxy.CLASS_NAME,
			hiddenName : CMDBuild.core.constants.Proxy.CLASS_NAME,
			valueField : 'name',
			displayField : 'description',
			allowBlank : false,
			editable: false
		});

		this.separator = new Ext.form.ComboBox({
			name: 'separator',
			fieldLabel: this.translation.separator,
			valueField: 'value',
			displayField: 'value',
			hiddenName: 'separator',
			store: new Ext.data.SimpleStore({
				fields: ['value'],
				data : [[';'],[','],['|']]
			}),
			width: 150,
			value: ";",
			queryMode: 'local',
			editable: false,
			allowBlank: false
		});

		this.form = Ext.create('Ext.form.Panel', {
			region: 'center',
			bodyCls: 'cmdb-blue-panel-no-padding',
			frame: false,
			border: false,
			monitorValid: true,
			standardSubmit: true, // IE Fix (see exportBtn for more)
			items:	[
				this.classList,
				this.separator,
				this.exportBtn
			]
		});

		Ext.apply(this, {
			title: CMDBuild.Translation.management.modutilities.csv.title_export,
			tools: [
				Ext.create('CMDBuild.view.common.panel.gridAndForm.tools.Properties', {
					style: {} // Reset margin setup
				})
			],
			items:[this.form]
		});

		this.callParent(arguments);
	}
});