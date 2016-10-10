(function() {
	Ext
			.define(
					'CMDBuild.view.management.classes.map.geoextension.Navigable',
					{
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
							if (!this.isANavigableClass(className)) {
								return null;
							}
							var cardId = card.cardId;
							if (!this.navigables[className][cardId]) {
								return null;
							}
							return this.navigables[className][cardId];
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
							this.toRefresh = false
							var previous = navigable.checked;
							navigable.checked = checked;
							var isBase = navigable.node.get("baseNode");
							if (previous !== checked) {
								this.toRefresh = true;
							}
							this.checkParentNodes(navigable, checked, false);
							var toExplode = (isBase === true);
							this.checkBrotherNodes(navigable, !toExplode);
							if (this.toRefresh) {
								this.interactionDocument.changedNavigables();
							}
						},
						checkParentNodes : function(navigable, checked, overBase) {
							var parentNode = navigable.node.parentNode;
							var parentNavigable = this.getNavigableFromNode(parentNode);
							if (parentNavigable) {
								var previous = parentNavigable.checked;
								parentNavigable.checked = checked;
								if (previous !== checked) {
									this.toRefresh = true;
								}
								var isBase = parentNode.get("baseNode");
								if (isBase) {
									this.checkParentNodes(parentNavigable, checked, true);
									this.checkBrotherNodes(parentNavigable, false);
								} else if (overBase) {
									this.checkParentNodes(parentNavigable, checked, true);
								} else {
									this.checkParentNodes(parentNavigable, checked, false);
									this.checkBrotherNodes(parentNavigable, true);
								}
							}
						},
						checkBrotherNodes : function(navigable, checked) {
							var parentNode = navigable.node.parentNode;
							var children = parentNode.childNodes || parentNode.children || [];
							for (var i = 0, l = children.length; i < l; ++i) {
								var child = children[i];
								if (!(child.get("className") === navigable.node.get("className") && child.get("cardId") === navigable.node
										.get("cardId"))) {
									var childNavigable = this.getNavigableFromNode(child);
									if (childNavigable) {
										childNavigable.checked = checked;
										this.checkNodeChildren(childNavigable, checked);
									}
								}

							}
						},
						checkNodeChildren : function checkNodeChildren(navigable, checked) {
							var children = navigable.node.childNodes || navigable.node.children || [];
							for (var i = 0, l = children.length; i < l; ++i) {
								var child = children[i];
								var childNavigable = this.getNavigableFromNode(child);
								if (childNavigable) {
									childNavigable.checked = checked;
									this.checkNodeChildren(childNavigable, checked);
								}
							}

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
					});
	function same(card1, card2) {
		return (card1.cardId == card2.cardId && card1.className === card2.className);
	}

})();
