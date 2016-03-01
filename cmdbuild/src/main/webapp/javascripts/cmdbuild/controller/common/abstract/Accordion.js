(function() {

	// External implementation to avoid overrides
	Ext.require(['CMDBuild.core.constants.Proxy']);

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.common.abstract.Accordion', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'accordionBuildId',
			'accordionDeselect',
			'accordionExpand',
			'accordionFirtsSelectableNodeGet',
			'accordionIdentifierGet',
			'accordionNodeByIdGet',
			'accordionSelectFirstSelectableNode',
			'accordionSelectNodeById',
			'accordionUpdateStore',
			'onAccordionBeforeSelect',
			'onAccordionExpand',
			'onAccordionSelectionChange'
		],

		/**
		 * Flag to disable next selection, will be reset on next store update
		 *
		 * @cfg {Boolean}
		 */
		disableSelection: false,

		/**
		 * Flag to disable next storeLoad, will be reset on next expand
		 *
		 * @cfg {Boolean}
		 */
		disableStoreLoad: false,

		/**
		 * @cfg {Boolean}
		 */
		hideIfEmpty: false,

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.model.common.accordion.Generic}
		 */
		lastSelection: undefined,

		/**
		 * @property {Object}
		 */
		view: undefined,

		/**
		 * Method must be overrided with view construction
		 *
		 * @param {Object} configurationObject
		 *
		 * @override
		 * @abstract
		 */

		/**
		 * Generates an unique id for the menu accordion, prepend to components array "accordion" string and identifier.
		 *
		 * @param {Object} parameters
		 * @param {Array} parameters.components
		 * @param {String} parameters.name
		 *
		 * @return {String}
		 */
		accordionBuildId: function(parameters) {
			if (
				Ext.isObject(parameters)
				&& !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.COMPONENTS])
			) {
				var components = parameters[CMDBuild.core.constants.Proxy.COMPONENTS];
				components = Ext.isArray(components) ? Ext.Array.clean(components) : [components];

				// Custom identifier management
				if (
					!Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.NAME])
					&& Ext.isString(parameters[CMDBuild.core.constants.Proxy.NAME])
				) {
					components.unshift(CMDBuild.core.constants.Proxy.ACCORDION, parameters[CMDBuild.core.constants.Proxy.NAME]);
				} else {
					components.unshift(CMDBuild.core.constants.Proxy.ACCORDION, this.cmfg('accordionIdentifierGet'));
				}

				Ext.Array.forEach(components, function(component, i, allComponents) {
					components[i] = Ext.String.trim(String(component));
				}, this);

				return components.join('-');
			}

			return CMDBuild.core.constants.Proxy.ACCORDION + '-' + this.cmfg('accordionIdentifierGet') + '-' + Date.now();
		},

		accordionDeselect: function() {
			this.view.getSelectionModel().deselectAll();

			this.cmfg('onAccordionSelectionChange');
		},

		accordionExpand: function() {
			if (!Ext.isEmpty(this.view))
				this.view.expand();
		},

		/**
		 * @returns {CMDBuild.model.common.accordion.Generic} node or null
		 */
		accordionFirtsSelectableNodeGet: function() {
			var node = null;

			if (!this.view.isDisabled()) {
				var inspectedNode = this.view.getRootNode();

				while (inspectedNode) {
					if (this.isNodeSelectable(inspectedNode)) {
						node = inspectedNode;

						break;
					} else {
						inspectedNode = inspectedNode.firstChild;
					}
				}
			}

			return node;
		},

		/**
		 * @returns {String or null}
		 */
		accordionIdentifierGet: function() {
			if (!Ext.isEmpty(this.identifier))
				return this.identifier;

			return null;
		},

		/**
		 * Search in entityId and id parameter
		 *
		 * @param {Number or String} id
		 *
		 * @returns {CMDBuild.model.common.accordion.Generic}
		 */
		accordionNodeByIdGet: function(id) {
			return (
				this.view.getStore().getRootNode().findChild(CMDBuild.core.constants.Proxy.ID, id, true)
				|| this.view.getStore().getRootNode().findChild(CMDBuild.core.constants.Proxy.ENTITY_ID, id, true)
			);
		},

		accordionSelectFirstSelectableNode: function() {
			var firstSelectableNode = this.cmfg('accordionFirtsSelectableNodeGet');

			if (!Ext.isEmpty(firstSelectableNode)) {
				this.cmfg('accordionExpand');
				this.cmfg('accordionSelectNodeById', firstSelectableNode.get(CMDBuild.core.constants.Proxy.ID));
			}
		},

		/**
		 * @param {Number or String} id
		 */
		accordionSelectNodeById: function(id) {
			if (!Ext.isEmpty(id)) {
				var node = this.cmfg('accordionNodeByIdGet', id);

				if (!Ext.isEmpty(node)) {
					// Expand fail if the accordion is not visible. I can't know when accordion's parent will be visible, so skip only the expand to avoid to fail
					if (this.view.isVisible(true))
						node.bubble(function() {
							this.expand();
						});

					this.view.getSelectionModel().select(node);
				} else {
					this.cmfg('accordionSelectFirstSelectableNode');
				}
			}
		},

		/**
		 * @param {Number or String} nodeIdToSelect
		 *
		 * @abstract
		 */
		accordionUpdateStore: Ext.emptyFn,

		/**
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isEmpty: function() {
			return !this.view.getStore().getRootNode().hasChildNodes();
		},

		/**
		 * @param {CMDBuild.model.common.accordion.Generic} node
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isNodeSelectable: function(node) {
			return (
				!node.isRoot() // Root is hidden by default
				&& !Ext.isEmpty(node.get(CMDBuild.core.constants.Proxy.ID)) // Node without id property are not selectable
			);
		},

		/**
		 * @param {CMDBuild.model.common.accordion.Generic} node
		 *
		 * @returns {Boolean}
		 */
		onAccordionBeforeSelect: function(node) {
			return this.isNodeSelectable(node);
		},

		onAccordionExpand: function() {
			this.cmfg('mainViewportModuleShow', { identifier: this.cmfg('accordionIdentifierGet') });

			// Update store
			if (!this.disableStoreLoad) {
				if (this.view.getSelectionModel().hasSelection()) {
					var selection = this.view.getSelectionModel().getSelection()[0];

					this.cmfg('accordionDeselect');
					this.cmfg('accordionUpdateStore', selection.get(CMDBuild.core.constants.Proxy.ENTITY_ID));
				} else {
					this.cmfg('accordionUpdateStore');
				}
			}

			// DisableStoreLoad flag reset
			this.disableStoreLoad = false;
		},

		onAccordionSelectionChange: function() {
			if (this.view.getSelectionModel().hasSelection()) {
				var selection = this.view.getSelectionModel().getSelection()[0];

				if (
					!this.cmfg('mainViewportModuleShow', {
						identifier: selection.get('cmName'),
						parameters: selection
					})
				) {
					// If the panel was not brought to front (report from the navigation menu), select the previous node or deselect the tree
					if (!Ext.isEmpty(this.lastSelection)) {
						this.view.getSelectionModel().select(this.lastSelection);
					} else {
						this.view.getSelectionModel().deselectAll(true);
					}
				} else {
					this.lastSelection = selection;
				};
			}
		},

		/**
		 * @param {Number or String} nodeIdToSelect
		 *
		 * @private
		 */
		updateStoreCommonEndpoint: function(nodeIdToSelect) {
			if (!this.disableSelection) {
				if (!Ext.isEmpty(nodeIdToSelect))
					this.cmfg('accordionSelectNodeById', nodeIdToSelect);

				// Select first selectable item if no selection and expanded
				if (!this.view.getSelectionModel().hasSelection() && this.view.getCollapsed() === false)
					this.cmfg('accordionSelectFirstSelectableNode');
			}

			// Accordion store update end event fire
			this.view.fireEvent('storeload');

			// Hide if accordion is empty
			if (this.hideIfEmpty && this.isEmpty())
				this.hide();

			// DisableSelection flag reset
			this.disableSelection = false;
		}
	});

})();