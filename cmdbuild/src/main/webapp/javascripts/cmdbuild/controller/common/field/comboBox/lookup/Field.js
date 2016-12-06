(function () {

	/**
	 * @private
	 */
	Ext.define('CMDBuild.controller.common.field.comboBox.lookup.Field', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.proxy.common.field.comboBox.Lookup'],

		/**
		 * @cfg {CMDBuild.controller.common.field.comboBox.lookup.Lookup}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldComboBoxLookupFieldIsValid',
			'fieldComboBoxLookupFieldReset = onFieldComboBoxLookupFieldTrigger2Click',
			'fieldComboBoxLookupFieldStoreFilterByValue',
			'fieldComboBoxLookupFieldValueGet',
			'fieldComboBoxLookupFieldValueSet',
			'onFieldComboBoxLookupFieldChange',
			'onFieldComboBoxLookupFieldTrigger1Click'
		],

		/**
		 * @cfg {String}
		 */
		type: undefined,

		/**
		 * @property {CMDBuild.view.common.field.comboBox.lookup.Field}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.comboBox.lookup.Lookup} configurationObject.parentDelegate
		 * @param {String} configurationObject.type
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			// Error handling
				if (!Ext.isString(this.type) || Ext.isEmpty(this.type))
					return _error('constructor(): unmanaged type parameter', this, type);
			// END: Error handling

			this.view = Ext.create('CMDBuild.view.common.field.comboBox.lookup.Field', {
				delegate: this,
				store: CMDBuild.proxy.common.field.comboBox.Lookup.getStore({ type: this.type })
			});
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Boolean}
		 */
		fieldComboBoxLookupFieldIsValid: function () {
			this.view.isValid();
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		fieldComboBoxLookupFieldReset: function () {
			this.view.reset();
		},

		/**
		 * @param {Number} value
		 *
		 * @returns {Void}
		 */
		fieldComboBoxLookupFieldStoreFilterByValue: function (value) {
			this.view.getStore().clearFilter();

			if (Ext.isNumber(value) && !Ext.isEmpty(value))
				this.view.getStore().filter('ParentId', value);
		},

		/**
		 * Returns value only if number
		 *
		 * @returns {Number or null}
		 */
		fieldComboBoxLookupFieldValueGet: function () {
			var value = this.view.getValue();

			return Ext.isNumber(value) ? value : null;
		},

		/**
		 * Defer value set on load event
		 *
		 * @param {Number} value
		 *
		 * @returns {Void}
		 */
		fieldComboBoxLookupFieldValueSet: function (value) {
			if (this.view.getStore().isLoading() || this.view.getStore().getCount() == 0) {
				this.view.getStore().on('load', function (store, records, successful, eOpts) {
					this.setValue(value);
				}, this, { single: true });
			} else {
				this.setValue(value);
			}
		},

		/**
		 * @returns {Void}
		 */
		onFieldComboBoxLookupFieldChange: function () {
			var value = this.view.getValue();

			if (Ext.isEmpty(value)) { // Catch reset event
				this.cmfg('onFieldComboBoxLookupReset', this);
			} else { // Catch setValue event
				var record = this.view.getStore().getAt(this.view.getStore().find('Id', value));

				// Error handling
					if (!Ext.isObject(record) || Ext.Object.isEmpty(record))
						return _error('onFieldComboBoxLookupFieldChange(): record not found', this, value, record);
				// END: Error handling

				this.cmfg('onFieldComboBoxLookupParentUpdate', {
					controller: this,
					value: record.get('ParentId')
				});
			}
		},

		/**
		 * If store has more than configuration limit records, no drop down but opens searchWindow
		 *
		 * @returns {Void}
		 */
		onFieldComboBoxLookupFieldTrigger1Click: function () {
			this.view.onTriggerClick();
		},

		/**
		 * Apply filter on selected record ParentId value
		 *
		 * @param {Number} value
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		setValue: function (value) {
			if (Ext.isNumber(value) && !Ext.isEmpty(value))
				this.view.setValue(value);

			this.view.validate();

			var record = this.view.getStore().getAt(
				this.view.getStore().find('Id', value)
			);

			if (Ext.isObject(record) && !Ext.Object.isEmpty(record))
				this.cmfg('fieldComboBoxLookupFieldStoreFilterByValue', record.get('ParentId'));
		}
	});

})();
