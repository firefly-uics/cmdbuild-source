(function() {

	/**
	 * Adapter class to FilterChooser structure
	 *
	 * TODO: waiting for FilterChooser refactor
	 */
	Ext.define('CMDBuild.view.administration.group.privileges.filterWindow.FilterWindow', {
		extend: 'CMDBuild.view.common.field.CMFilterChooserWindow',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.model.group.privileges.FilterWindowAttribute'
		],

		/**
		 * Model for selected group where want to set privileges
		 *
		 * @cfg {Object}
		 */
		group: undefined,

		/**
		 * @cfg {CMDBuild.model.CMFilterModel}
		 */
		filter: undefined,

		/**
		 * Array of attributes objects
		 *
		 * @cfg {Array}
		 */
		attributes: [],

		/**
		 * Target class name
		 *
		 * @cfg {String}
		 */
		className: undefined,

		filterTabToEnable: {
			attributeTab: true,
			relationTab: false,
			functionTab: true
		},

		/**
		 * @cfg {String}
		 */
		saveButtonText: CMDBuild.Translation.common.buttons.save,

		layout: 'fit',

		/**
		 * @override
		 */
		buildItems: function() {
			this.callParent(arguments);

			var data = [];
			var attributePrivileges = {};

			Ext.Array.forEach(this.group.get(CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTES_PRIVILEGES), function(privilege, i, allPrivileges) { // String to object conversion
				var parts = privilege.split(':');

				if (parts.length == 2)
					attributePrivileges[parts[0]] = parts[1];
			}, this);

			Ext.Array.forEach(this.attributes, function(classAttribute, i, allClassAttributes) {
				if (classAttribute.name != 'Notes') { // As usual, the notes attribute is managed in a special way
					var attributeConf = {};
					attributeConf[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = classAttribute[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION];
					attributeConf[CMDBuild.core.proxy.CMProxyConstants.NAME] = classAttribute[CMDBuild.core.proxy.CMProxyConstants.NAME];
					attributeConf[CMDBuild.core.proxy.CMProxyConstants.NONE] = false;
					attributeConf[CMDBuild.core.proxy.CMProxyConstants.READ] = false;
					attributeConf[CMDBuild.core.proxy.CMProxyConstants.WRITE] = false;

					var privilege = attributePrivileges[classAttribute[CMDBuild.core.proxy.CMProxyConstants.NAME]];
					if (!Ext.isEmpty(privilege) && Ext.isString(privilege))
						attributeConf[privilege] = true;

					data.push(attributeConf);
				}
			}, this);

			Ext.apply(this, {
				items: [
					Ext.create('Ext.tab.Panel', {
						border: false,
						frame: false,

						items: [
							this.rowPrivilegePanel = Ext.create('Ext.panel.Panel', {
								title: CMDBuild.Translation.rowsPrivileges,
								layout: 'border',
								border: false,
								frame: false,

								items: this.items
							}),
							this.columnPrivilegeGrid = Ext.create('CMDBuild.view.administration.group.privileges.filterWindow.GridPanel', {
								store: Ext.create('Ext.data.Store', {
									model: 'CMDBuild.model.group.privileges.FilterWindowAttribute',
									data: data
								})
							})
						]
					})
				]
			});
		},

		/**
		 * The convention is to send to server an array of string.
		 * Each string has the template:
		 *
		 * 	attributeName:mode
		 *
		 * mode = none | read | write
		 *
		 * @returns {Array}
		 */
		getAttributePrivileges: function() {
			var out = [];

			this.columnPrivilegeGrid.getStore().each(function(record) {
				out.push(record.getName() + ':' + record.getPrivilege());
			});

			return out;
		},

		/**
		 * @override
		 */
		setWindowTitle: function() {
			this.title = CMDBuild.Translation.rowAndColumnPrivileges;
		}
	});

})();