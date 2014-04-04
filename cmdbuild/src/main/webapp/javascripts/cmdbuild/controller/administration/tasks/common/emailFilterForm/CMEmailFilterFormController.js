(function() {

	Ext.define('CMDBuild.controller.administration.tasks.common.emailFilterForm.CMEmailFilterFormController', {

		buttonField: undefined,
		textareaField: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onFilterButtonClick':
					return this.onFilterButtonClick(param.titleWindow, param.type);

				case 'onFilterChange':
					return this.onFilterChange(param);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onFilterButtonClick: function(titleWindow, type, content) {
			var me = this;

			this.filterWindow = Ext.create('CMDBuild.view.administration.tasks.email.CMFilterWindow', {
				title: titleWindow,
				type: type,
				content: me.textareaField.getValue()
			});

			this.filterWindow.delegate.parentDelegate = this;
			this.filterWindow.show();
		},

		onFilterChange: function(parameters) {
			var filterString = '';

			for (key in parameters) {
				if (parameters[key] !== '') {
					if (filterString != '')
						filterString = filterString + ' OR ';

					filterString = filterString.concat(parameters[key]);
				}
			}

			this.textareaField.setValue(filterString);
		},

		setValue: function(filterString) {
			this.textareaField.setValue(filterString);
		}
	});

})();