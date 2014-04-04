(function() {

	Ext.define('CMDBuild.view.administration.tasks.CMTasksWizard', {
		extend: 'Ext.tab.Panel',

		previousButton: undefined,
		nextButton: undefined,

		activeTab: 0,
		numberOfTabs: 0,
		width: '100%',
		height: '100%',
		frame: false,
		border: false,

		defaults: {
			bodyPadding: 10,
			layout: 'anchor'
		},

		initComponent: function() {
			this.callParent(arguments);

			this.getTabBar().setVisible(false);
		},

		/**
		 * To change wizard displayed tab
		 *
		 * @param (Int) step
		 */
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

			// Fire show event on tabs activate
			var currentActiveTab = this.getActiveTab();

			if (currentActiveTab)
				currentActiveTab.fireEvent('show');
		}
	});

})();