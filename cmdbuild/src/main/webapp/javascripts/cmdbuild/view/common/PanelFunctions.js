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
		 */
		getData: function(withDisabled) {
			if (withDisabled) {
				var data = {};
				this.cascade(function(item) {
					if (
						item
						&& item.submitValue
						&& (
							(item instanceof Ext.form.Field)
							|| (item instanceof Ext.form.field.Base)
							|| (item instanceof Ext.form.field.HtmlEditor)
						)
					) {
						data[item.name] = item.getValue();
					}
				});

				return data;
			} else {
				return this.getForm().getValues();
			}
		},

		getNonValidFields: function() {
			var data = [];

			this.cascade(function(item) {
				if (
					item
					&& (item instanceof Ext.form.Field)
					&& !item.disabled
					&& !item.isValid()
				) {
					data.push(item);
				}
			});

			return data;
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
					if (typeof button.setDisabled == 'function')
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
				if (typeof item.setDisabled == 'function')
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
					&& typeof item.setDisabled == 'function'
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
			this.setDisabledTopBar(tBarState || !state);
			this.setDisabledBottomBar(bBarState || state);
		},

		/**
		 * @param {Boolean} state
		 */
		setDisabledTopBar: function(state) {
			var topToolbar = this.getDockedComponent(CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP);

			if (!Ext.isEmpty(topToolbar))
				Ext.Array.forEach(topToolbar.items.items, function(button, i, allButtons) {
					if (typeof button.setDisabled == 'function')
						button.setDisabled(state);
				}, this);
		}
	});

})();