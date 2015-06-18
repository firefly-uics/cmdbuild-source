(function() {

	Ext.define('CMDBuild.controller.administration.domain.Properties', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Domain'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.domain.Domain}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onDomainPropertiesAbortButtonClick',
			'onDomainPropertiesAddButtonClick',
			'onDomainPropertiesModifyButtonClick',
			'onDomainPropertiesCardinalitySelect',
			'onDomainPropertiesMasterDetailCheckboxChange',
		],

		/**
		 * @cfg {CMDBuild.view.administration.domain.PropertiesForm}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.domain.Domain} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.domain.PropertiesForm', {
				delegate: this
			});
		},

		onDomainPropertiesAbortButtonClick: function() {
			if (Ext.isEmpty(this.cmfg('selectedDomainGet'))) {
				this.view.reset();
				this.view.setDisabledModify(true, true, true);
			} else {
				this.onDomainSelected(this.cmfg('selectedDomainGet'));
			}
		},

		onDomainPropertiesAddButtonClick: function() {
			this.cmfg('selectedDomainSet');

			this.view.reset();
			this.view.setDisabledModify(false, true);
			this.view.loadRecord(Ext.create('CMDBuild.model.Domain'));

			this.onDomainPropertiesCardinalitySelect(); // Execute cardinality selection event actions to disable masterDetailCheckbox

			_CMCache.initAddingTranslations();
		},

		/**
		 * A domain could set MD only if the cardinality is '1:N' or 'N:1'
		 */
		onDomainPropertiesCardinalitySelect: function() {
			if (
				!Ext.isEmpty(this.view.cardinalityCombo.getValue())
				&& (
					this.view.cardinalityCombo.getValue() == '1:N'
					|| this.view.cardinalityCombo.getValue() == 'N:1'
				)
			) {
				this.view.masterDetailCheckbox.enable();
			} else {
				this.view.masterDetailCheckbox.setValue(false);
				this.view.masterDetailCheckbox.disable();
			}
		},

		/**
		 * Show the masterDetailLabel field only when the domain is setted as a masterDetail
		 */
		onDomainPropertiesMasterDetailCheckboxChange: function() {
			if (this.view.masterDetailCheckbox.getValue()) {
				this.view.masterDetailLabel.show();
				this.view.masterDetailLabel.setDisabled(this.view.masterDetailCheckbox.isDisabled());
			} else {
				this.view.masterDetailLabel.hide();
				this.view.masterDetailLabel.disable();
			}
		},

		onDomainPropertiesModifyButtonClick: function() {
			this.view.setDisabledModify(false);

			this.onDomainPropertiesCardinalitySelect(); // Execute cardinality selection event actions to disable masterDetailCheckbox

			_CMCache.initModifyingTranslations();
		},

		onDomainSelected: function() {
			var selectedDomain = this.cmfg('selectedDomainGet');

			if (!Ext.isEmpty(selectedDomain)) {
				this.view.domainDescription.translationsKeyName = selectedDomain.get(CMDBuild.core.proxy.Constants.NAME);
				this.view.directDescription.translationsKeyName = selectedDomain.get(CMDBuild.core.proxy.Constants.NAME);
				this.view.inverseDescription.translationsKeyName = selectedDomain.get(CMDBuild.core.proxy.Constants.NAME);
				this.view.masterDetailLabel.translationsKeyName = selectedDomain.get(CMDBuild.core.proxy.Constants.NAME);

				this.view.reset();
				this.view.setDisabledModify(true);
				this.view.loadRecord(selectedDomain);
			}
		}
	});

})();