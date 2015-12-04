(function() {

	Ext.define('CMDBuild.controller.common.field.comboBox.Erasable', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFieldComboBoxErasableTrigger1Click',
			'onFieldComboBoxErasableTrigger2Click'
		],

		/**
		 * @property {CMDBuild.view.common.field.comboBox.Erasable}
		 */
		view: undefined,

		/**
		 * If store has more than configuration limit records, no drop down but opens searchWindow
		 */
		onFieldComboBoxErasableTrigger1Click: function() {
			this.view.onTriggerClick();
		},

		onFieldComboBoxErasableTrigger2Click: function() {
			if (!this.view.isDisabled())
				this.view.setValue();
		}
	});

})();