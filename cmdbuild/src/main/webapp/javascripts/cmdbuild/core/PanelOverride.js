Ext.override(Ext.Panel, {
	callForeachItem: function(fnName) {
		var items = this.items.items;
		for (var i=0, l=items.length; i<l; ++i) {
			if (items[i][fnName]) {
				items[i][fnName]();
			}
		}
	}
});

/*
 * https://extjs.net/forum/showthread.php?t=48859&page=2
 * 
 * Stefano mystical code
 */
Ext.override(Ext.layout.BorderLayout, {
	onLayout : function(ct, target){
		var collapsed;
		var size = target.getViewSize(), w = size.width, h = size.height;
		if(!this.rendered){
			target.position();
			target.addClass('x-border-layout-ct');
			collapsed = [];
			var items = ct.items.items;
			for(var i = 0, len = items.length; i < len; i++) {
				var c = items[i];
				var pos = c.region;
				if(c.collapsed){
					collapsed.push(c);
				}
				c.collapsed = false;
				var r = this[pos] = pos != 'center' && c.split ?
					new Ext.layout.BorderLayout.SplitRegion(this, c.initialConfig, pos) :
					new Ext.layout.BorderLayout.Region(this, c.initialConfig, pos);
				if(pos == 'north' || pos == 'south'){
					if(typeof c.height == 'string' && c.relHeight === undefined){
						var p = c.height.match(/(\d+)%/);
						if(p[1]){
							c.relHeight = parseInt(p[1], 10) * .01;
						}
					}
					if(c.relHeight !== undefined){
						if(typeof c.relHeight != 'number'){
							c.relHeight = parseFloat(c.relHeight);
						}
						c.height = h * c.relHeight;
					}
					r.minSize = r.minSize || r.minHeight;
					r.maxSize = r.maxSize || r.maxHeight;
				} else if(pos == 'east' || pos == 'west'){
					if(typeof c.width == 'string' && c.relWidth === undefined){
						var p = c.width.match(/(\d+)%/);
						if(p[1]){
							c.relWidth = parseInt(p[1], 10) * .01;
						}
					}
					if(c.relWidth !== undefined){
						if(typeof c.relWidth != 'number'){
							c.relWidth = parseFloat(c.relWidth);
						}
						c.width = w * c.relWidth;
					}
					r.minSize = r.minSize || r.minWidth;
					r.maxSize = r.maxSize || r.maxWidth;
				}
				if(!c.rendered){
					c.cls = c.cls ? c.cls +' x-border-panel' : 'x-border-panel';
					c.render(target, i);
				}
				r.render(target, c);
			}
			this.rendered = true;
		}
		if(w < 20 || h < 20){ 
			if(collapsed){
				this.restoreCollapsed = collapsed;
			}
			return;
		}else if(this.restoreCollapsed){
			collapsed = this.restoreCollapsed;
			delete this.restoreCollapsed;
		}
		var centerW = w, centerH = h, centerY = 0, centerX = 0;
		var n = this.north, s = this.south, west = this.west, e = this.east, c = this.center;
		if(!c && Ext.layout.BorderLayout.WARN !== false){
			throw 'No center region defined in BorderLayout ' + ct.id;
		}
		if(n && n.isVisible()){
			var b = n.getSize();
			var m = n.getMargins();
			b.width = w - (m.left+m.right);
			if(n.panel.relHeight !== undefined){
				n.height = Math.round(h * n.panel.relHeight);
				b.height = n.minSize && n.height < n.minSize ? n.minSize :
					(n.maxSize && n.height > n.maxSize ? n.maxSize : n.height);
			}
			b.x = m.left;
			b.y = m.top;
			centerY = b.height + b.y + m.bottom;
			centerH -= centerY;
			n.applyLayout(b);
		}
		if(s && s.isVisible()){
			var b = s.getSize();
			var m = s.getMargins();
			b.width = w - (m.left+m.right);
			if(s.panel.relHeight !== undefined){
				s.height = Math.round(h * s.panel.relHeight);
				b.height = s.minSize && s.height < s.minSize ? s.minSize :
					(s.maxSize && s.height > s.maxSize ? s.maxSize : s.height);
			}
			b.x = m.left;
			var totalHeight = (b.height + m.top + m.bottom);
			b.y = h - totalHeight + m.top;
			centerH -= totalHeight;
			s.applyLayout(b);
		}
		if(west && west.isVisible()){
			var b = west.getSize();
			var m = west.getMargins();
			b.height = centerH - (m.top+m.bottom);
			if(west.panel.relWidth !== undefined){
				west.width = Math.round(w * west.panel.relWidth);
				b.width = west.minSize && west.width < west.minSize ? west.minSize :
					(west.maxSize && west.width > west.maxSize ? west.maxSize : west.width);
			}
			b.x = m.left;
			b.y = centerY + m.top;
			var totalWidth = (b.width + m.left + m.right);
			centerX += totalWidth;
			centerW -= totalWidth;
			west.applyLayout(b);
		}
		if(e && e.isVisible()){
			var b = e.getSize();
			var m = e.getMargins();
			b.height = centerH - (m.top+m.bottom);
			if(e.panel.relWidth !== undefined){
				e.width = Math.round(w * e.panel.relWidth);
				b.width = e.minSize && e.width < e.minSize ? e.minSize :
					(e.maxSize && e.width > e.maxSize ? e.maxSize : e.width);
			}
			var totalWidth = (b.width + m.left + m.right);
			b.x = w - totalWidth + m.left;
			b.y = centerY + m.top;
			centerW -= totalWidth;
			e.applyLayout(b);
		}
		if(c){
			var m = c.getMargins();
			var centerBox = {
				x: centerX + m.left,
				y: centerY + m.top,
				width: centerW - (m.left+m.right),
				height: centerH - (m.top+m.bottom)
			};
			c.applyLayout(centerBox);
		}
		if(collapsed){
			for(var i = 0, len = collapsed.length; i < len; i++){
				collapsed[i].collapse(false);
			}
		}
		if(Ext.isIE && Ext.isStrict){ 
			target.repaint();
		}
	}
});
Ext.override(Ext.layout.BorderLayout.SplitRegion, {
	onSplitMove : function(split, newSize){
		var s = this.panel.getSize();
		this.lastSplitSize = newSize;
		if(this.position == 'north' || this.position == 'south'){
			this.panel.setSize(s.width, newSize);
			if(this.panel.relHeight !== undefined){
				this.state.relHeight = this.panel.relHeight *= newSize / this.height;
			}else{
				this.state.height = newSize;
			}
		}else{
			this.panel.setSize(newSize, s.height);
			if(this.panel.relWidth !== undefined){
				this.state.relWidth = this.panel.relWidth *= newSize / this.width;
			}else{
				this.state.width = newSize;
			}
		}
		this.layout.layout();
		this.panel.saveState();
		return false;
	}
});
