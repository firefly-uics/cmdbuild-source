(function () {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.view.common.panel.gridAndForm.FormPanel', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.Form}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-blue-panel-no-padding',
		border: false,
		cls: 'cmdb-border-top',
		frame: false,
		height: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.CARD_FORM_RATIO) + '%',
		region: 'south',
		split: true
	});

})();
