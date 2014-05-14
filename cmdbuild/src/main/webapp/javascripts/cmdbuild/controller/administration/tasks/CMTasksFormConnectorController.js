(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyEmailAccounts');

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksFormConnectorController", {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		delegateStep: undefined,
		parentDelegate: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: 'connector',
		view: undefined,

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
		getStoreFilteredClass: function() {
			var store = _CMCache.getClassesStore();

			store.filterBy(
				function(record, id) {
					if (CMDBuild.Utils.inArray(record.get(CMDBuild.ServiceProxy.parameter.NAME), this.delegateStep[3].getSelectedClassArray())) {
						_debug('true');
						return true;
					}
					_debug('false');
					return false;
				}, this
			);

			return store;
		},

		/**
		 * Filter source store to delete unselected views
		 *
		 * @return (Object) store
		 */
		getStoreFilteredSource: function() {
			var sourceArray = this.delegateStep[3].getSelectedSourceArray();
			var store = Ext.create('Ext.data.Store', {
				autoLoad: true,
				fields: [CMDBuild.ServiceProxy.parameter.NAME],
				data: []
			});

			for(item in sourceArray)
				store.add({ name: sourceArray[item] });

			return store;
		},

		// overwrite
		onSaveButtonClick: function() {
			var formData = this.view.getData(true);

_debug('Step 4 datas [3]');
_debug(this.delegateStep[3].getData());

_debug('Step 5 datas [4]');
_debug(this.delegateStep[4].getData());
_debug(Ext.encode(this.delegateStep[4].getData()));

_debug('Step 6 datas [5]');
_debug(Ext.encode(this.delegateStep[5].getData()));

_debug('Filtered class and source');
_debug(this.getFilteredClassStore());
_debug(this.getFilteredSourceStore());

_debug(formData);

_debug('onSaveButtonClick to implement');
		}
	});

})();