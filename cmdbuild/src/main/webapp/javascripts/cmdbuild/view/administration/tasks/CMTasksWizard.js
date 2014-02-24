(function() {

	Ext.define('CMDBuild.view.administration.tasks.CMTasksWizard', {
		extend: 'Ext.tab.Panel',

		previousButton: undefined,
		nextButton: undefined,

		activeTab: 0,
		numberOfTabs: 0,
		width: '100%',
		height: '100%',
		border: false,

		defaults: {
			bodyPadding: 10,
			layout: 'anchor'
		},

		items: [],

		initComponent: function() {
			this.callParent(arguments);

			this.getTabBar().setVisible(false);
		},

		changeTab: function(step) {
			if (typeof step === 'number' && step == 0) {
				var activeTab = 0;
				this.setActiveTab(0);
			} else {
				var activeTab = this.items.indexOf(this.activeTab);

				if (
					activeTab + step >= 0
					&& activeTab + step < this.numberOfTabs
				) {
					activeTab = activeTab + step;
					this.setActiveTab(activeTab);
				}
			}

			if (activeTab == 0) {
				this.previousButton.disable();
			} else {
				this.previousButton.enable();
			}

			if (activeTab == this.numberOfTabs - 1) {
				this.nextButton.disable();
			} else {
				this.nextButton.enable();
			}
		}
	});

})();