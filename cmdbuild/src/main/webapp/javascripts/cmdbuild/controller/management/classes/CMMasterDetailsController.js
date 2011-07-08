(function() {

	var tr = CMDBuild.Translation.management.modcard;
	var MD = "detail";
	var FK = "foreignkey";

	Ext.define("CMDBuild.controller.management.classes.masterDetails.CMMasterDetailsController", {
		constructor: function(v, sc) {
			this.view = v;
			this.superController = sc;

			this.currentEntryType = null;
			this.currentForeignKey = null;
			this.currentDetail = null;
			this.actualMasterData = null;

			this.view.tabs.on("click", onTabClick, this);

			this.callBacks = {
				
			};

		},

		onEntrySelect: function(selection) {
			this.currentTab = null;
			this.currentForeignKey = null;
			this.currentDetail = null;
			this.actualMasterData = null;

			this.currentEntryType = selection;
			this.view.loadDetailsAndFKThenBuildSideTabs(this.currentEntryType.get("id"));
			this.view.resetDetailGrid();
		},

		onCardSelected: function(card) {
			this.actualMasterData = card;
			// given the tab is not active but enabled
			// and we change card
			// when the tab is activated
			// then the grid should be updated
			if (tabIsActive(this.view)) {
				updateDetailGrid.call(this);
			} else {
				this.view.on('activate', updateDetailGrid, this, {single: true});
			}
		}
	});

	function updateDetailGrid() {
		if (this.actualMasterData != null) {
			if (this.currentDetail != null) {
				this.view.updateDetailGrid({
					domain: this.currentDetail,
					masterCard: this.actualMasterData
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

})()