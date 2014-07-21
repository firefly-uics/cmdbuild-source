(function() {

	var CLASS_ID_AS_RETURNED_BY_GETCARDLIST = 'IdClass';

	Ext.require('CMDBuild.core.proxy.CMProxyRelations');

	Ext.define('CMDBuild.controller.management.classes.CMCardRelationsController', {
		extend: 'CMDBuild.controller.management.classes.CMModCardSubController',

		constructor: function(v, sc) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);
			this.hasDomains = false;

			this.callBacks = {
				'action-relation-go': this.onFollowRelationClick,
				'action-relation-edit': this.onEditRelationClick,
				'action-relation-delete': this.onDeleteRelationClick,
				'action-relation-editcard': this.onEditCardClick,
				'action-relation-viewcard': this.onViewCardClick,
				'action-relation-attach': this.onOpenAttachmentClick
			};

			this.view.store.getRootNode().on('append', function(root, newNode) {
				// the nodes with depth == 1 are the folders
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

		onEntryTypeSelected: function(entryType) {
			this.callParent(arguments);

			this.card = null;

			if (!this.entryType || this.entryType.get(CMDBuild.core.proxy.CMProxyConstants.TABLE_TYPE) == 'simpletable')
				this.entryType = null;

			this.view.disable();
			this.view.clearStore();
		},

		onCardSelected: function(card) {
			this.callParent(arguments);
			this.view.clearStore();
			this.view.disable();

			if (card) {
				this.updateCurrentClass(card);

				if (this.hasDomains) {
					this.view.enable();
					this.loadData();
				}
			}
		},

		updateCurrentClass: function(card) {
			var classId = card.get(CLASS_ID_AS_RETURNED_BY_GETCARDLIST);
			var currentClass = _CMCache.getEntryTypeById(classId);

			if (this.currentClass != currentClass) {
				if (!currentClass || currentClass.get(CMDBuild.core.proxy.CMProxyConstants.TABLE_TYPE) == 'simpletable')
					currentClass = null;

				this.currentClass = currentClass;
				this.hasDomains = this.view.addRelationButton.setDomainsForEntryType(currentClass);
			}
		},

		loadData: function() {
			if (this.card != null && tabIsActive(this.view)) {
				var el = this.view.getEl();

				if (el)
					el.mask();

				var parameterNames = CMDBuild.ServiceProxy.parameter;
				var parameters = {};

				parameters[parameterNames.CARD_ID] = this.getCardId();
				parameters[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.getClassId());
				parameters[parameterNames.DOMAIN_LIMIT] = CMDBuild.Config.cmdbuild.relationlimit;

				CMDBuild.ServiceProxy.relations.getList({
					params: parameters,
					scope: this,
					success: function(a, b, response) {
						el.unmask();

						this.view.fillWithData(response.domains);

						// AddRelation button update
							var toDisableButtons = [];

							// Max relations number check on domains
							Ext.Array.forEach(response.domains, function(item, index, allItems) {
								var domainObjext = _CMCache.getDomainById(item[CMDBuild.core.proxy.CMProxyConstants.ID]);

								switch (domainObjext.get(CMDBuild.core.proxy.CMProxyConstants.CARDINALITY)) {
									case '1:1': {
										if (item[CMDBuild.core.proxy.CMProxyConstants.RELATIONS_SIZE] == 1)
											toDisableButtons.push(domainObjext.get(CMDBuild.core.proxy.CMProxyConstants.ID));
									} break;

									case '1:N': {
										if (item[CMDBuild.core.proxy.CMProxyConstants.RELATIONS_SIZE] == 1)
											toDisableButtons.push(domainObjext.get(CMDBuild.core.proxy.CMProxyConstants.ID));
									} break;
								}
							}, this);

							// Loop trough split button menu items and enable/disable
							Ext.Array.forEach(this.view.addRelationButton.menu.items.items, function(item, index, allItems) {
								item.setDisabled(Ext.Array.contains(toDisableButtons, item.domain.dom_id));
							}, this);
						// END: AddRelation button update
					}
				});
			}
		},

		getCardId: function() {
			return this.card.get('Id');
		},

		getClassId: function() {
			return this.card.get('IdClass');
		},

		onFollowRelationClick: function(model) {
			if (model.get('depth') > 1)
				_CMMainViewportController.openCard({
					Id: model.get('dst_id'),
					IdClass: model.get('dst_cid')
				});
		},

		/**
		 * @param (object) model - relation model
		 */
		onAddRelationButtonClick: function(model) {
			var me = this;
			var masterAndSlave = getMasterAndSlave(model.src);
			var domain = _CMCache.getDomainById(model.dom_id);
			var isMany = false;
			var destination = model.src == '_1' ? '_2' : '_1';

			if (domain)
				isMany = domain.isMany(destination);

			var editRelationWindow = Ext.create('CMDBuild.view.management.classes.relations.CMEditRelationWindow', {
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
				filterType: this.view.id,
				successCb: function() {
					me.onAddRelationSuccess();
				}
			});

			this.mon(editRelationWindow, 'destroy', function() {
				this.loadData();
			}, this, { single: true });

			editRelationWindow.show();

			// Card filter to avoid wrong selection on relation creation
			editRelationWindow.grid.getStore().load({
				scope: this,
				callback: function(records, operation, success) {
					Ext.Function.createDelayed(function() { // HACK to wait store to be correctly loaded
						var parameters = {};
						var cardsIdArray = [];

						editRelationWindow.grid.getStore().each(function(record) {
							cardsIdArray.push(record.get(CMDBuild.core.proxy.CMProxyConstants.ID));
						});

						parameters[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_ID] = parseInt(model.dom_id);
						parameters[CMDBuild.core.proxy.CMProxyConstants.CLASS_TARGET_ID] = model.dst_cid;
						parameters[CMDBuild.core.proxy.CMProxyConstants.CARDS] = Ext.encode(cardsIdArray);

						// TODO: change with real implementation
//						CMDBuild.core.proxy.CMProxyRelations.isCardAssignedToRelation({
//							params: parameters,
//							scope: this,
//							success: function(records, operation, success) {
//								_debug('CMDBuild.ServiceProxy.relations.isCardAssignedToRelation success');
//								// returns data to delete from grid
//							}
//						});

						var array = CMDBuild.core.proxy.CMProxyRelations.isCardAssignedToRelation({
							params: parameters,
							scope: this,
							success: function(records, operation, success) {
								_debug('CMDBuild.ServiceProxy.relations.isCardAssignedToRelation success');
							}
						});

						// TODO: move in proxy call success function
						editRelationWindow.grid.getStore().clearFilter(true);
						editRelationWindow.grid.getStore().filterBy(function(record, id) {
							if (Ext.Array.contains(array, id))
								return false;

							return true;
						}, this);
					}, 100)();
				}
			});
		},

		onAddRelationSuccess: function() {
			this.defaultOperationSuccess();
		},

		/**
		 * @param (object) model - relation model
		 */
		onEditRelationClick: function(model) {
			var me = this;
			var data = model.raw || model.data;
			var masterAndSlave = getMasterAndSlave(model.get(CMDBuild.core.proxy.CMProxyConstants.SOURCE));

			var editRelationWindow = Ext.create('CMDBuild.view.management.classes.relations.CMEditRelationWindow', {
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
					CMDBuild.ServiceProxy.relations.remove({
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

		// overridden in CMManageRelationController
		onDeleteRelationSuccess: function() {
			this.defaultOperationSuccess();
		},

		// overridden in CMManageRelationController
		defaultOperationSuccess: function() {
			if (true) { // TODO Check if the modified relation was associated to a reference
				this.fireEvent(this.CMEVENTS.serverOperationSuccess);
			} else {
				this.loadData();
			}
		},

		onEditCardClick: function(model) {
			openCardWindow.call(this, model, true);
		},

		onViewCardClick: function(model) {
			openCardWindow.call(this, model, false);
		},

		onOpenAttachmentClick: function(model) {
			var w = new CMDBuild.view.management.common.CMAttachmentsWindow();

			new CMDBuild.controller.management.common.CMAttachmentsWindowController(w,modelToCardInfo(model));

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

		// override
		updateForProcessInstance: function(pi) {
			this.card = pi;
			var classId = pi.getClassId();

			if (classId) {
				var entryType = _CMCache.getEntryTypeById(classId);

				if (this.lastEntryType != entryType) {
					if (!entryType || entryType.get(CMDBuild.core.proxy.CMProxyConstants.TABLE_TYPE) == 'simpletable')
						entryType = null;

					this.lastEntryType = entryType;
					this.hasDomains = this.view.addRelationButton.setDomainsForEntryType(entryType);
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

				CMDBuild.ServiceProxy.relations.getList({
					params: parameters,
					scope: this,
					success: function(a,b, response) {
						el.unmask();
						this.view.suspendLayouts();
						this.view.fillWithData(response.domains);
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

				if (this.hasDomains) {
					this.view.enable();
					this.loadData();
				} else {
					this.view.disable();
				}
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

			CMDBuild.ServiceProxy.relations.getList({
				params: parameters,
				scope: this,
				success: function(a,b, response) {
					el.unmask();
					this.view.suspendLayouts();

					var cc = this.view.convertRelationInNodes(
						response.domains[0].relations,
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