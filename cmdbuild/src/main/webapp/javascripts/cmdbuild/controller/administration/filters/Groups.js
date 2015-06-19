(function() {

	Ext.define('CMDBuild.controller.administration.filters.Groups', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.filters.Groups',
			'CMDBuild.model.filters.Groups'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.filters.Filters}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFiltersGroupsAbortButtonClick',
			'onFiltersGroupsAddButtonClick',
			'onFiltersGroupsClassesComboSelect',
			'onFiltersGroupsModifyButtonClick = onFiltersGroupsItemDoubleClick',
			'onFiltersGroupsRemoveButtonClick',
			'onFiltersGroupsRowSelected',
			'onFiltersGroupsSaveButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.filters.groups.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.filters.groups.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.filters.Groups}
		 */
		selectedFilter: undefined,

		/**
		 * @property {CMDBuild.view.administration.filter.groups.GroupsView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.filters.Filters} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.filters.groups.GroupsView', {
				delegate: this
			});

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		onFiltersGroupsAbortButtonClick: function() {
			if (!Ext.isEmpty(this.selectedFilter)) {
				this.onFiltersGroupsRowSelected();
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		onFiltersGroupsAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.selectedFilter = null;

			this.form.reset();
			this.form.filterChooser.reset(); // Manual filter reset
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.filters.Groups'));
		},

		/**
		 * @param {String} selectedClassName
		 */
		onFiltersGroupsClassesComboSelect: function(selectedClassName) {
			if (!Ext.isEmpty(selectedClassName)) {
				this.form.filterChooser.setClassName(selectedClassName);
			} else {
				_error('empty selectedClassName in onFiltersGroupsClassesComboSelect', this);
			}
		},

		onFiltersGroupsModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onFiltersGroupsRemoveButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == 'yes')
						this.removeItem();
				}
			});
		},

		/**
		 * TODO: server implementation to get a single view data
		 */
		onFiltersGroupsRowSelected: function() {
			this.selectedFilter = this.grid.getSelectionModel().getSelection()[0];

			this.form.loadRecord(this.selectedFilter);

			// FilterChooser field setup
			this.form.filterChooser.setClassName(this.selectedFilter.get(CMDBuild.core.proxy.Constants.ENTRY_TYPE));
			this.form.filterChooser.setFilter(
				Ext.create('CMDBuild.model.CMFilterModel', {
					configuration: this.selectedFilter.get(CMDBuild.core.proxy.Constants.CONFIGURATION),
					entryType: this.selectedFilter.get(CMDBuild.core.proxy.Constants.ENTRY_TYPE)
				})
			);

			// Translation setup
			Ext.apply(this.form.descriptionTextField, {
				translationsKeyName: this.selectedFilter.get(CMDBuild.core.proxy.Constants.NAME)
			});

			this.form.setDisabledModify(true, true);
		},

		onFiltersGroupsSaveButtonClick: function() {
			// Validate before save
			if (this.validate(this.form)) {
				var formData = this.form.getData(true);

				if (!Ext.isEmpty(this.form.filterChooser.getFilter()))
					formData[CMDBuild.core.proxy.Constants.CONFIGURATION] = Ext.encode(this.form.filterChooser.getFilter().getConfiguration());

				formData = Ext.create('CMDBuild.model.filters.Groups', formData); // Filter unwanted data of filterChooser internal fields

				// TODO: needed a refactor because i read a entryType parameter but i write as className
				var params = formData.getData();
				params[CMDBuild.core.proxy.Constants.CLASS_NAME] = params[CMDBuild.core.proxy.Constants.ENTRY_TYPE];

				if (Ext.isEmpty(formData.get(CMDBuild.core.proxy.Constants.ID))) {
					CMDBuild.core.proxy.filters.Groups.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.filters.Groups.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		removeItem: function() {
			if (!Ext.isEmpty(this.selectedFilter)) {
				var params = {};
				params[CMDBuild.core.proxy.Constants.ID] = this.selectedFilter.get(CMDBuild.core.proxy.Constants.ID);

				CMDBuild.core.proxy.filters.Groups.remove({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.form.reset();

						this.grid.getStore().load({
							scope: this,
							callback: function(records, operation, success) {
								this.grid.getSelectionModel().select(0, true);

								// If no selections disable all UI
								if (!this.grid.getSelectionModel().hasSelection())
									this.form.setDisabledModify(true, true, true, true);
							}
						});
					}
				});
			}
		},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 */
		success: function(result, options, decodedResult) {
			var me = this;

			_CMCache.flushTranslationsToSave(options.params[CMDBuild.core.proxy.Constants.NAME]);

			this.grid.getStore().load({
				callback: function(records, operation, success) {
					var rowIndex = this.find(
						CMDBuild.core.proxy.Constants.NAME,
						me.form.getForm().findField(CMDBuild.core.proxy.Constants.NAME).getValue()
					);

					me.grid.getSelectionModel().select(rowIndex, true);
					me.form.setDisabledModify(true);
				}
			});
		}
	});

})();