(function() {

	Ext.define('CMDBuild.controller.administration.domain.Properties', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.domain.Domain'
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
			'onDomainPropertiesCardinalitySelect',
			'onDomainPropertiesDomainSelected = onDomainSelected',
			'onDomainPropertiesMasterDetailCheckboxChange',
			'onDomainPropertiesModifyButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.domain.properties.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.domain.properties.PropertiesView}
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

			this.view = Ext.create('CMDBuild.view.administration.domain.properties.PropertiesView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		/**
		 * @returns {Object} data
		 *
		 * @public
		 */
		getData: function() {
			var data = this.form.getData(true);

			// TODO: waiting for refactor (server side variables rename)
			data['descr_1'] = data[CMDBuild.core.constants.Proxy.DIRECT_DESCRIPTION];
			data['descr_2'] = data[CMDBuild.core.constants.Proxy.INVERSE_DESCRIPTION];
			data['idClass1'] = data[CMDBuild.core.constants.Proxy.ORIGIN_CLASS_ID];
			data['idClass2'] = data[CMDBuild.core.constants.Proxy.DESTINATION_CLASS_ID];
			data['md_label'] = data[CMDBuild.core.constants.Proxy.MASTER_DETAIL_LABEL];
			data[CMDBuild.core.constants.Proxy.ID] = Ext.isEmpty(data[CMDBuild.core.constants.Proxy.ID]) ? -1 : data[CMDBuild.core.constants.Proxy.ID];

			return data;
		},

		onDomainPropertiesAbortButtonClick: function() {
			if (this.cmfg('domainSelectedDomainIsEmpty')) {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			} else {
				this.onDomainPropertiesDomainSelected();
			}
		},

		onDomainPropertiesAddButtonClick: function() {
			this.cmfg('domainSelectedDomainSet');

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.domain.Domain'));

			this.onDomainPropertiesCardinalitySelect(); // Execute cardinality selection event actions to disable masterDetailCheckbox
		},

		/**
		 * A domain could set MD only if the cardinality is '1:N' or 'N:1'
		 */
		onDomainPropertiesCardinalitySelect: function() {
			if (
				!Ext.isEmpty(this.form.cardinalityCombo.getValue())
				&& (
					this.form.cardinalityCombo.getValue() == '1:N'
					|| this.form.cardinalityCombo.getValue() == 'N:1'
				)
			) {
				this.form.masterDetailCheckbox.enable();
			} else {
				this.form.masterDetailCheckbox.setValue(false);
				this.form.masterDetailCheckbox.disable();
			}
		},

		onDomainPropertiesDomainSelected: function() {
			if (!this.cmfg('domainSelectedDomainIsEmpty')) {
				this.form.reset();
				this.form.setDisabledModify(true);
				this.form.loadRecord(this.cmfg('domainSelectedDomainGet'));
			}
		},

		/**
		 * Show the masterDetailLabel field only when the domain is setted as a masterDetail
		 */
		onDomainPropertiesMasterDetailCheckboxChange: function() {
			if (this.form.masterDetailCheckbox.getValue()) {
				this.form.masterDetailLabel.show();
				this.form.masterDetailLabel.setDisabled(this.form.masterDetailCheckbox.isDisabled());
			} else {
				this.form.masterDetailLabel.hide();
				this.form.masterDetailLabel.disable();
			}
		},

		onDomainPropertiesModifyButtonClick: function() {
			this.form.setDisabledModify(false);

			this.onDomainPropertiesCardinalitySelect(); // Execute cardinality selection event actions to disable masterDetailCheckbox
		}
	});

})();