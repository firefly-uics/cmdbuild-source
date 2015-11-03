(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyRelations');

	Ext.define('CMDBuild.controller.management.classes.CMCardRelationsController', {
		extend: 'CMDBuild.controller.management.classes.CMModCardSubController',

		/**
		 * @param {CMDBuild.view.management.classes.CMCardRelationsPanel} view
		 * @param {CMDBuild.controller.management.classes.CMModCardController} superController
		 *
		 * @override
		 */
		constructor: function(view, superController) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.callBacks = {
				'action-relation-go': this.onFollowRelationClick,
				'action-relation-edit': this.onEditRelationClick,
				'action-relation-delete': this.onDeleteRelationClick,
				'action-relation-editcard': this.onEditCardClick,
				'action-relation-viewcard': this.onViewCardClick,
				'action-relation-attach': this.onOpenAttachmentClick
			};

			this.view.store.getRootNode().on('append', function(root, newNode) {
				// The nodes with depth == 1 are the folders
				if (newNode.get('depth') == 1)
					newNode.on('expand', onDomainNodeExpand, this, {single: true});
			}, this);

			this.mon(this.view, this.view.CMEVENTS.openGraphClick, this.onShowGraphClick, this);
			this.mon(this.view, this.view.CMEVENTS.addButtonClick, this.onAddRelationButtonClick, this);
			this.mon(this.view, 'beforeitemclick', cellclickHandler, this);
			this.mon(this.view, 'itemdblclick', onItemDoubleclick, this);
			this.mon(this.view, 'activate', this.loadData, this);

			this.CMEVENTS = { serverOperationSuccess: 'cm-server-success' };

			this.addEvents(this.CMEVENTS.serverOperationSuccess);
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 *
		 * @override
		 */
		onEntryTypeSelected: function(entryType) {
			this.callParent(arguments);

			this.card = null;

			// Set empty entryType on simple classes
			if (Ext.isEmpty(this.entryType) || this.entryType.get(CMDBuild.core.proxy.CMProxyConstants.TABLE_TYPE) == 'simpletable')
				this.entryType = null;

			this.view.disable();
			this.view.clearStore();
		},

		/**
		 * @param {Object} entryType - card model
		 *
		 * @override
		 */
		onCardSelected: function(card) {
			this.callParent(arguments);

			this.view.clearStore();
			this.view.disable();

			if (!Ext.isEmpty(card) && !Ext.isEmpty(this.entryType)) { // Don't enable tab on simple classes
				this.updateCurrentClass(card);

				this.loadData();

				this.view.enable();
			}
		},

		/**
		 * @param {Object} entryType - card model
		 */
		updateCurrentClass: function(card) {
			var classId = card.get('IdClass');
			var currentClass = _CMCache.getEntryTypeById(classId);

			if (this.currentClass != currentClass) {
				if (!currentClass || currentClass.get(CMDBuild.core.proxy.CMProxyConstants.TABLE_TYPE) == 'simpletable')
					currentClass = null;

				this.currentClass = currentClass;
				this.view.addRelationButton.setDomainsForEntryType(currentClass);
			}
		},

		/**
		 * Function to load data to treePanel and edit addRelation button to avoid to violate domains cardinality
		 */
		loadData: function() {
			if (this.card != null && tabIsActive(this.view)) {
				var me = this;
				var el = this.view.getEl();

				if (el)
					el.mask();

				var parameters = {};
				parameters[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = this.getCardId();
				parameters[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.getClassId());
				parameters[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_LIMIT] = CMDBuild.Config.cmdbuild.relationlimit;

				CMDBuild.core.proxy.CMProxyRelations.getList({
					params: parameters,
					scope: this,
					success: function(result, options, decodedResult) {
						el.unmask();

						this.view.fillWithData(decodedResult.domains);

						// AddRelation button update
							var toDisableButtons = [];

							// Domains relation cardinality check
							Ext.Array.forEach(decodedResult.domains, function(item, index, allItems) {
								var domainObjext = _CMCache.getDomainById(item[CMDBuild.core.proxy.CMProxyConstants.ID]);

								if ( // Checks when disable add buttons ...
									item[CMDBuild.core.proxy.CMProxyConstants.RELATIONS_SIZE] == 1 // ... relation size equals 1 ...
									&& ( // ... and i'm on N side of domain (so i have only one target) ...
										(
											domainObjext.get(CMDBuild.core.proxy.CMProxyConstants.CARDINALITY) == 'N:1'
											&& item[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_SOURCE] == '_1'
										)
										|| (
											domainObjext.get(CMDBuild.core.proxy.CMProxyConstants.CARDINALITY) == '1:N'
											&& item[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_SOURCE] == '_2'
										)
										|| domainObjext.get(CMDBuild.core.proxy.CMProxyConstants.CARDINALITY) == '1:1' // ... or i'm on 1:1 relation
									)
								) {
									toDisableButtons.push(domainObjext.get(CMDBuild.core.proxy.CMProxyConstants.ID));
								}
							}, this);

							// Loop trough split button menu items to modify handler if relation is full
							Ext.Array.forEach(this.view.addRelationButton.menu.items.items, function(item, index, allItems) {
								if (Ext.Array.contains(toDisableButtons, item.domain.dom_id)) { // Overwrites button handler to display error popup
									item.setHandler(function() {
										CMDBuild.Msg.error(
											CMDBuild.Translation.common.failure,
											CMDBuild.Translation.errors.domainCardinalityViolation,
											false
										);
									});
								} else { // Setup standard handler for button
									item.setHandler(function(item, e) {
										me.view.addRelationButton.fireEvent('cmClick', item.domain);
									});
								}
							}, this);
						// END: AddRelation button update
					}
				});
			}
		},

		/**
		 * @return {Int} cardId
		 */
		getCardId: function() {
			return this.card.get('Id');
		},

		/**
		 * @return {Int} classId
		 */
		getClassId: function() {
			return this.card.get('IdClass');
		},

		/**
		 * @param {CMDBuild.model.classes.CMRelationPanelModel} model (CMRelationPanelModel)
		 */
		onFollowRelationClick: function(model) {
			if (model.get('depth') > 1)
				_CMMainViewportController.openCard({
					Id: model.get('dst_id'),
					IdClass: model.get('dst_cid')
				});
		},

		/**
		 * AddRelation click function to open CMEditRelationWindow filtered excluding already related cards based on relation type
		 *
		 * @param {Object} model - relation grid model
		 */
		onAddRelationButtonClick: function(model) {
			var me = this;
			var masterAndSlave = getMasterAndSlave(model.src);
			var domain = _CMCache.getDomainById(model.dom_id);
			var classData = _CMCache.getClassById(model.dst_cid);
			var isMany = false;
			var destination = model[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_SOURCE] == '_1' ? '_2' : '_1'; // Probably tells in witch direction of relation you are looking at

			if (domain)
				isMany = domain.isMany(destination);

			var editRelationWindow = Ext.create('CMDBuild.view.management.classes.relations.CMEditRelationWindow', {
				domain: domain,
				classObject: classData,
				sourceCard: this.card,
				relation: {
					dst_cid: model.dst_cid,
					dom_id: model.dom_id,
					rel_id: -1,
					masterSide: masterAndSlave.masterSide,
					slaveSide: masterAndSlave.slaveSide
				},
				selModel: Ext.create('CMDBuild.selection.CMMultiPageSelectionModel', {
					mode: isMany ? 'MULTI' : 'SINGLE',
					avoidCheckerHeader: true,
					idProperty: 'Id' // required to identify the records for the data and not the id of ext
				}),
				successCb: function() {
					me.onAddRelationSuccess();
				}
			});

			this.mon(editRelationWindow, 'destroy', function() {
				this.loadData();
			}, this, { single: true });

			editRelationWindow.show();

			editRelationWindow.grid.getStore().load({
				scope: this,
				callback: function(records, operation, success) {
					Ext.Function.createDelayed(function() { // HACK to wait store to be correctly loaded
						var parameters = {};
						var cardsIdArray = [];

						editRelationWindow.grid.getStore().each(function(record) {
							cardsIdArray.push(record.get(CMDBuild.core.proxy.CMProxyConstants.ID));
						});

						parameters[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_NAME] = domain.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
						parameters[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = classData.get(CMDBuild.core.proxy.CMProxyConstants.NAME);
						parameters[CMDBuild.core.proxy.CMProxyConstants.CARDS] = Ext.encode(cardsIdArray);
						parameters[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_DIRECTION] = destination;

						CMDBuild.core.proxy.CMProxyRelations.getAlreadyRelatedCards({
							params: parameters,
							scope: this,
							success: function(result, options, decodedResult) {
								var alreadyRelatedCardsIds = [];

								// Create ids array to use as filter
								Ext.Array.forEach(decodedResult.response, function(alreadyRelatedItem, index, allItems) {
									if (alreadyRelatedItem[CMDBuild.core.proxy.CMProxyConstants.ID]) {
										var parameters = {};
										parameters[CMDBuild.core.proxy.CMProxyConstants.CARD_ID] = alreadyRelatedItem[CMDBuild.core.proxy.CMProxyConstants.ID];
										parameters[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = alreadyRelatedItem[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME];
										parameters[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_LIMIT] = CMDBuild.Config.cmdbuild.relationlimit;

										// Get all domains of grid-card to check if it have relation with current-card
										CMDBuild.core.proxy.CMProxyRelations.getList({
											params: parameters,
											scope: this,
											success: function(result, options, decodedResult) {
												// Loop through domains array
												Ext.Array.forEach(decodedResult[CMDBuild.core.proxy.CMProxyConstants.DOMAINS], function(domainItem, index, allItems) {
													if (domainItem[CMDBuild.core.proxy.CMProxyConstants.ID] == domain.get(CMDBuild.core.proxy.CMProxyConstants.ID)) {
														// Loop through domain (witch i'm creating a relation) relations array
														Ext.Array.forEach(domainItem[CMDBuild.core.proxy.CMProxyConstants.RELATIONS], function(domainRelationItem, index, allItems) {
															if (!Ext.Object.isEmpty(classData)) {
																if (domain.get(CMDBuild.core.proxy.CMProxyConstants.CARDINALITY) == '1:1') {
																	alreadyRelatedCardsIds.push(options.params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID]);
																} else if (
																	(
																		domain.get(CMDBuild.core.proxy.CMProxyConstants.CARDINALITY) == '1:N'
																		&& model[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_SOURCE] == '_1' // Direct
																	)
																	|| (
																		domain.get(CMDBuild.core.proxy.CMProxyConstants.CARDINALITY) == 'N:1'
																		&& model[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_SOURCE] == '_2' // Inverse
																	)
																) {
																	alreadyRelatedCardsIds.push(options.params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID]);
																} else if (
																	(
																		domain.get(CMDBuild.core.proxy.CMProxyConstants.CARDINALITY) == 'N:1'
																		&& model[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_SOURCE] == '_1' // Direct
																	)
																	|| (
																		domain.get(CMDBuild.core.proxy.CMProxyConstants.CARDINALITY) == '1:N'
																		&& model[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_SOURCE] == '_2' // Inverse
																	)
																) {
																	// Here should never enter because you'll be blocked from button pop-up
																} else if (
																	domain.get(CMDBuild.core.proxy.CMProxyConstants.CARDINALITY) == 'N:N'
																	&& domainRelationItem['dst_id'] == me.card.get(CMDBuild.core.proxy.CMProxyConstants.ID)
																) {
																	alreadyRelatedCardsIds.push(options.params[CMDBuild.core.proxy.CMProxyConstants.CARD_ID]);
																} else {
																	_warning('onAddRelationButtonClick, domain valutation not catch');
																}
															} else {
																_warning('onAddRelationButtonClick, empty class data object');
															}
														});
													}
												});

												// Add class to disable rows as user feedback
												editRelationWindow.grid.getView().getRowClass = function(record, rowIndex, rowParams, store) {
													return Ext.Array.contains(alreadyRelatedCardsIds, record.get('Id')) ? 'grid-row-disabled' : null;
												};
												editRelationWindow.grid.getView().refresh();

												// Disable row selection
												editRelationWindow.grid.getSelectionModel().addListener('beforeselect', function(selectionModel, record, index, eOpts) {
													return Ext.Array.contains(alreadyRelatedCardsIds, record.get('Id')) ? false : true;
												});
											}
										});
									}
								});
							}
						});
					}, 100)();
				}
			});
		},

		onAddRelationSuccess: function() {
			this.defaultOperationSuccess();
		},

		/**
		 * @param {CMRelationPanelModel} model - relation grid model
		 */
		onEditRelationClick: function(model) {
			var me = this;
			var data = model.raw || model.getData();
			var classData = _CMCache.getClassById(model.get('dst_cid'));
			var domain = _CMCache.getDomainById(model.get('dom_id'));
			var masterAndSlave = getMasterAndSlave(model.get(CMDBuild.core.proxy.CMProxyConstants.SOURCE));

			var editRelationWindow = Ext.create('CMDBuild.view.management.classes.relations.CMEditRelationWindow', {
				domain: domain,
				classObject: classData,
				sourceCard: this.card,
				relation: {
					rel_attr: data.attr_as_obj,
					dst_cid: model.get('dst_cid'),
					dst_id: model.get('dst_id'),
					dom_id: model.get('dom_id'),
					rel_id: model.get('rel_id'),
					masterSide: masterAndSlave.masterSide,
					slaveSide: masterAndSlave.slaveSide
				},
				filterType: this.view.id,
				successCb: function() {
					me.onEditRelationSuccess();
				},
				selModel: Ext.create('CMDBuild.selection.CMMultiPageSelectionModel', {
					mode: 'SINGLE',
					idProperty: 'Id' // required to identify the records for the data and not the id of ext
				})
			});

			this.mon(editRelationWindow, 'destroy', function() {
				this.loadData();
			}, this, { single: true });

			editRelationWindow.show();

			// Model fix to select right row(s) with select()
			model.set({
				Code: model.get('dst_code'),
				Description: model.get('dst_desc'),
				Id: model.get('dst_id'),
				id: model.get('dst_id'),
				IdClass: model.get('dst_cid')
			});

			// Select right cards as a modify routine
			editRelationWindow.grid.getStore().load({
				callback: function(records, operation, success) {
					Ext.Function.createDelayed(function() { // HACK to wait store to be correctly loaded
						if (!Ext.isEmpty(model))
							editRelationWindow.grid.getSelectionModel().select(model);
					}, 100)();
				}
			});
		},

		onEditRelationSuccess: function() {
			this.defaultOperationSuccess();
		},

		/**
		 * @param {Object} model - relation grid model
		 */
		onDeleteRelationClick: function(model) {
			var me = this;
			var parameterNames = CMDBuild.ServiceProxy.parameter;
			var masterAndSlave = getMasterAndSlave(model.get(CMDBuild.core.proxy.CMProxyConstants.SOURCE));

			Ext.Msg.confirm(
				CMDBuild.Translation.management.findfilter.msg.attention,
				CMDBuild.Translation.management.modcard.delete_relation_confirm,
				makeRequest,
				this
			);

			function makeRequest(btn) {
				if (btn == 'yes') {
					var domain = _CMCache.getDomainById(model.get('dom_id'));
					var params = {};
					var attributes = {};

					params[parameterNames.DOMAIN_NAME] = domain.getName();
					params[parameterNames.RELATION_ID] = model.get('rel_id');
					params[parameterNames.RELATION_MASTER_SIDE] = masterAndSlave.masterSide;

					var masterSide = {};
					masterSide[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.card.get('IdClass'));
					masterSide[parameterNames.CARD_ID] = me.card.get('Id');

					attributes[masterAndSlave.masterSide] = [masterSide];

					var slaveSide = {};
					slaveSide[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(model.get('dst_cid'));
					slaveSide[parameterNames.CARD_ID] = model.get('dst_id');

					attributes[masterAndSlave.slaveSide] = [slaveSide];

					params[parameterNames.ATTRIBUTES] = Ext.encode(attributes);

					CMDBuild.LoadMask.get().show();
					CMDBuild.core.proxy.CMProxyRelations.remove({
						params: params,
						scope: this,
						success: this.onDeleteRelationSuccess,
						callback: function() {
							CMDBuild.LoadMask.get().hide();
							this.loadData();
						}
					});
				}
			}
		},

		onDeleteRelationSuccess: function() {
			this.defaultOperationSuccess();
		},

		defaultOperationSuccess: function() {
			if (true) { // TODO Check if the modified relation was associated to a reference
				this.fireEvent(this.CMEVENTS.serverOperationSuccess);
			} else {
				this.loadData();
			}
		},

		/**
		 * @param {CMDBuild.model.classes.CMRelationPanelModel} model (CMRelationPanelModel)
		 */
		onEditCardClick: function(model) {
			openCardWindow.call(this, model, true);
		},

		/**
		 * @param {CMDBuild.model.classes.CMRelationPanelModel} model (CMRelationPanelModel)
		 */
		onViewCardClick: function(model) {
			openCardWindow.call(this, model, false);
		},

		/**
		 * @param {CMDBuild.model.classes.CMRelationPanelModel} model (CMRelationPanelModel)
		 */
		onOpenAttachmentClick: function(model) {
			var w = new CMDBuild.view.management.common.CMAttachmentsWindow();

			new CMDBuild.controller.management.common.CMAttachmentsWindowController(w, modelToCardInfo(model));

			w.show();
		}
	});

	Ext.define('CMDBuild.controller.management.workflow.CMActivityRelationsController', {
		extend: 'CMDBuild.controller.management.classes.CMCardRelationsController',

		mixins: {
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		constructor: function() {
			this.callParent(arguments);

			_CMWFState.addDelegate(this);
		},

		/**
		 * @param {Object} pi
		 */
		updateForProcessInstance: function(pi) {
			this.card = pi;

			var classId = pi.getClassId();

			if (classId) {
				var entryType = _CMCache.getEntryTypeById(classId);

				if (this.lastEntryType != entryType) {
					if (!entryType || entryType.get(CMDBuild.core.proxy.CMProxyConstants.TABLE_TYPE) == 'simpletable')
						entryType = null;

					this.lastEntryType = entryType;
					this.view.addRelationButton.setDomainsForEntryType(entryType);
				}
			}
		},

		// override
		loadData: function() {
			var pi = _CMWFState.getProcessInstance();

			if (pi != null && tabIsActive(this.view)) {
				var el = this.view.getEl();

				if (el)
					el.mask();

				var parameterNames = CMDBuild.ServiceProxy.parameter;
				var parameters = {};
				parameters[parameterNames.CARD_ID] =  pi.getId();
				parameters[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(pi.getClassId());
				parameters[parameterNames.DOMAIN_LIMIT] = CMDBuild.Config.cmdbuild.relationlimit;

				CMDBuild.core.proxy.CMProxyRelations.getList({
					params: parameters,
					scope: this,
					success: function(result, options, decodedResult) {
						el.unmask();
						this.view.suspendLayouts();
						this.view.fillWithData(decodedResult.domains);
						this.view.resumeLayouts(true);
					}
				});
			}
		},

		// override
		getCardId: function() {
			return this.card.get('id');
		},

		// override
		getClassId: function() {
			return this.card.get('classId');
		},

		// wfStateDelegate
		onProcessClassRefChange: function(entryType) {
			this.view.disable();
			this.view.clearStore();
		},

		onProcessInstanceChange: function(processInstance) {
			if (processInstance && processInstance.isNew()) {
				this.onProcessClassRefChange();
			} else {
				this.updateForProcessInstance(processInstance);

				this.view.enable();
				this.loadData();
			}
		},

		onActivityInstanceChange: Ext.emptyFn,

		// override
		buildCardModuleStateDelegate: Ext.emptyFn,
		onEntryTypeSelected: Ext.emptyFn,
		onCardSelected: Ext.emptyFn
	});

	function modelToCardInfo(model) {
		return {
			Id: model.get('dst_id'),
			IdClass: model.get('dst_cid'),
			Description: model.get('dst_desc')
		};
	}

	function openCardWindow(model, editable) {
		var w = Ext.create('CMDBuild.view.management.common.CMCardWindow', {
			cmEditMode: editable,
			withButtons: editable,
			title: model.get(CMDBuild.core.proxy.CMProxyConstants.LABEL) + ' - ' + model.get('dst_desc')
		});

		if (editable) {
			w.on('destroy', function() {
				// cause the reload of the main card-grid, it is needed for the case in which I'm editing the target card
				this.fireEvent(this.CMEVENTS.serverOperationSuccess);
				this.loadData();
			}, this, {single: true});
		}

		new CMDBuild.controller.management.common.CMCardWindowController(w, {
			entryType: model.get('dst_cid'), // classid of the destination
			card: model.get('dst_id'), // id of the card destination
			cmEditMode: editable
		});

		w.show();
	}

	function tabIsActive(t) {
		return t.ownerCt.layout.getActiveItem().id == t.id;
	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className;

		if (this.callBacks[className])
			this.callBacks[className].call(this, model);
	}

	function onItemDoubleclick(grid, model, html, index, e, options) {
		this.onFollowRelationClick(model);
	}

	// Define who is the master
	function getMasterAndSlave(source) {
		var out = {};
		if (source == '_1') {
			out.slaveSide = '_2';
			out.masterSide = '_1';
		} else {
			out.slaveSide = '_1';
			out.masterSide = '_2';
		}

		return out;
	}

	function onDomainNodeExpand(node) {
		if (node.get('relations_size') > CMDBuild.Config.cmdbuild.relationlimit) {
			node.removeAll();

			var el = this.view.getEl();
			if (el)
				el.mask();

			var parameterNames = CMDBuild.ServiceProxy.parameter;
			var parameters = {};

			parameters[parameterNames.CARD_ID] = this.getCardId();
			parameters[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.getClassId());
			parameters[parameterNames.DOMAIN_ID] = node.get('dom_id');
			parameters[parameterNames.DOMAIN_SOURCE] = node.get(CMDBuild.core.proxy.CMProxyConstants.SOURCE);

			CMDBuild.core.proxy.CMProxyRelations.getList({
				params: parameters,
				scope: this,
				success: function(result, options, decodedResult) {
					el.unmask();
					this.view.suspendLayouts();

					var cc = this.view.convertRelationInNodes(
						decodedResult.domains[0].relations,
						node.data.dom_id,
						node.data.src,
						node.data,
						node
					);

					this.view.resumeLayouts(true);
				}
			});
		}
	}

})();