(function() {

	Ext.define('CMDBuild.controller.common.AbstractAccordionController', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'accordionBuildId',
			'onAccordionBeforeSelect',
			'onAccordionDeselect',
			'onAccordionExpand',
			'onAccordionGetFirtsSelectableNode',
			'onAccordionGetNodeById',
			'onAccordionIsEmpty',
			'onAccordionIsNodeSelectable',
			'onAccordionSelectFirstSelectableNode',
			'onAccordionSelectionChange',
			'onAccordionSelectNodeById',
			'onAccordionUpdateStore'
		],

		/**
		 * @property {CMDBuild.model.common.accordion.Generic}
		 */
		lastSelection: undefined,

		/**
		 * @property {Object}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Object} configurationObject.view
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			// Shorthands
			this.cmName = this.view.cmName;
		},

		/**
		 * Generates an unique id for the menu accordion, prepend to components array "accordion" string and cmName.
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

				// Custom cmName management
				if (
					!Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.NAME])
					&& Ext.isString(parameters[CMDBuild.core.constants.Proxy.NAME])
				) {
					components.unshift(CMDBuild.core.constants.Proxy.ACCORDION, parameters[CMDBuild.core.constants.Proxy.NAME]);
				} else {
					components.unshift(CMDBuild.core.constants.Proxy.ACCORDION, this.cmName);
				}

				Ext.Array.forEach(components, function(component, i, allComponents) {
					components[i] = Ext.String.trim(String(component));
				}, this);

				return components.join('-');
			}

			return CMDBuild.core.constants.Proxy.ACCORDION + '-' + this.cmName + '-' + Date.now();
		},

		/**
		 * @param {CMDBuild.model.common.accordion.Generic} node
		 *
		 * @returns {Boolean}
		 */
		onAccordionBeforeSelect: function(node) {
			return this.cmfg('onAccordionIsNodeSelectable', node);
		},

		onAccordionDeselect: function() {
			this.cmfg('onAccordionSelectionChange');
			this.view.getSelectionModel().deselectAll();
		},

		onAccordionExpand: function() {
			_CMMainViewportController.bringTofrontPanelByCmName(this.view.cmName);

			// Reselect selected or select first leaf
			if (this.view.getSelectionModel().hasSelection()) {
				this.cmfg('onAccordionSelectionChange');
			} else {
				this.cmfg('onAccordionSelectFirstSelectableNode');
			}
		},

		/**
		 * @returns {CMDBuild.model.common.accordion.Generic} node or null
		 */
		onAccordionGetFirtsSelectableNode: function() {
			var node = null;

			if (!this.view.isDisabled()) {
				var inspectedNode = this.view.getRootNode();

				while (inspectedNode) {
					if (this.cmfg('onAccordionIsNodeSelectable', inspectedNode)) {
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
		 * Search in entityId and id parameter
		 *
		 * @param {Number or String} id
		 *
		 * @returns {CMDBuild.model.common.accordion.Generic}
		 */
		onAccordionGetNodeById: function(id) {
			return (
				this.view.getStore().getRootNode().findChild(CMDBuild.core.constants.Proxy.ID, id, true)
				|| this.view.getStore().getRootNode().findChild(CMDBuild.core.constants.Proxy.ENTITY_ID, id, true)
			);
		},

		/**
		 * @returns {Boolean}
		 */
		onAccordionIsEmpty: function() {
			return !this.view.getStore().getRootNode().hasChildNodes();
		},

		/**
		 * @param {CMDBuild.model.common.accordion.Generic} node
		 *
		 * @returns {Boolean}
		 */
		onAccordionIsNodeSelectable: function(node) {
			return (
				!node.isRoot() // Root is hidden by default
				&& !Ext.isEmpty(node.get(CMDBuild.core.constants.Proxy.ID)) // Node without id property are not selectable
			);
		},

		onAccordionSelectFirstSelectableNode: function() {
			var firstSelectableNode = this.cmfg('onAccordionGetFirtsSelectableNode');

			if (!Ext.isEmpty(firstSelectableNode)) {
				this.view.expand();

				this.cmfg('onAccordionSelectNodeById', firstSelectableNode.get(CMDBuild.core.constants.Proxy.ID));
			}
		},

		onAccordionSelectionChange: function() {
			if (this.view.getSelectionModel().hasSelection()) {
				var selection = this.view.getSelectionModel().getSelection()[0];

				if (_CMMainViewportController.bringTofrontPanelByCmName(selection.get('cmName'), selection) === false) {
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
		 * @param {Number or String} id
		 */
		onAccordionSelectNodeById: function(id) {
			if (!Ext.isEmpty(id)) {
				var node = this.cmfg('onAccordionGetNodeById', id);

				if (!Ext.isEmpty(node)) {
					// Expand fail if the accordion is not visible. I can't know when accordion's parent will be visible, so skip only the expand to avoid to fail
					if (this.view.isVisible(true))
						node.bubble(function() {
							this.expand();
						});

					this.view.getSelectionModel().select(node);
				} else {
					_warning('cannot find node with id "' + id + '"', this);
				}
			}
		},

		/**
		 * @param {Number or String} nodeIdToSelect
		 */
		onAccordionUpdateStore: function(nodeIdToSelect) {
			if (!Ext.isEmpty(nodeIdToSelect))
				this.cmfg('onAccordionSelectNodeById', this.cmfg('accordionBuildId', nodeIdToSelect));

			// Select first selectable item if no selection and expanded
			if (!this.view.getSelectionModel().hasSelection() && this.view.getCollapsed() === false)
				this.cmfg('onAccordionSelectFirstSelectableNode');

			// Hide if accordion is empty
			if (this.hideIfEmpty && this.cmfg('onAccordionIsEmpty'))
				this.hide();
		}
	});

})();