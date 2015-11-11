(function() {

	/**
	 * FilterChooser should be refactored but until that event this should be left here
	 *
	 * TODO: waiting for refactor (filterChooser)
	 */
	Ext.define('CMDBuild.model.group.privileges.FilterWindowAttribute', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NONE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.READ, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.WRITE, type: 'boolean' }
		],

		getName: function() {
			return this.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
		},

		getPrivilege: function() {
			if (this.get(CMDBuild.core.proxy.CMProxyConstants.NONE)) {
				return CMDBuild.core.proxy.CMProxyConstants.NONE;
			}

			if (this.get(CMDBuild.core.proxy.CMProxyConstants.READ)) {
				return CMDBuild.core.proxy.CMProxyConstants.READ;
			}

			if (this.get(CMDBuild.core.proxy.CMProxyConstants.WRITE)) {
				return CMDBuild.core.proxy.CMProxyConstants.WRITE;
			}

			/**
			 * If no privileges are set, assume that the group could have no privilege on this attribute
			 */
			return CMDBuild.core.proxy.CMProxyConstants.NONE;
		},

		setPrivilege: function(privilege) {
			var me = this;

			var setAs = {
				write: function() {
					me.set(CMDBuild.core.proxy.CMProxyConstants.NONE, false);
					me.set(CMDBuild.core.proxy.CMProxyConstants.READ, false);
					me.set(CMDBuild.core.proxy.CMProxyConstants.WRITE, true);
				},
				read: function() {
					me.set(CMDBuild.core.proxy.CMProxyConstants.NONE, false);
					me.set(CMDBuild.core.proxy.CMProxyConstants.READ, true);
					me.set(CMDBuild.core.proxy.CMProxyConstants.WRITE, false);
				},
				none: function() {
					me.set(CMDBuild.core.proxy.CMProxyConstants.NONE, true);
					me.set(CMDBuild.core.proxy.CMProxyConstants.READ, false);
					me.set(CMDBuild.core.proxy.CMProxyConstants.WRITE, false);
				}
			}

			setAs[privilege]();

			this.commit();
		}
	});

})();