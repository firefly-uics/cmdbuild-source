(function() {

	var tr = CMDBuild.Translation.administration.tasks.workflowForm;

	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormGrid', {
		extend: 'CMDBuild.view.administration.common.CMDynamicKeyValueGrid',

		delegate: undefined,

		title: tr.attributes,
		keyLabel: CMDBuild.Translation.name,
		valueLabel: CMDBuild.Translation.value,
		disabled: true,

		initComponent: function() {
			var me = this;

			this.keyEditorConfig = {
				xtype: 'combo',
				valueField: CMDBuild.ServiceProxy.parameter.VALUE,
				displayField: CMDBuild.ServiceProxy.parameter.VALUE,
				forceSelection: true,
				editable: false,
				allowBlank: false,

				listeners: {
					select: function(combo, records, eOpts) {
						me.delegate.onAttributeComboSelect(me.store.indexOf(me.delegate.gridField.getSelectionModel().getSelection()[0]));
					}
				}
			},

			this.callParent(arguments);
		}
	});

})();