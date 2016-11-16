(function () {

	/**
	 * Managed metadata names
	 */
	Ext.define('CMDBuild.core.constants.Metadata', {

		singleton: true,

		config: {
			activitySubsetId: 'ActivitySubsetId', // Workflow
			additionalActivityLabel: 'AdditionalActivityLabel', // Workflow
			nextActivitySubsetId: 'NextActivitySubsetId', // Workflow
			selectedAttributesGroup: 'SelectedAttributesGroup' // Workflow
		}
	});

})();
