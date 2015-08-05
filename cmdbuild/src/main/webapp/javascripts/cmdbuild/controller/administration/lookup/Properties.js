(function() {

	Ext.define('CMDBuild.controller.administration.lookup.Properties', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.lookup.Type',
			'CMDBuild.model.lookup.Type'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.lookup.Lookup}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLookupPropertiesAbortButtonClick',
			'onLookupPropertiesAddButtonClick',
			'onLookupPropertiesModifyButtonClick',
			'onLookupPropertiesSaveButtonClick',
			'onLookupPropertiesTabShow'
		],

		/**
		 * @property {CMDBuild.view.administration.lookup.properties.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.lookup.properties.PropertiesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.lookup.Lookup} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.lookup.properties.PropertiesView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		onLookupPropertiesAbortButtonClick: function() {
			if (this.cmfg('selectedLookupTypeIsEmpty')) {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			} else {
				this.onLookupPropertiesTabShow();
			}
		},

		onLookupPropertiesAddButtonClick: function() {
			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.lookup.Type'));
		},

		onLookupPropertiesModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onLookupPropertiesSaveButtonClick: function() {
			// Validate before save
			if (this.validate(this.form)) {
				var formData = this.form.getData(true);
				formData['orig_type'] = formData[CMDBuild.core.proxy.Constants.ID]; // TODO: wrong server implementation to fix

				CMDBuild.core.proxy.lookup.Type.save({ // TODO: server side refactor needed to follow new CMDBuild standards (create/update)
					params: formData,
					scope: this,
					success: this.success
				});
			}
		},

		onLookupPropertiesTabShow: function() {
			if (!this.cmfg('selectedLookupTypeIsEmpty')) {
				this.form.reset();
				this.form.setDisabledModify(true);
				this.form.loadRecord(this.cmfg('selectedLookupTypeGet'));
			}
		},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 *
		 * TODO: server side refactor needed to follow new CMDBuild standards
		 */
		success: function(result, options, decodedResult) {
			if (!Ext.isEmpty(decodedResult.isNew)) {
				_CMCache.onNewLookupType(decodedResult.lookup);
			} else {
				_CMCache.onModifyLookupType(decodedResult.lookup);
			}

			this.form.setDisabledModify(true);
		}
	});

})();