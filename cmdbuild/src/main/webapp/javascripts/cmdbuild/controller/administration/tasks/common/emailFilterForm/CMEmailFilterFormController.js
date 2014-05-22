(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyTasks');

	Ext.define('CMDBuild.controller.administration.tasks.common.emailFilterForm.CMEmailFilterFormController', {

		buttonField: undefined,
		filterWindow: undefined,
		textAreaFieldValueBuffer: undefined,
		textareaConcatParameter: ' OR ',
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

		/**
		 * Concats array's items with textareaConcatParameter
		 *
		 * @param (Array) parameters
		 *
		 * @return (String) filterString
		 */
		filterStringBuild: function(parameters) {
			if (typeof parameters == 'object') {
				var filterString = '';

				for (key in parameters) {
					if (!Ext.isEmpty(parameters[key])) {
						if (!Ext.isEmpty(filterString))
							filterString = filterString + this.getTextareaConcatParameter();

						filterString = filterString.concat(parameters[key]);
					}
				}
			} else  {
				var filterString = parameters;
			}

			return filterString;
		},

		// GETters functions
			/**
			 * @return (String)
			 */
			getTextareaConcatParameter: function() {
				return this.textareaConcatParameter;
			},

		/**
		 * Creates filter window structure
		 *
		 * @param (String) titleWindow
		 * @param (String) type
		 * @param (String) content
		 */
		onFilterButtonClick: function(titleWindow, type, content) {
			this.filterWindow = Ext.create('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormWindow', {
				title: titleWindow,
				type: type,
				content: this.textareaField.getValue(),
				textareaConcatParameter: this.getTextareaConcatParameter()
			});

			this.filterWindow.delegate.parentDelegate = this;
			this.filterWindow.show();
		},

		/**
		 * @param (Array) parameters
		 */
		onFilterChange: function(parameters) {
			if (Ext.isEmpty(this.textAreaFieldValueBuffer))
				this.textAreaFieldValueBuffer = this.textareaField.getValue();

			this.textareaField.setValue(this.filterStringBuild(parameters));
		},

		onFilterWindowAbort: function() {
			if (Ext.isEmpty(this.textAreaFieldValueBuffer)) {
				this.textareaField.setValue(this.textAreaFieldValueBuffer);
			} else {
				this.textareaField.reset();
			}

			this.filterWindow.hide();
		},

		// SETters functions
			/**
			 * @param (String) value
			 */
			setValue: function(value) {
				this.textareaField.setValue(value);
			}
	});

})();