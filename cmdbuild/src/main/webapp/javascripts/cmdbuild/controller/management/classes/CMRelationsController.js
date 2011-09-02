(function() {
	var ID_AS_RETURNED_BY_GETCARDLIST = "Id",
		CLASS_ID_AS_RETURNED_BY_GETCARDLIST = "IdClass";

	var tr = CMDBuild.Translation.management.modcard;

	Ext.define("CMDBuild.controller.management.classes.CMCardRelationsController", {
		constructor: function(v, sc) {
			this.view = v;
			this.superController = sc;

			this.currentClass = null;
			this.currentCard = null;
			this.hasDomains = false;

			this.callBacks = {
				'action-relation-go': this.onFollowRelationClick,
				'action-relation-edit': this.onEditRelationClick,
				'action-relation-delete': this.onDeleteRelationClick,
				'action-relation-editcard': this.onEditCardClick,
				'action-relation-viewcard': this.onViewCardClick
			};

			this.view.store.getRootNode().on("append", function(root, newNode) {
				// the nodes with depth == 1 are the folders
				if (newNode.get("depth") == 1) {
					newNode.on("expand", onDomainNodeExpand, this, {single: true});
				}
			}, this);

			this.view.addRelationButton.on("cmClick", this.onAddRelationButtonClick, this);

			this.view.on('beforeitemclick', cellclickHandler, this);
			this.view.on("itemdblclick", onItemDoubleclick, this);
			this.view.on("activate", this.loadData, this);
		},

		onEntrySelect: function(selection) {
			var cachedEntryType = _CMCache.getEntryTypeById(selection.get("id"));

			this.currentCard = null;

			if (!cachedEntryType || cachedEntryType.get("tableType") == "simpletable") {
				this.currentClass = null;
			} else {
				this.currentClass = selection;
			}

			this.view.disable();
			this.view.clearStore();
		},


		onCardSelected: function(card) {
			this.currentCard = card;
			this.currentCardPrivileges = {
				create: card.get("priv_create"),
				write: card.get("priv_write")
			};
			this.updateCurrentClass(card);

			if (this.hasDomains) {
				this.view.enable();
				this.loadData();
			} else {
				this.view.clearStore();
			}
		},

		updateCurrentClass: function(card) {
			var classId = card.get(CLASS_ID_AS_RETURNED_BY_GETCARDLIST),
				currentClass = _CMCache.getEntryTypeById(classId);
			if (this.currentClass != currentClass) {
				if (!currentClass || currentClass.get("tableType") == "simpletable") {
					currentClass = null;
				}
				this.currentClass = currentClass;
				this.hasDomains = this.view.addRelationButton.setDomainsForEntryType(currentClass);
			}
		},

		loadData: function() {
			if (this.currentCard == null || !tabIsActive(this.view)) {
				return;
			}

			var el = this.view.getEl();
			if (el) {
				el.mask();
			}

			CMDBuild.ServiceProxy.relations.getList({
				params: {
					Id: this.currentCard.get("Id"),
					IdClass: this.currentCard.get("IdClass"),
					domainlimit: CMDBuild.Config.cmdbuild.relationlimit
				},
				scope: this,
				success: function(a,b, response) {
					el.unmask();
					this.view.fillWithData(response.domains);
				}
			});
		},

		onFollowRelationClick: function(model) {
			if (model.get("depth") > 1) {
				this.superController.openCard({
					Id: model.get("dst_id"),
					IdClass: model.get("dst_cid")
				});
			}
		},

		onAddRelationButtonClick: function(d) {
			var domain = _CMCache.getDomainById(d.dom_id),
				isMany = false,
				dest = d.src == "_1" ? "_2" : "_1";

			if (domain) {
				isMany = domain.isMany(dest);
			};

			var me = this,
				a = new CMDBuild.view.management.classes.relations.CMEditRelationWindow({
					currentCard: this.currentCard,
					relation: {
						dst_cid: d.dst_cid,
						dom_id: d.dom_id,
						rel_id: -1,
						src: d.src
					},
					selType: isMany ? "checkboxmodel" : "rowmodel",
					multiSelect: isMany,
					filterType: this.view.id,

					successCb: function() {
						me.onAddRelationSuccess();
					}
				}).show();
		},

		onAddRelationSuccess: function() {
			this.defaultOperationSuccess();
		},

		onEditRelationClick: function(model) {
			var me = this,
				data = model.raw || model.data,
				a = new CMDBuild.view.management.classes.relations.CMEditRelationWindow({
					relation: {
						rel_attr: data.attr_as_obj,
						dst_cid: model.get("dst_cid"),
						dom_id: model.get("dom_id"),
						rel_id: model.get("rel_id"),
						src: model.get("src")
					},
					filterType: this.view.id,
					successCb: function() {
						me.onEditRelationSuccess();
					}
				}).show();
				
				
		},

		onEditRelationSuccess: function() {
			this.defaultOperationSuccess();
		},

		onDeleteRelationClick: function(model) {
			Ext.Msg.confirm(CMDBuild.Translation.management.findfilter.msg.attention,
				CMDBuild.Translation.management.modcard.delete_relation_confirm,
				makeRequest, this);

			function makeRequest(btn) {
				if (btn != 'yes') {
					return;
				}

				var o = {
					did: model.get("dom_id"),
					id: model.get("rel_id")
				};

				CMDBuild.LoadMask.get().show();
				CMDBuild.ServiceProxy.relations.remove({
					params: {
						JSON: Ext.JSON.encode(o)
					},
					scope: this,
					success: this.onDeleteRelationSuccess,
					callback: function() {
						CMDBuild.LoadMask.get().hide();
					}
				});
			};
		},

		// overridden in CMManageRelationController
		onDeleteRelationSuccess: function() {
			this.defaultOperationSuccess();
		},

		// overridden in CMManageRelationController
		defaultOperationSuccess: function() {
			if (true) { // TODO Check if the modified relation was associated to a reference
				var card = this.currentCard;
				this.superController.openCard({
					Id: card.get(ID_AS_RETURNED_BY_GETCARDLIST),
					IdClass: card.get(CLASS_ID_AS_RETURNED_BY_GETCARDLIST)
				});
			} else {
				this.loadData();
			}
		},

		onEditCardClick: function(model) {
			openCardWindow.call(this, model, true);
		},

		onViewCardClick: function(model) {
			openCardWindow.call(this, model, false);
		}
	});

	function openCardWindow(model, editable) {
		var w = new CMDBuild.view.management.common.CMCardWindow({
			cmEditMode: editable,
			withButtons: editable,
			classId: model.get("dst_cid"), // classid of the destination
			cardId: model.get("dst_id"), // id of the card destination
			title: model.get("label") + " - " + model.get("dst_desc")
		});

		if (editable) {
			w.on("destroy", function() {
				this.loadData();
			}, this, {single: true});
		}
		
		new CMDBuild.controller.management.common.CMCardWindowController(w);
		w.show();
	}

	function tabIsActive(t) {
		return t.ownerCt.layout.getActiveItem().id == t.id;
	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className; 

		if (this.callBacks[className]) {
			this.callBacks[className].call(this, model);
		}
	}

	function onItemDoubleclick(grid, model, html, index, e, options) {
		this.onFollowRelationClick(model);
	}

	function onDomainNodeExpand(node) {
		if (node.get("relations_size") > CMDBuild.Config.cmdbuild.relationlimit) {
			node.removeAll();

			var el = this.view.getEl();
			if (el) {
				el.mask();
			}

			CMDBuild.ServiceProxy.relations.getList({
				params: {
					Id: this.currentCard.get("Id"),
					IdClass: this.currentCard.get("IdClass"),
					domainId: node.get("dom_id"),
					src: node.get("src")
				},
				scope: this,
				success: function(a,b, response) {
					var cc = this.view.convertRelationInNodes(response.domains[0].relations, 
							node.data.dom_id, 
							node.data.src,
							node.data);

					node.appendChild(cc);
					el.unmask();
				}
			});
		}
	}

})();