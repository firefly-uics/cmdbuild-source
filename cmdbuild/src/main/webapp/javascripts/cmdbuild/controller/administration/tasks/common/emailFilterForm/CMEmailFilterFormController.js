(function() {

	Ext.define('CMDBuild.controller.administration.tasks.common.emailFilterForm.CMEmailFilterFormController', {

		buttonField: undefined,
		filterWindow: undefined,
		textareaField: undefined,
		textareaConcatParameter: ' OR ',

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

				case 'onFilterWindowChange':
					return this.onFilterChange(param);

				case 'onFilterWindowAbort':
					return this.onFilterWindowAbort();

				case 'onFilterWindowConfirm':
					return this.filterWindow.hide();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		getTextareaConcatParameter: function() {
			return this.textareaConcatParameter;
		},

		onFilterButtonClick: function(titleWindow, type, content) {
			var me = this;

			this.filterWindow = Ext.create('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormWindow', {
				title: titleWindow,
				type: type,
				content: me.textareaField.getValue(),
				textareaConcatParameter: this.getTextareaConcatParameter()
			});

			this.filterWindow.delegate.parentDelegate = this;
			this.filterWindow.show();
		},

		onFilterChange: function(parameters) {
			var filterString = '';

			for (key in parameters) {
				if (parameters[key] !== '') {
					if (filterString != '')
						filterString = filterString + this.getTextareaConcatParameter();

					filterString = filterString.concat(parameters[key]);
				}
			}

			this.textareaField.setValue(filterString);
		},

		onFilterWindowAbort: function() {
			// TODO: Fix reverting edits to store data
			this.textareaField.reset();
			this.filterWindow.hide();
		},

		setValue: function(filterString) {
			this.textareaField.setValue(filterString);
		}
	});

})();