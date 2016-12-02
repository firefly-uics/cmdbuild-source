(function() {
	Ext.define('CMDBuild.view.management.classes.map.geoextension.Navigable', {
		navigables : {},
		oldCard : undefined,
		interactionDocument : undefined,
		navigablesToChange : {},
		toRefresh : false,

		constructor : function(interactionDocument) {
			this.interactionDocument = interactionDocument;
		},

		getNavigableFromNode : function(node) {
			var nodeCardId = node.get("cardId");
			var nodeClassName = node.get("className");
			return this.getNavigable({
				cardId : nodeCardId,
				className : nodeClassName
			});
		},
		getNavigable : function(card) {
			var className = card.className;
			if (this.isANavigableClass(className)) {
				var cardId = card.cardId;
				if (!this.navigables[className][cardId]) {
					return null;
				}
				return this.navigables[className][cardId];
			} else {
				return null;
			}
		},
		getNavigableNode : function(card) {
			var navigable = this.getNavigable(card);
			return (!navigable) ? null : navigable.node;
		},
		isANavigableCard : function(card) {
			var navigable = this.getNavigable(card);
			return (!navigable) ? false : navigable.checked;
		},
		isANavigableClass : function(className) {
			return this.navigables[className];
		},
		prepareNavigables : function() {
			var card = this.interactionDocument.getCurrentCard();
			if (this.oldCard && same(this.oldCard, card) && card.carId !== -1) {
				return;
			}
			this.oldCard = card;
			var navigable = this.getNavigable(card);
			if (!navigable) {
				return;
			}
			this.check(navigable, true);
		},

		check : function(navigable, checked) {
			if (navigable.checked === checked) {
				return;
			}
			if (checked) {
				var parents = this.getParents(navigable);
				this.openFromRoot(parents);
			} else {
				this.close(navigable)
			}
			this.interactionDocument.changedNavigables();
		},
		/**
		 * 
		 * @param {Array}
		 *            arrayNavigables Ext.data.TreeModel
		 */
		setNavigables : function(arrayNavigables) {
			this.navigables = {};
			for (var i = 0; i < arrayNavigables.length; i++) {
				var navigable = arrayNavigables[i];
				var cardId = parseInt(navigable.get("cardId"));
				var className = navigable.get("className");
				if (!this.navigables[className]) {
					this.navigables[className] = [];
				}
				this.navigables[className][cardId] = {
					node : navigable,
					checked : navigable.get("checked")
				}
			}
			this.interactionDocument.changedNavigables();
		},
		getParents : function(navigable) {
			var parents = [];
			while (navigable) {
				parents.push({
					navigable : navigable,
					position : undefined
				});
				var parentNode = navigable.node.parentNode;
				navigable = (parentNode) ? this.getNavigableFromNode(parentNode) : null;
			}
			var overBase = true;
			for (var i = parents.length - 1; i >= 0; i--) {
				var isBase = parents[i].navigable.node.get("baseNode");
				if (isBase) {
					overBase = false;
					parents[i].position = "base";
				} else {
					parents[i].position = (overBase) ? "over" : "under";

				}
			}
			return parents;
		},
		openFromRoot : function(parents) {
			for (var i = parents.length - 1; i >= 0; i--) {
				var navigable = parents[i];
				navigable.navigable.checked = true;
				if (navigable.position === "base") {
					this.closeBrothers(navigable.navigable);
				} else if (navigable.position === "under") {
					this.openBrothers(navigable.navigable);
				}
				//over NOP
			}
			this.openAllNode(parents[0].navigable);
		},
		openBrothers : function(navigable) {
			var parentNode = navigable.node.parentNode;
			var children = parentNode.childNodes || parentNode.children || [];
			for (var i = 0; i < children.length; i++) {
				var child = children[i];
				if (child != navigable.node) {
					var navigableChild = this.getNavigableFromNode(child);
					this.openAllNode(navigableChild)
				}
			}
		},
		closeBrothers : function(navigable) {
			var parentNode = navigable.node.parentNode;
			var children = parentNode.childNodes || parentNode.children || [];
			for (var i = 0, l = children.length; i < l; ++i) {
				var child = children[i];
				if (child != navigable.node) {
					var navigableChild = this.getNavigableFromNode(child);
					this.closeAllNode(navigableChild)
				}
			}
		},
		closeAllNode : function(navigable) {
			navigable.checked = false;
			var children = navigable.node.childNodes || navigable.node.children || [];
			for (var i = 0, l = children.length; i < l; ++i) {
				var child = children[i];
				var navigableChild = this.getNavigableFromNode(child);
				this.closeAllNode(navigableChild);
			}
		},
		openAllNode : function(navigable) {
			navigable.checked = true;
			var baseInserted = false;
			var children = navigable.node.childNodes || navigable.node.children || [];
			for (var i = 0; i < children.length; ++i) {
				var child = children[i];
				var navigableChild = this.getNavigableFromNode(child);
				if (!baseInserted) {
					this.openAllNode(navigableChild);
					baseInserted = (child.get("baseNode") === true) ;
				}
				else {
					this.closeAllNode(navigableChild);
					
				}
			}
		},
		close : function(currentNavigable) {
			navigable.checked = false;
			var children = navigable.node.childNodes || navigable.node.children || [];
			for (var i = 0, l = children.length; i < l; ++i) {
				var child = children[i];
				var navigableChild = this.getNavigableFromNode(child);
				this.close(navigableChild);
			}
		}
	});
				 
	function same(card1, card2) {
		var parents = [];
		return (card1.cardId == card2.cardId && card1.className === card2.className);
	}
})();
