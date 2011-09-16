(function() {

	var tr = CMDBuild.Translation.management.modcard,
		MD = "detail",
		FK = "foreignkey",
		detailURL = {
			get: "services/json/management/modcard/getdetaillist",
			remove: "services/json/management/modcard/deleterelation"
		},
		bigTablesURL = {
			get: "services/json/management/modcard/getcardlist",
			remove: "services/json/management/modcard/deletecard"
		};

	Ext.define("CMDBuild.controller.management.classes.masterDetails.CMMasterDetailsController", {
		constructor: function(v, sc) {
			this.view = v;
			this.superController = sc;

			this.currentEntryType = null;
			this.currentForeignKey = null;
			this.currentDetail = null;
			this.currentMasterData = null;

			this.view.tabs.on("click", onTabClick, this);
			this.view.detailGrid.mon(this.view.detailGrid, 'beforeitemclick', cellclickHandler, this);
			this.view.detailGrid.mon(this.view.detailGrid, "itemdblclick", onDetailDoubleClick, this);
			this.callBacks = {
				'action-masterdetail-edit': this.onEditDetailClick,
				'action-masterdetail-show': this.onShowDetailClick,
				'action-masterdetail-delete': this.onDeleteDetailClick,
				'action-masterdetail-graph': this.onOpenGraphClick,
				'action-masterdetail-note': this.onOpenNoteClick,
				'action-masterdetail-attach': this.onOpenAttachmentClick 
			};
			
			this.view.addDetailButton.on("cmClick", onAddDetailButtonClick, this);
		},

		onEntrySelect: function(selection) {
			this.currentTab = null;
			this.currentForeignKey = null;
			this.currentDetail = null;
			this.currentMasterData = null;

			this.currentEntryType = selection;
			this.view.disable();
			this.view.loadDetailsAndFKThenBuildSideTabs(this.currentEntryType.get("id"));
			this.view.resetDetailGrid();
		},

		onCardSelected: function(card) {
			this.currentMasterData = card;
			this.view.setDisabled(this.view.empty);
			// given the tab is not active but enabled
			// and we change card
			// when the tab is activated
			// then the grid should be updated
			if (tabIsActive(this.view)) {
				updateDetailGrid.call(this);
			} else {
				this.view.on('activate', updateDetailGrid, this, {single: true});
			}
		},

		onEditDetailClick: function(model) {
			var me = this,
				w = buildWindow.call(this, {
					classId: model.get("IdClass"), // classid of the destination
					cardId: model.get("Id"), // id of the card destination
					editable: true
				});

			w.mon(w, "destroy", function() {
				this.view.reload();
			}, this, {single: true});
	
			new CMDBuild.controller.management.common.CMDetailWindowController(w);
			w.show();
		},

		onShowDetailClick: function(model) {
			var w = buildWindow.call(this, {
				classId: model.get("IdClass"), // classid of the destination
				cardId: model.get("Id"), // id of the card destination
				editable: false
			});

			w.show();
		},

		onDeleteDetailClick: function(model) {
			Ext.Msg.confirm(CMDBuild.Translation.management.findfilter.msg.attention,
				CMDBuild.Translation.management.modcard.delete_card_confirm,
				makeRequest, this);

			function makeRequest(btn) {
				if (btn != 'yes') {
					return;
				}
				
				// if I'm deleting a detail is needed to remove the
				// relation at first
				
				if (this.currentDetail) {
					CMDBuild.LoadMask.get().show()
					CMDBuild.ServiceProxy.relations.remove({
						params : {
							"DomainId" : this.currentDetail.get("id"),
							"Class1Id" : this.currentMasterData.get("IdClass"),
							"Card1Id" : this.currentMasterData.get("Id"),
							"Class2Id" : model.get("IdClass"),
							"Card2Id" : model.get("Id")
						},
						scope: this,
						success: function() {
							removeCard.call(this, model);
						},
						callback: function() {
							CMDBuild.LoadMask.get().hide()
						}
					});
				} else if(this.currentForeignKey) {
					// so, remove directly the card
					removeCard.call(this, model);
				}
			};
		},

		onOpenGraphClick: function(model) {
			CMDBuild.Management.showGraphWindow(model.get("IdClass"), model.get("Id"));
		},

		onOpenNoteClick: function(model) {
			var w = new CMDBuild.view.management.common.CMNoteWindow({
				cardInfo: model
			}).show();
			
			w.mon(w, "destroy", function() {
				this.view.reload();
			}, this, {single: true});
		},

		onOpenAttachmentClick: function(model) {
			new CMDBuild.controller.management.common.CMAttachmentsWindowController(
				new CMDBuild.view.management.common.CMAttachmentsWindow({
					cardInfo: modelToCardInfo(model)
				}).show()
			);
		}
	});

	function modelToCardInfo(model) {
		return {
			Id: model.get("Id"),
			ClassId: model.get("IdClass"),
			Description: model.get("Description")
		};
	}

	function onDetailDoubleClick(grid, model, html, index, e, options) {
		this.superController.openCard({
			Id: model.get("Id"),
			IdClass: model.get("IdClass"),
			activateFirstTab: true
		});
	}

	function removeCard(model) {
		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.card.remove({
			scope : this,
			important: true,
			params : {
				"IdClass": model.get("IdClass"),
				"Id": model.get("Id")
			},
			success : updateDetailGrid,
			callback : function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
	
	function updateDetailGrid() {
		if (this.currentMasterData != null) {
			var p = {
				masterCard: this.currentMasterData
			};

			if (this.currentDetail != null) {
				p["detail"] = this.currentDetail
				this.view.updateGrid(MD, p);
			} else if (this.currentForeignKey != null) {
				p["detail"] = this.currentForeignKey;
				this.view.updateGrid(FK, p);
			} else {
				this.view.activateFirstTab();
			}
		}
	}

	function onTabClick(tab) {
		if (this.currentTab === tab || !tabIsActive(this.view)) {
			return;
		}

		var targetPanel = tab.targetPanel,
			type = targetPanel.detailType,
			detail = this.view.details[type][targetPanel.detailId];

		this.view.addDetailButton.enable();
		this.currentTab = tab;

		if (type == MD) {
			selectDetail.call(this, detail);
		} else {
			selectFK.call(this, detail);
		}

		updateDetailGrid.call(this);
	}

	function selectDetail(detail) {
		this.currentForeignKey = undefined;
		this.currentDetail = detail;
		this.view.selectDetail(detail);
	}

	function selectFK(fk) {
		this.currentDetail = undefined;
		this.currentForeignKey = fk;
		this.view.selectForeignKey(fk);
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

	function onAddDetailButtonClick(o) {
		var me = this,
			w = buildWindow.call(this, {
				classId: o.classId,
				cardId: -1, // new card,
				editable: true
			});
		
		w.mon(w, "destroy", function() {
			this.view.reload();
		}, this, {single: true});

		new CMDBuild.controller.management.common.CMAddDetailWindowController(w);
		w.show();
	}

	/*
	 * o = {
	 * 	classId: ...,
	 *  cardId: ...,
	 *  editable: bool
	 * }
	 */
	function buildWindow(o) {
		var me = this,
			c = {
				referencedIdClass: me.currentMasterData.get("IdClass"),
				fkAttribute: me.currentForeignKey,
				masterData: me.currentMasterData,
				detail: me.currentDetail,
				classId: o.classId,
				cardId: o.cardId,
				cmEditMode: o.editable,
				withButtons: o.editable
			};

		return new CMDBuild.view.management.classes.masterDetail.DetailWindow(c);
	}
})()