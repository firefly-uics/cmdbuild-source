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
			this.view.detailGrid.on('beforeitemclick', cellclickHandler, this);

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
			new CMDBuild.controller.management.common.CMCardWindowController(
				new CMDBuild.view.management.classes.masterDetail.DetailWindow({
					cmEditMode: true,
					withButtons: true,
					classId: model.get("IdClass"), // classid of the destination
					cardId: model.get("Id") // id of the card destination
				}).show()
			);
		},
		
		onShowDetailClick: function(model) {
			alert("@@ show detail");
		},
		
		onDeleteDetailClick: function(model) {
			Ext.Msg.confirm(CMDBuild.Translation.management.findfilter.msg.attention,
				CMDBuild.Translation.management.modcard.delete_relation_confirm,
				makeRequest, this);

			function makeRequest(btn) {
				if (btn != 'yes') {
					return;
				}

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
			};
		},
		
		onOpenGraphClick: function(model) {
			alert("@@ open graph detail");
		},
		
		onOpenNoteClick: function(model) {
			new CMDBuild.view.management.common.CMNoteWindow({
				masterCard: model
			}).show();
		},
		
		onOpenAttachmentClick: function(model) {
			new CMDBuild.controller.management.common.CMAttachmentsWindowController(
				new CMDBuild.view.management.common.CMAttachmentsWindow({
					masterCard: model
				}).show()
			);
		}
	});
	
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
			if (this.currentDetail != null) {
				this.view.updateDetailGrid({
					domain: this.currentDetail,
					masterCard: this.currentMasterData
				});
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
			this.view.selectForeignKey(detail);
		}

		updateDetailGrid.call(this);
	}

	function selectDetail(detail) {
		this.currentForeignKey = undefined;
		this.currentDetail = detail;
		this.view.selectDetail(detail);
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
		new CMDBuild.view.management.classes.masterDetail.DetailWindow({
			cmEditMode: true,
			withButtons: true,
			classId: o.classId,
			cardId: -1 // for a new card
		}).show()
	}
})()