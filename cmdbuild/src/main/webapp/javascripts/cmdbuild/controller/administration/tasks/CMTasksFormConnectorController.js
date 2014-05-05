(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyEmailAccounts');

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksFormConnectorController", {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		parentDelegate: undefined,
		delegateStep: undefined,
		view: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: 'connector',


		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();

				case 'onAddButtonClick':
					return this.onAddButtonClick(name, param, callBack);

				case 'onClassSelected':
					this.onClassSelected(param.className);

				case 'onCloneButtonClick':
					return this.onCloneButtonClick();

				case 'onModifyButtonClick':
					return this.onModifyButtonClick();

				case 'onRemoveButtonClick':
					return this.onRemoveButtonClick();

				case 'onRowSelected':
					return this.onRowSelected();

				case 'onSaveButtonClick':
					return this.onSaveButtonClick();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Filter class store to delete unselected classes
		 *
		 * @return (Object) store
		 */
		getFilteredClassStore: function() {
			var store = _CMCache.getClassesStore();

			store.filterBy(
				function(record, id) {
					if (CMDBuild.Utils.inArray(record.get(CMDBuild.ServiceProxy.parameter.NAME), this.delegateStep[3].getSelectedClassArray()))
						return true;

					return false;
				}, this
			);

			return store;
		},

		/**
		 * Filter view store to delete unselected views
		 *
		 * @return (Object) store
		 */
		getFilteredViewStore: function() {
			var store = CMDBuild.core.proxy.CMProxyTasks.getViewStore();

			store.filterBy(
				function(record, id) {
					if (CMDBuild.Utils.inArray(record.get(CMDBuild.ServiceProxy.parameter.NAME), this.delegateStep[3].getSelectedViewArray()))
						return true;

					return false;
				}, this
			);

			return store;
		},

		// overwrite
		onSaveButtonClick: function() {
			var formData = this.view.getData(true);

_debug('Step 4 datas [3]');
_debug(this.delegateStep[3].getData());

_debug('Step 5 datas [4]');
_debug(this.delegateStep[4].getData());

_debug('Step 6 datas [5]');
_debug(this.delegateStep[5].getData());

_debug(formData);

_debug('onSaveButtonClick to implement');
		}
	});

})();