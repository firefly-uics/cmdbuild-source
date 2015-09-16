(function() {

	/**
	 * FilterChooser should be refactored but until that event this should be left here
	 *
	 * TODO: waiting for refactor (filterChooser)
	 */
	Ext.define('CMDBuild.model.group.privileges.FilterWindowAttribute', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NONE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.READ, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.WRITE, type: 'boolean' }
		],

		getName: function() {
			return this.get(CMDBuild.core.constants.Proxy.NAME);
		},

		getPrivilege: function() {
			if (this.get(CMDBuild.core.constants.Proxy.NONE)) {
				return CMDBuild.core.constants.Proxy.NONE;
			}

			if (this.get(CMDBuild.core.constants.Proxy.READ)) {
				return CMDBuild.core.constants.Proxy.READ;
			}

			if (this.get(CMDBuild.core.constants.Proxy.WRITE)) {
				return CMDBuild.core.constants.Proxy.WRITE;
			}

			/**
			 * If no privileges are set, assume that the group could have no privilege on this attribute
			 */
			return CMDBuild.core.constants.Proxy.NONE;
		},

		setPrivilege: function(privilege) {
			var me = this;

			var setAs = {
				write: function() {
					me.set(CMDBuild.core.constants.Proxy.NONE, false);
					me.set(CMDBuild.core.constants.Proxy.READ, false);
					me.set(CMDBuild.core.constants.Proxy.WRITE, true);
				},
				read: function() {
					me.set(CMDBuild.core.constants.Proxy.NONE, false);
					me.set(CMDBuild.core.constants.Proxy.READ, true);
					me.set(CMDBuild.core.constants.Proxy.WRITE, false);
				},
				none: function() {
					me.set(CMDBuild.core.constants.Proxy.NONE, true);
					me.set(CMDBuild.core.constants.Proxy.READ, false);
					me.set(CMDBuild.core.constants.Proxy.WRITE, false);
				}
			}

			setAs[privilege]();

			this.commit();
		}
	});

})();