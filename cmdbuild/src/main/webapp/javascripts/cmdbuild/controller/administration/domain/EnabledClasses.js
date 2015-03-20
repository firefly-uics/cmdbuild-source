(function() {

	Ext.define('CMDBuild.controller.administration.domain.EnabledClasses', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.model.Classes'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.domain.CMModDomainController}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.domain.enabledClasses.MainPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.domain.enabledClasses.MainPanel', {
				delegate: this
			});
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
				case 'onEnabledClassesAbortButtonClick':
					return this.onEnabledClassesAbortButtonClick();

				case 'onEnabledClassesSaveButtonClick':
					return this.onEnabledClassesSaveButtonClick();

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
		buildClassesStore: function(disabledClasses) {
			var treeStore =  Ext.create('Ext.data.TreeStore', {
				model: 'CMDBuild.model.Classes.domainsTreePanel',
				root: {
					text: 'ROOT',
					expanded: true,
					children: []
				}
			});
			var root = treeStore.getRootNode();
			var rootChildren = [];
			root.removeAll();

			// GetAllClasses data to get default translations
			CMDBuild.ServiceProxy.classes.read({
				params: {
					active: true
				},
				scope: this,
				success: function(response, options, decodedResponse) {
					var nodesMap = {};
_debug('decodedResponse', decodedResponse);
					Ext.Array.forEach(decodedResponse.classes, function(classObject, index, allClasses) {
						if (
							classObject[CMDBuild.core.proxy.CMProxyConstants.TYPE] == 'class' // Discard processes from visualization
							&& classObject[CMDBuild.core.proxy.CMProxyConstants.NAME] != 'Class' // Discard root class of all classes
						) {
//							if (Ext.isEmpty(classObject.raw.system)) {
								// Class main node
								var classMainNodeObject = { expandable: false, };
								classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.LEAF] = true;
								classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.NAME] = classObject[CMDBuild.core.proxy.CMProxyConstants.NAME];
								classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = classObject[CMDBuild.core.proxy.CMProxyConstants.TEXT];
								classMainNodeObject[CMDBuild.core.proxy.CMProxyConstants.ENABLED] = !Ext.Array.contains(disabledClasses, classObject[CMDBuild.core.proxy.CMProxyConstants.NAME]);


//								classMainNodeObject['parent'] = classObject["parent"];
//								classMainNodeObject['iconCls'] = classObject["superclass"] ? "cmdbuild-tree-superclass-icon" : "cmdbuild-tree-class-icon";

								rootChildren.push(classMainNodeObject);
//								var nodeConf =  buildNodeConf(classes[key]);

//								nodesMap[nodeConf.id] = nodeConf;
//							}
						}
					}, this);
				},
				callback: function(records, operation, success) {
					// TODO: use server ordered calls when will be implemented
					root.appendChild(
						Ext.Array.sort(rootChildren, function(a, b) {
							if(a[CMDBuild.core.proxy.CMProxyConstants.NAME] < b[CMDBuild.core.proxy.CMProxyConstants.NAME]) return -1;

							if(a[CMDBuild.core.proxy.CMProxyConstants.NAME] > b[CMDBuild.core.proxy.CMProxyConstants.NAME]) return 1;

							return 0;
						})
					);
				}
			});

			return treeStore;
		},

		/**
		 * @return {CMDBuild.cache.CMDomainModel} or null
		 */
		getSelectedDomain: function() {
			if (Ext.isEmpty(this.parentDelegate.selectedDomain))
				return null;

			return this.parentDelegate.selectedDomain;
		},

		/**
		 * @return {CMDBuild.view.administration.domain.enabledClasses.MainPanel}
		 */
		getView: function() {
			return this.view;
		},

		onDomainSelected: function() {
			var bottomToolbar = this.view.getDockedComponent(CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM);

			this.view.buildTrees();

			this.view.originTree.setDisabled(true);
			this.view.destinationTree.setDisabled(true);

			Ext.Array.forEach(bottomToolbar.items.items, function(button, i, allButtons) {
				button.setDisabled(true);
			}, this);
		},

		onEnabledClassesAbortButtonClick: function() {
			this.view.buildTrees();
		},

		onEnabledClassesSaveButtonClick: function() {
			var originDisabledClasses = [];
			var destinationDisabledClasses = [];

			// Get origin disabled classes
			this.view.originTree.getStore().getRootNode().eachChild(function(childNode) {
				if (!childNode.get(CMDBuild.core.proxy.CMProxyConstants.ENABLED))
					originDisabledClasses.push(childNode.get(CMDBuild.core.proxy.CMProxyConstants.NAME));
			}, this);

			// Get destination disabled classes
			this.view.destinationTree.getStore().getRootNode().eachChild(function(childNode) {
				if (!childNode.get(CMDBuild.core.proxy.CMProxyConstants.ENABLED))
					destinationDisabledClasses.push(childNode.get(CMDBuild.core.proxy.CMProxyConstants.NAME));
			}, this);

			var invalidFields = this.parentDelegate.view.domainForm.getNonValidFields();

			if (invalidFields.length == 0) {
				CMDBuild.LoadMask.get().show();
				var withDisabled = true;
				var data = this.parentDelegate.view.domainForm.getData(withDisabled);
				if (this.parentDelegate.formController.currentDomain == null) {
					data.id = -1;
				} else {
					data.id = this.parentDelegate.formController.currentDomain.get(CMDBuild.core.proxy.CMProxyConstants.ID);

					data.disabled1 = Ext.encode(originDisabledClasses); // TODO proxy constants
					data.disabled2 = Ext.encode(destinationDisabledClasses); // TODO proxy constants
				}

				CMDBuild.ServiceProxy.administration.domain.save({
					params: data,
					scope: this,
					success: function(req, res, decoded) {
						this.parentDelegate.view.domainForm.disableModify();
						_CMCache.onDomainSaved(decoded.domain);
						_CMCache.flushTranslationsToSave(decoded.domain.name);
					},
					callback: function() {
						CMDBuild.LoadMask.get().hide();
					}
				});

			} else {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
			}
		},

		onModifyButtonClick: function() {
			var bottomToolbar = this.view.getDockedComponent(CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM);

			this.view.originTree.setDisabled(false);
			this.view.destinationTree.setDisabled(false);

			Ext.Array.forEach(bottomToolbar.items.items, function(button, i, allButtons) {
				button.setDisabled(false);
			}, this);
		}
	});

})();