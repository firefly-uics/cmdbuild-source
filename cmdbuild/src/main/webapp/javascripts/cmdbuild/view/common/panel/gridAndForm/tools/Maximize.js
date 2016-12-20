(function () {

	Ext.define('CMDBuild.view.common.panel.gridAndForm.tools.Maximize', {
		extend: 'Ext.panel.Tool',

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.GridAndForm}
		 */
		delegate: undefined,

		tooltip: CMDBuild.Translation.maximizeGrid,
		type: 'maximize',

		/**
		 * @param {Ext.EventObject} event
		 * @param {Ext.Element} target
		 * @param {Ext.panel.Header} owner
		 * @param {Ext.panel.Tool} tool
		 *
		 * @returns {Void}
		 */
		handler: function (event, target, owner, tool) {
			this.delegate.cmfg('panelGridAndFromFullScreenUiSetup', {
				force: true,
				maximize: 'top'
			});
		}
	});

})();
