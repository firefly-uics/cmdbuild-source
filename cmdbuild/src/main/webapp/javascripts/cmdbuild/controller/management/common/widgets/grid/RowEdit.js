(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.grid.RowEdit', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.grid.Grid}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onRowEditWindowAbortButtonClick',
			'onRowEditWindowSaveButtonClick'
		],

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		/**
		 * @cfg {Ext.data.Model}
		 */
		record: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.grid.RowEditWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.widgets.grid.Grid} configurationObject.parentDelegate
		 * @param {Object} configurationObject.record
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.grid.RowEditWindow', {
				delegate: this
			});

			// Shorthand
			this.form = this.view.form;

			this.form.add(this.buildFormFields());
			this.form.getForm().setValues(this.record.data); // record.getData() doesn't returns all values (Description and Code) so it's used record.data

			this.fieldsInitialization();

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		/**
		 * @return {Array} itemsArray
		 */
		buildFormFields: function() {
			var itemsArray = [];

			Ext.Array.forEach(this.parentDelegate.getCardAttributes(), function(attribute, i, allAttributes) {
				var item = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);

				if (attribute[CMDBuild.core.proxy.CMProxyConstants.FIELD_MODE] == 'read')
					item.setDisabled(true);

				itemsArray.push(item);
			}, this);

			return itemsArray;
		},

		/**
		 * Calls field template resolver and store load
		 */
		fieldsInitialization: function() {
			Ext.Array.forEach(this.form.getForm().getFields().getRange(), function(field, i, allFields) {
				if (!Ext.Object.isEmpty(field) && !Ext.isEmpty(field.resolveTemplate))
					field.resolveTemplate();

				if (!Ext.Object.isEmpty(field) && !Ext.Object.isEmpty(field.store) && field.store.count() == 0)
					field.store.load();
			}, this);
		},

		onRowEditWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		/**
		 * Saves data to widget's grid
		 */
		onRowEditWindowSaveButtonClick: function() {
			Ext.Object.each(this.form.getValues(), function(key, value, myself) {
				this.record.set(key, value);
			}, this);

			this.onRowEditWindowAbortButtonClick();
		}
	});

})();