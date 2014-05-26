(function() {

	var tr = CMDBuild.Translation.administration.tasks.workflowForm;

	// TODO: to update without extends CMDynamicKeyValueGrid
	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormGrid', {
		extend: 'CMDBuild.view.administration.common.CMDynamicKeyValueGrid',

		delegate: undefined,

		title: tr.attributes,
		keyLabel: CMDBuild.Translation.name,
		valueLabel: CMDBuild.Translation.value,
		considerAsFieldToDisable: true,
		margin: '0 0 5 0',

		initComponent: function() {
			var me = this;

			this.keyEditorConfig = {
				xtype: 'combo',
				valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				displayField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				forceSelection: true,
				editable: false,
				allowBlank: false,

				queryMode: 'local',

				listeners: {
					select: function(combo, records, eOpts) {
						me.delegate.cmOn('onSelectAttributeCombo', me.store.indexOf(me.delegate.gridField.getSelectionModel().getSelection()[0]));
					}
				}
			},

			this.callParent(arguments);
		}
	});

})();
