(function() {

	/**
	 * FilterChooser should be refactored but until that event this should be left here
	 *
	 * TODO: waiting for refactor (filterChooser)
	 */
	Ext.define('CMDBuild.model.group.privileges.FilterWindowAttribute', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.NONE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.READ, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.WRITE, type: 'boolean' }
		],

		getName: function() {
			return this.get(CMDBuild.core.proxy.Constants.NAME);
		},

		getPrivilege: function() {
			if (this.get(CMDBuild.core.proxy.Constants.NONE)) {
				return CMDBuild.core.proxy.Constants.NONE;
			}

			if (this.get(CMDBuild.core.proxy.Constants.READ)) {
				return CMDBuild.core.proxy.Constants.READ;
			}

			if (this.get(CMDBuild.core.proxy.Constants.WRITE)) {
				return CMDBuild.core.proxy.Constants.WRITE;
			}

			/**
			 * If no privileges are set, assume that the group could have no privilege on this attribute
			 */
			return CMDBuild.core.proxy.Constants.NONE;
		},

		setPrivilege: function(privilege) {
			var me = this;

			var setAs = {
				write: function() {
					me.set(CMDBuild.core.proxy.Constants.NONE, false);
					me.set(CMDBuild.core.proxy.Constants.READ, false);
					me.set(CMDBuild.core.proxy.Constants.WRITE, true);
				},
				read: function() {
					me.set(CMDBuild.core.proxy.Constants.NONE, false);
					me.set(CMDBuild.core.proxy.Constants.READ, true);
					me.set(CMDBuild.core.proxy.Constants.WRITE, false);
				},
				none: function() {
					me.set(CMDBuild.core.proxy.Constants.NONE, true);
					me.set(CMDBuild.core.proxy.Constants.READ, false);
					me.set(CMDBuild.core.proxy.Constants.WRITE, false);
				}
			}

			setAs[privilege]();

			this.commit();
		}
	});

})();