(function() {

	/**
	 * New class than will replace CMFormFunctions
	 */
	Ext.define('CMDBuild.view.common.PanelFunctions', {

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @param {Boolean} withDisabled
		 */
		getData: function(withDisabled) {
			if (withDisabled) {
				var data = {};

				this.cascade(function(item) {
					if (
						!Ext.isEmpty(item)
						&& Ext.isFunction(item.getValue)
						&& (
							item instanceof Ext.form.Field
							|| item instanceof Ext.form.field.Base
							|| item instanceof Ext.form.field.HtmlEditor
							|| item instanceof Ext.form.FieldContainer
						)
					) {
						data[item.name] = item.getValue();
					}
				}, this);

				return data;
			} else {
				return this.getForm().getValues();
			}
		},

		getNonValidFields: function() {
			var data = [];

			this.cascade(function(item) {
				if (
					!Ext.isEmpty(item)
					&& (
						item instanceof Ext.form.Field
						|| item instanceof Ext.form.field.Base
						|| item instanceof Ext.form.FieldContainer
					)
					&& !item.disabled
					&& !item.isValid()
					&& !item.disableCascade // Property to disable cascade on fields
				) {
					data.push(item);
				}
			}, this);

			return data;
		},

		/**
		 * Custom implementation of setValues and reset (to catch also non Ext.form.Fields items)
		 */
		reset: function() {
			// SetValues
			this.cascade(function(item) {
				if (
					!Ext.isEmpty(item)
					&& Ext.isFunction(item.setValue)
					&& (
						item instanceof Ext.form.Field
						|| item instanceof Ext.form.field.Base
						|| item instanceof Ext.form.field.HtmlEditor
						|| item instanceof Ext.form.FieldContainer
					)
				) {
					item.setValue();
				}
			}, this);

			// Reset
			this.cascade(function(item) {
				if (
					!Ext.isEmpty(item)
					&& Ext.isFunction(item.reset)
					&& (
						item instanceof Ext.form.Field
						|| item instanceof Ext.form.field.Base
						|| item instanceof Ext.form.field.HtmlEditor
						|| item instanceof Ext.form.FieldContainer
					)
				) {
					item.reset();
				}
			}, this);
		},

		/**
		 * @param {Boolean} state
		 */
		setDisabledBottomBar: function(state) {
			var bottomToolbar = this.getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM);

			if (!Ext.isEmpty(bottomToolbar))
				Ext.Array.forEach(bottomToolbar.items.items, function(button, i, allButtons) {
					if (Ext.isFunction(button.setDisabled))
						button.setDisabled(state);
				}, this);
		},

		/**
		 * @param {Boolean} state
		 * @param {Boolean} allFields
		 * @param {Boolean} disableIsVisibleCheck
		 *
		 * @private
		 */
		setDisableFields: function(state, allFields, disableIsVisibleCheck) {
			allFields = Ext.isBoolean(allFields) ? allFields : false;
			disableIsVisibleCheck = Ext.isBoolean(disableIsVisibleCheck) ? disableIsVisibleCheck : false;

			// For Ext.form.field.Field objects
			this.getForm().getFields().each(function(item, i, length) {
				if (Ext.isFunction(item.setDisabled))
					if (state) {
						item.setDisabled(state);
					} else {
						if ((allFields || !item.cmImmutable) && item.isVisible())
							item.setDisabled(state);
					}
			}, this);

			// For extra objects (Buttons and objects with considerAsFieldToDisable property)
			this.cascade(function(item) {
				if (
					!Ext.isEmpty(item)
					&& Ext.isFunction(item.setDisabled)
					&& (
						item instanceof Ext.button.Button
						|| item.considerAsFieldToDisable
					)
				) {
					if (state) {
						item.setDisabled(state);
					} else {
						if (
							(allFields || !item.cmImmutable)
							&& (item.isVisible() || disableIsVisibleCheck)
						) {
							item.setDisabled(state);
						}
					}
				}
			}, this);
		},

		/**
		 * @param {Boolean} state
		 * @param {Boolean} allFields
		 * @param {Boolean} tBarState
		 * @param {Boolean} bBarState
		 */
		setDisabledModify: function(state, allFields, tBarState, bBarState) {
			this.setDisableFields(state, allFields);
			this.setDisabledTopBar(Ext.isBoolean(tBarState) ? tBarState : !state);
			this.setDisabledBottomBar(Ext.isBoolean(bBarState) ? bBarState : state);
		},

		/**
		 * @param {Boolean} state
		 */
		setDisabledTopBar: function(state) {
			var topToolbar = this.getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_TOP);

			if (!Ext.isEmpty(topToolbar))
				Ext.Array.forEach(topToolbar.items.items, function(button, i, allButtons) {
					if (Ext.isFunction(button.setDisabled)) {
						if (Ext.isBoolean(button.forceDisabledState)) // Force disabled state implementation
							state = button.forceDisabledState;

						button.setDisabled(state);
					}
				}, this);
		}
	});

})();