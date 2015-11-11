(function() {

	/**
	 * New class than will replace CMFormFunctions
	 */
	Ext.define('CMDBuild.view.common.PanelFunctions', {

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @param {Boolean} disableTBar
		 */
		disableModify: function(disableTBar) {
			this.setDisableFields(true);
			this.setDisabledTopBar(disableTBar);
			this.setDisabledBottomBar(true);
		},

		/**
		 * @param {Boolean} allFields
		 */
		enableModify: function(allFields) {
			this.setDisableFields(false, allFields);
			this.setDisabledTopBar(true);
			this.setDisabledBottomBar(false);
		},

		/**
		 * @param {Boolean} withDisabled
		 *
		 * @returns {Array}
		 */
		getData: function(withDisabled) {
			if (withDisabled) {
				var data = {};

				this.cascade(function(item) {
					if (
						!Ext.isEmpty(item)
						&& this.isManagedField(item)
					) {
						data[item.name] = item.getValue();
					}
				}, this);

				return data;
			}

			return this.getForm().getValues();
		},

		/**
		 * @returns {Array} nonValidFields
		 */
		getNonValidFields: function() {
			var nonValidFields = [];

			this.cascade(function(item) {
				if (
					!Ext.isEmpty(item)
					&& item instanceof Ext.form.Field
					&& !item.isDisabled()
					&& !item.isHidden()
					&& !item.isValid()
				) {
					nonValidFields.push(item);
				}
			}, this);

			return nonValidFields;
		},

		/**
		 * @param {Object} field
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isManagedField: function(field) {
			return (
				field instanceof Ext.form.Field
				|| field instanceof Ext.form.field.Base
				|| field instanceof Ext.form.field.HtmlEditor
				|| field instanceof Ext.form.FieldContainer
			);
		},

		reset: function() {
			this.getForm().setValues();
			this.getForm().reset();
		},

		/**
		 * @param {Boolean} state
		 */
		setDisabledBottomBar: function(state) {
			var bottomToolbar = this.getDockedComponent(CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM);

			if (!Ext.isEmpty(bottomToolbar))
				Ext.Array.forEach(bottomToolbar.items.items, function(button, i, allButtons) {
					if (Ext.isFunction(button.setDisabled))
						button.setDisabled(state);
				}, this);
		},

		/**
		 * @param {Boolean} state
		 * @param {Boolean} allFields
		 *
		 * @private
		 */
		setDisableFields: function(state, allFields) {
			allFields = allFields || false;

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
						if ((allFields || !item.cmImmutable) && item.isVisible())
							item.setDisabled(state);
					}
				}
			});
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
			var topToolbar = this.getDockedComponent(CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP);

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