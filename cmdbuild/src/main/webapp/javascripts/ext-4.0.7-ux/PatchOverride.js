Ext.override(Ext.slider.Multi, {
    onDisable: function() {
        var me = this,
            i = 0,
            thumbs = me.thumbs,
            len = thumbs.length,
            thumb,
            el,
            xy;

        me.callParent();

        for (; i < len; i++) {
            thumb = thumbs[i];
            el = thumb.el;

            thumb.disable();

            if(Ext.isIE && el) { // [Fix] el is not there on IE9!
                //IE breaks when using overflow visible and opacity other than 1.
                //Create a place holder for the thumb and display it.
                xy = el.getXY();
                el.hide();

                me.innerEl.addCls(me.disabledCls).dom.disabled = true;

                if (!me.thumbHolder) {
                    me.thumbHolder = me.endEl.createChild({cls: Ext.baseCSSPrefix + 'slider-thumb ' + me.disabledCls});
                }

                me.thumbHolder.show().setXY(xy);
            }
        }
    }
});

/*
 * The date field return null if is empty, so the form does not
 * send anything. The hack is to return "" if the field is empty
 */

Ext.override(Ext.form.field.Date, {
	getSubmitValue: function() {
		var me = this,
		format = me.submitFormat || me.format,
		value = me.getValue();

		return value ? Ext.Date.format(value, format) : "";
	}
});

Ext.override(Ext.menu.Menu, {
	doConstrain : function() {
		var me = this,
			y = me.el.getY(),
			max, full,
			vector,
			returnY = y, normalY, parentEl, scrollTop, viewHeight;

		delete me.height;
		// first set "full" and then reset the size
		full = me.getHeight();
		me.setSize();
		if (me.floating) {
			parentEl = Ext.fly(me.el.dom.parentNode);
			scrollTop = parentEl.getScroll().top;
			viewHeight = parentEl.getViewSize().height;
			// Normalize y by the scroll position for the parent
			// element. Need to move it into the coordinate
			// space
			// of the view.
			normalY = y - scrollTop;
			max = me.maxHeight ? me.maxHeight : viewHeight
					- normalY;

			if (full > viewHeight) {
				max = viewHeight;
				// Set returnY equal to (0,0) in view space by
				// reducing y by the value of normalY
				returnY = y - normalY;
			} else if (max < full) {
				returnY = y - (full - max);
				max = full;
			}

		} else {
			max = me.getHeight();
		}
		// Always respect maxHeight
		if (me.maxHeight) {
			max = Math.min(me.maxHeight, max);
		}
		if (full > max && max > 0) {
			me.layout.autoSize = false;
			me.setHeight(max);
			if (me.showSeparator) {
				me.iconSepEl.setHeight(me.layout
						.getRenderTarget().dom.scrollHeight);
			}
		}
		vector = me.getConstrainVector(me.el.dom.parentNode);
		if (vector) {
			me.setPosition(me.getPosition()[0] + vector[0]);
		}
		me.el.setY(returnY);
	}
});