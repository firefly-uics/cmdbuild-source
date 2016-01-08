(function($$) {
	'use strict';

	var STARTPHI = Math.PI / 7.0;
	var STEPRADIUS = 200;

	var defaults = {
		startAngle: 0
	};

	function CircleLayout(options) {
		//this.options = $$.util.extend({}, defaults, options);
	}

	CircleLayout.prototype.run = function() {
		var params = this.options;
		var options = params;
		var cy = params.cy;
		var eles = options.eles;
		var nodes = eles.nodes().not(':parent');
//		var bb = $$.util.makeBoundingBox({
//			x1: 0,
//			y1: 0,
//			w: cy.width(),
//			h: cy.height()
//		});

		var center = {
			x: 0 + cy.width() / 2,
			y: 0  + cy.height() / 2,
			z: 0
		};
		this.clean = function() {
			nodes[0].position(center);
			$.Cmdbuild.g3d.Model.removeGraphData(nodes, "moved");
		};
		this.initialAngle = function(nChildren) {
			return {
				theta: 0,
				phi: 0 + STARTPHI,
				gamma: 0,
				thetaSteps: parseInt(nChildren / 5) + 1,
				phiSteps: 5,
				gammaSteps: 0,
				dTheta: (2 * Math.PI) / (parseInt(nChildren / 5) + 1),
				dPhi: (Math.PI) / 7,
				dGamma: 0
			};
		};
		this.initialLittleAngle = function(nChildren, nChildrenWithChildren, isRoot) {
			return {
				theta: 0,
				phi: 0 + STARTPHI,
				gamma: 0,
				thetaSteps: parseInt(nChildren / 5) + 1,
				phiSteps: 5,
				gammaSteps: nChildrenWithChildren,
				dTheta: (isRoot) ? (2 * Math.PI) / (parseInt(nChildren / 5) + 1) : 0,
				dPhi: (isRoot) ? (Math.PI) / 7 : 0,
				dGamma: (isRoot || ! nChildrenWithChildren) ? 0 : (2 * Math.PI) / nChildrenWithChildren
			};
		};
		this.getActualAngle = function(angle, index) {
			var thetaIndex = (angle.thetaSteps) ? parseInt(index % angle.thetaSteps) : index;
			var phiIndex = parseInt(index % angle.phiSteps);
			return {
				theta: angle.theta + angle.dTheta * thetaIndex,
				phi: angle.phi + angle.dPhi * phiIndex,
				gamma: 0
			};
		};
		this.getActualLittleAngle = function(angle, indexAngle, indexLittleAngle, isRoot) {
			var thetaIndex = (angle.thetaSteps) ? parseInt(indexAngle % angle.thetaSteps) : 0;
			var phiIndex = parseInt(indexAngle % angle.phiSteps);
			var gammaIndex = indexLittleAngle;
			return {
				theta: (isRoot)
						? angle.theta + angle.dTheta * thetaIndex
						: Math.PI / 4,
				phi: (isRoot) ? angle.phi + angle.dPhi * phiIndex : 0,
				gamma: (isRoot) ? 0 : angle.gamma + angle.dGamma * gammaIndex
			};
		};
		
		this.getNodeQuaternion = function(angle) {
			var quaternion = new THREE.Quaternion();
			var order = (angle.gamma) ? "ZYX" : "ZYX";
			var euler = new THREE.Euler(0, angle.phi, angle.theta,
					order);
			quaternion.setFromEuler(euler);
			var quaternionLilAngle = new THREE.Quaternion();
			quaternionLilAngle.setFromAxisAngle(new THREE.Vector3(0, 0, 1), angle.gamma);
			return quaternion;
		};
		this.openChildren = function(node, isRoot) {
			$.Cmdbuild.g3d.Model.setGraphData(node, "justOpen", true);
			var openChildren = $.Cmdbuild.g3d.Model.getGraphData(node, "children");
			var children = getNodesById(openChildren);
			var lChildrenWithChildren = lengthChildrenWithChildren(children);
			var angle = this.initialAngle(children.length);
			var indexWithChildren = 0;
			var littleAngle = this.initialLittleAngle(children.length, lChildrenWithChildren,
					isRoot);
			for (var i = 0; i < children.length; i++) {
				var areChildren = withChildren(children[i]);
				if ($.Cmdbuild.g3d.Model.getGraphData(children[i], "moved")) {
					if (areChildren) {
						indexWithChildren++;
					}
					continue;
				}
				var r = (!areChildren) ? STEPRADIUS : STEPRADIUS * 2;
				var rLilAngle = (areChildren && !isRoot) ? STEPRADIUS : 0;
				var vectorPosition = new THREE.Vector3(0, rLilAngle, r);
				var q;
				$.Cmdbuild.g3d.Model.setGraphData(children[i], "isRoot", isRoot);
				if (areChildren && !isRoot) {
					q = this.getNodeQuaternion(this.getActualLittleAngle(
							littleAngle, i, indexWithChildren, isRoot));
					var lilAngle = this.getActualLittleAngle(littleAngle, i,
							indexWithChildren, isRoot);
					var euler = new THREE.Euler(0, 0, lilAngle.gamma, "XYZ");
					var qLilAngle = new THREE.Quaternion();
					qLilAngle.setFromEuler(euler);
					qLilAngle.multiply(q);
					vectorPosition.applyQuaternion(qLilAngle);
					$.Cmdbuild.g3d.Model.setGraphData(children[i], "quaternion", qLilAngle)
					
				} else  {
					q = this.getNodeQuaternion(this.getActualAngle(angle, i));
					vectorPosition.applyQuaternion(q);
					$.Cmdbuild.g3d.Model.setGraphData(children[i], "quaternion", q);
				}
				var composedPosition = composeQuaternions(node, vectorPosition);
				children[i].position({
					x: composedPosition.x,
					y: composedPosition.y,
					z: composedPosition.z
				});
				if (areChildren) {
					indexWithChildren++;
					this.openChildren(children[i], false);
				}
			}
		};
		this.layoutPositions = function(nodes) {
			//nodes[0].position(center);
			for (var i = 0; i < nodes.length; i++) {
				if (! $.Cmdbuild.g3d.Model.getGraphData(nodes[i], "previousPathNode") || 
						$.Cmdbuild.g3d.Model.getGraphData(nodes[i], "moved") === true) {
					this.openChildren(nodes[i], true);
				}
			}
			return nodes; // chaining
		};

		this.layoutPositions(nodes);

		return this; // chaining
	};

	$$('layout', 'guisphere', CircleLayout);

	function withChildren(node) {
		var chs = $.Cmdbuild.g3d.Model.getGraphData(node, "children");
		return (chs != undefined && chs.length > 0);
	};
	function composeQuaternions(node, vector) {
		while (node) {
			if (! $.Cmdbuild.g3d.Model.getGraphData(node, "previousPathNode") || 
					$.Cmdbuild.g3d.Model.getGraphData(node, "moved") === true) {
				vector.add(node.position());
				break;
			}
			else {
				var q = $.Cmdbuild.g3d.Model.getGraphData(node, "quaternion");
				var rLilAngle = ($.Cmdbuild.g3d.Model.getGraphData(node, "isRoot") === true) ? 0 : STEPRADIUS;
				vector.add(new THREE.Vector3(0, rLilAngle, STEPRADIUS * 2));
				vector.applyQuaternion(q);
			}
			var parentId = $.Cmdbuild.g3d.Model.getGraphData(node, "previousPathNode");
			node = cy.getElementById(parentId);
		}
		return vector;
	};
	function getChildrenByFunct(children, f) {
		var children2Return = [];
		if (!children) {
			return children2Return;
		}
		for (var i = 0; i < children.length; i++) {
			var element = children[i];
			if (f(element)) {
				children2Return.push(element);
			}
		}
		return children2Return;

	};
	function lengthChildrenWithChildren(children) {
		var f = function(element) {
			return withChildren(element);
		};
		return getChildrenByFunct(children, f).length;
	};
	function lengthChildrenWithoutChildren(children) {
		var f = function(element) {
			return !withChildren(element);
		};
		return getChildrenByFunct(children, f).length;
	};
	function getNodesById(children) {
		if (!children) {
			return [];
		}
		var arChildren = [];
		for (var i = 0; i < children.length; i++) {
			arChildren.push(cy.getElementById(children[i]));
		}
		return arChildren;
	};
})(cytoscape);