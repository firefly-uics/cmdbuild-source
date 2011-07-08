(function() {

	var tr = CMDBuild.Translation.management.modcard;

	Ext.define("CMDBuild.controller.management.classes.CMCardRelationsController", {
		constructor: function(v, sc) {
			this.view = v;
			this.superController = sc;

			this.currentClass = null;
			this.currentCard = null;

			this.callBacks = {
				'action-relation-go': this.onFollowRelationClick,
				'action-relation-edit': this.onEditRelationClick,
				'action-relation-delete': this.onDeleteRelationClick,
				'action-relation-editcard': this.onEditCardClick,
				'action-relation-viewcard': this.onViewCardClick,
				// TODO: refactor this, as the next 2 things are useful only for the workflow (or port modify/card also here)
				'action-card-modify': this.modifyCard,
				'action-card-delete': this.deleteCard
			};

			this.view.store.getRootNode().on("append", function(root, newNode) {
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
			this.view.addRelationButton.setDomainsForEntryType(selection);
		},

		onCardSelected: function(card) {
			this.currentCard = card;
			this.currentCardPrivileges = {
				create: card.get("data.priv_create"),
				write: card.get("data.priv_write")
			};

			if (this.currentClass != null) {
				this.loadData();
				this.view.enable();
			}

			// else is a simple table that has no relations
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
			var a = new CMDBuild.view.management.classes.relations.CMEditRelationWindow({
				currentCard: this.currentCard,
				relation: {
					dst_cid: d.dst_cid,
					dom_id: d.dom_id,
					rel_id: -1,
					src: d.src
				},
				selType: "checkboxmodel",
				multiSelect: true,
				filterType: this.view.id
			}).show();
		},

		onEditRelationClick: function(model) {
			var a = new CMDBuild.view.management.classes.relations.CMEditRelationWindow({
				relation: {
					dst_cid: model.get("dst_cid"),
					dom_id: model.get("dom_id"),
					rel_id: model.get("rel_id"),
					src: model.get("src")
				},
				filterType: this.view.id
			}).show();
		},

		onDeleteRelationClick: function(model) {
			alert("@@ delete the relation");
		},

		onEditCardClick: function(model) {
			openCardWindow(model, true);
		},

		onViewCardClick: function(model) {
			openCardWindow(model, false);
		}
	});

	function openCardWindow(model, editable) {
		new CMDBuild.controller.management.common.CMCardWindowController(
			new CMDBuild.view.management.common.CMCardWindow({
				cmEditMode: editable,
				withButtons: editable,
				classId: model.get("dst_cid"), // classid of the destination
				cardId: model.get("dst_id"), // id of the card destination
				title: model.get("label") + " - " + model.get("dst_desc")
			}).show()
		);
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
					var cc = this.view.convertRelationInNodes(response.domains[0].relations);
					node.appendChild(cc);
					el.unmask();
				}
			});
		}
	}

})();

/*
 addRelations: function() {
		addRelationsWin = new CMDBuild.Management.AddRelationWindow({
			classId: this.currentClassId,
			cardId: this.currentCardId,
			filterType: 'addrelation'
		});
		addRelationsWin.show();
	},

	deleteRelation: function(jsonRow) {
		Ext.Msg.confirm(
			this.translation.delete_relation,
			this.translation.delete_relation_confirm,
			function(btn) {
				if (btn != 'yes') {
					return;
				}
				CMDBuild.LoadMask.get().show();
				CMDBuild.Ajax.request({
					url : 'services/json/management/modcard/deleterelation',
					params : {
						"DomainId" : jsonRow.DomainId,
						"Class1Id" : jsonRow.Class1Id,
						"Card1Id" : jsonRow.Card1Id,
						"Class2Id" : jsonRow.Class2Id,
						"Card2Id" : jsonRow.Card2Id
					},					
					method : 'POST',
					scope : this,
					success : this.onDeleteRelationSuccess,
					callback: function() {
		            	CMDBuild.LoadMask.get().hide();
		            }
			 	});
			}, this);
	},

	onDeleteRelationSuccess: function() {
		this.publish('cmdb-reload-' + this.eventtype, {cardId: this.currentCardId});
	},
	
	editRelation: function(jsonRowP) {
		var cardIdP = this.currentCardId;
		var _this = this;
		var showRelationWindow = function(attributes) {
			var jsonRow = jsonRowP;
			var cardId = cardIdP; //visibility issues
			editRelationsWin = new CMDBuild.Management.EditRelationWindow({
				cardId: cardId,
				domainId : jsonRow.DomainId,
				class1Id : jsonRow.Class1Id,
				card1Id : jsonRow.Card1Id,
				class2Id : jsonRow.Class2Id,
				card2Id : jsonRow.Card2Id,
				domainDirection : jsonRow.DomainDir,
				domainDestClassId : jsonRow.DomainDestClassId,
				attributes: attributes,
				filterType: 'editrelation',
				updateEventName: _this.updateEventName
			});
			editRelationsWin.show();
		};
		CMDBuild.Management.FieldManager.loadAttributes(jsonRowP.DomainDestClassId, showRelationWindow);
	},

	viewCard: function(jsonRow) {
		var _this = this;
		CMDBuild.Ajax.request({
			url : 'services/json/management/modcard/getcard',
			params : {
				"IdClass" : jsonRow.ClassId,
				"Id" : jsonRow.CardId
			},
			method : 'POST',
			scope : this,
			success : function(response, options, decoded) {
				new CMDBuild.Management.DetailWindow({
					cardData: decoded.card,
					className: jsonRow.Class,
					classAttributes: decoded.attributes,
					idDomain: jsonRow.Domain
				}).show();
			}
	 	});
	},

	editCard: function(jsonRow) {
		var _this = this;
		CMDBuild.Ajax.request({
			url : 'services/json/management/modcard/getcard',
			params : {
				"IdClass" : jsonRow.ClassId,
				"Id" : jsonRow.CardId
			},
			method : 'POST',
			scope : this,
			success : function(response, options, decoded) {
				new CMDBuild.Management.EditDetailWindow({
					updateEventName: _this.updateEventName,
					cardData: decoded.card,
					className: jsonRow.Class,
					classAttributes: decoded.attributes,
					idDomain: jsonRow.Domain
				}).show();
			}
	 	});
	}
 */