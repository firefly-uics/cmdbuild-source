(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.cronForm.CMCronForm', {
		extend: 'Ext.panel.Panel',

		border: false,

		/**
		 * To acquire informations to setup fields before creation
		 *
		 * @param (Object) advancedSetup
		 * @param (Object) baseSetup
		 */
		constructor: function(advancedSetup, baseSetup) {
			this.delegate = Ext.create('CMDBuild.controller.administration.tasks.common.cronForm.CMCronFormController', this);

			if (typeof advancedSetup == 'undefined') {
				this.advancedSetup = { delegate: this.delegate };
			} else {
				this.advancedSetup = advancedSetup;
				this.advancedSetup.delegate = this.delegate;
			}

			if (typeof baseSetup == 'undefined') {
				this.baseSetup = { delegate: this.delegate };
			} else {
				this.baseSetup = baseSetup;
				this.baseSetup.delegate = this.delegate;
			}

			this.callParent(arguments);
		},

		initComponent: function() {
			this.advanced = Ext.create('CMDBuild.view.administration.tasks.common.cronForm.CMCronFormAdvanced', this.advancedSetup);
			this.base = Ext.create('CMDBuild.view.administration.tasks.common.cronForm.CMCronFormBase', this.baseSetup);

			this.delegate.advancedField = this.advanced;
			this.delegate.baseField = this.base;

			Ext.apply(this, {
				items: [this.base, this.advanced]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * To correctly enable radio fields on tab show
			 */
			show: function(view, eOpts) {
				if (this.delegate.isEmptyBase() && !this.delegate.isEmptyAdvanced()) {
					this.advanced.advanceRadio.setValue(true);
				} else {
					this.base.baseRadio.setValue(true);
				}
			}
		}
	});

})();