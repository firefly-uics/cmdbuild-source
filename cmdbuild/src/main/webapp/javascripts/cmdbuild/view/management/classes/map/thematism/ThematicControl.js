(function() {
	Ext.define('CMDBuild.view.management.classes.map.thematism.ThematicControl', {
		extend : 'Ext.panel.Panel',
		interactionDocument : undefined,
		initComponent : function() {
			var configuration = this.interactionDocument.getConfigurationMap();
			var divControl = document.createElement('div');
			divControl.innerHTML = '<h1>pippo</h1>';

			var divContainerControl = document.createElement('div');
			divContainerControl.className = "laycntrl-mapcont";
			divContainerControl.id = "laycntrl-mapcont";
			document.getElementById(configuration.mapDivId).appendChild(divContainerControl);
			this.callParent(arguments);
		}
	});
})();
