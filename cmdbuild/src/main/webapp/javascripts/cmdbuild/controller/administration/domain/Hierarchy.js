(function() {

	Ext.define('CMDBuild.controller.administration.domain.Hierarchy', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
//			'CMDBuild.core.proxy.Classes',
//			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.domain.CMModDomainController}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.domain.hierarchy.MainPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.domain.CMModDomainController} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.administration.domain.hierarchy.MainPanel', {
				delegate: this
			});
_debug('hierarchy constructor');
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onHierarchyAbortButtonClick':
					return this.onHierarchyAbortButtonClick();

				case 'onHierarchySaveButtonClick':
					return this.onHierarchySaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param {Array} disabledClasses
		 *
		 * @return {Ext.data.TreeStore} treeStore
		 */
		buildClassesStore: function(disabledClasses) { // TODO implementazione delle preconfigurazioni dei check
			var treeStore =  Ext.create('Ext.data.TreeStore', {
				fields: [ // TODO: build model
					{ name: 'enabled', type: 'boolean', defaultValue: true },
					{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
					{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' }
				],
				folderSort: true,
				root: {
					text: 'ROOT',
					expanded: true,
					children: []
				}
			});
			var root = treeStore.getRootNode();
			root.removeAll();

			// GetAllClasses data to get default translations
			CMDBuild.ServiceProxy.classes.read({
				params: {
					active: true
				},
				scope: this,
				success: function(response, options, decodedResponse) {
_debug('Classes decodedResponse', decodedResponse);
					Ext.Array.forEach(decodedResponse.classes, function(classObject, index, allClasses) {
						if (
							classObject[CMDBuild.core.proxy.CMProxyConstants.TYPE] == 'class' // Discard processes from visualization
							&& classObject[CMDBuild.core.proxy.CMProxyConstants.NAME] != 'Class' // Discard root class of all classes
						) {
							// Class main node
							var classMainNodeObject = { expandable: true, };
							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.NAME] = classObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
							classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = classObject[CMDBuild.core.proxy.CMProxyConstants.TEXT];

							root.appendChild(classMainNodeObject);
						}
					}, this);
				}
			});
_debug('treeStore root', root);
			return treeStore;
		},

		/**
		 * @return {CMDBuild.view.administration.domain.hierarchy.MainPanel}
		 */
		getView: function() {
			return this.view;
		},

		onHierarchyAbortButtonClick: function() { // TODO rilettura delle classi disabilitate e configurazione dello store
_debug('onHierarchyAbortButtonClick');
		},

		onHierarchySaveButtonClick: function() {
			var originDisabledClasses = [];
			var destinationDisabledClasses = [];

			// Get origin disabled classes
			this.view.originTree.getStore().getRootNode().eachChild(function(childNode) {
				if (!childNode.get('enabled'))
					originDisabledClasses.push(childNode.get(CMDBuild.core.proxy.CMProxyConstants.NAME));
			}, this);

			// Get destination disabled classes
			this.view.destinationTree.getStore().getRootNode().eachChild(function(childNode) {
				if (!childNode.get('enabled'))
					destinationDisabledClasses.push(childNode.get(CMDBuild.core.proxy.CMProxyConstants.NAME));
			}, this);
_debug('onHierarchySaveButtonClick', [originDisabledClasses, destinationDisabledClasses]);
			// TODO on success
//			this.onHierarchyAbortButtonClick();
		},

		onViewOnFront: function() {
			this.view.wrapper.removeAll();

			this.view.fillWrapper();
		}
	});

})();
