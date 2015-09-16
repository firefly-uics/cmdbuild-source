(function() {

	Ext.define('CMDBuild.controller.administration.filter.Groups', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.filter.Groups',
			'CMDBuild.view.common.field.translatable.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.filter.Filter}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFilterGroupsAbortButtonClick',
			'onFilterGroupsAddButtonClick',
			'onFilterGroupsClassesComboSelect',
			'onFilterGroupsModifyButtonClick = onFilterGroupsItemDoubleClick',
			'onFilterGroupsRemoveButtonClick',
			'onFilterGroupsRowSelected',
			'onFilterGroupsSaveButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.filter.groups.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.filter.groups.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.filter.Groups}
		 */
		selectedFilter: undefined,

		/**
		 * @property {CMDBuild.view.administration.filter.groups.GroupsView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.filter.Filter} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.filter.groups.GroupsView', {
				delegate: this
			});

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		onFilterGroupsAbortButtonClick: function() {
			if (!Ext.isEmpty(this.selectedFilter)) {
				this.onFilterGroupsRowSelected();
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		onFilterGroupsAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.selectedFilter = null;

			this.form.reset();
			this.form.filterChooser.reset(); // Manual filter reset
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.filter.Groups'));
		},

		/**
		 * @param {String} selectedClassName
		 */
		onFilterGroupsClassesComboSelect: function(selectedClassName) {
			if (!Ext.isEmpty(selectedClassName)) {
				this.form.filterChooser.setClassName(selectedClassName);
			} else {
				_error('empty selectedClassName in onFilterGroupsClassesComboSelect', this);
			}
		},

		onFilterGroupsModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onFilterGroupsRemoveButtonClick: function() {
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
		onFilterGroupsRowSelected: function() {
			this.selectedFilter = this.grid.getSelectionModel().getSelection()[0];

			this.form.loadRecord(this.selectedFilter);

			// FilterChooser field setup
			this.form.filterChooser.setClassName(this.selectedFilter.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE));
			this.form.filterChooser.setFilter(
				Ext.create('CMDBuild.model.CMFilterModel', {
					configuration: this.selectedFilter.get(CMDBuild.core.constants.Proxy.CONFIGURATION),
					entryType: this.selectedFilter.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE)
				})
			);

			this.form.setDisabledModify(true, true);
		},

		onFilterGroupsSaveButtonClick: function() {
			// Validate before save
			if (this.validate(this.form)) {
				var formData = this.form.getData(true);

				if (!Ext.isEmpty(this.form.filterChooser.getFilter()))
					formData[CMDBuild.core.constants.Proxy.CONFIGURATION] = Ext.encode(this.form.filterChooser.getFilter().getConfiguration());

				formData = Ext.create('CMDBuild.model.filter.Groups', formData); // Filter unwanted data of filterChooser internal fields

				// TODO: needed a refactor because i read a entryType parameter but i write as className
				var params = formData.getData();
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = params[CMDBuild.core.constants.Proxy.ENTRY_TYPE];

				if (Ext.isEmpty(formData.get(CMDBuild.core.constants.Proxy.ID))) {
					CMDBuild.core.proxy.filter.Groups.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.filter.Groups.update({
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
				params[CMDBuild.core.constants.Proxy.ID] = this.selectedFilter.get(CMDBuild.core.constants.Proxy.ID);

				CMDBuild.core.proxy.filter.Groups.remove({
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

			CMDBuild.view.common.field.translatable.Utils.commit(this.view.form);

			this.grid.getStore().load({
				callback: function(records, operation, success) {
					var rowIndex = this.find(
						CMDBuild.core.constants.Proxy.NAME,
						me.form.getForm().findField(CMDBuild.core.constants.Proxy.NAME).getValue()
					);

					me.grid.getSelectionModel().select(rowIndex, true);
					me.form.setDisabledModify(true);
				}
			});
		}
	});

})();