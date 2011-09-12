Ext.override(Ext.selection.CheckboxModel, {
	bindComponent: function() {

		this.callOverridden(arguments);
		if (this.view) {
			this.mon(this.view.ownerCt, 'reconfigure', this.onGridReconfigure, this);
		}
	},

	onGridReconfigure: function() {
		_debug("********* reconfigure");
		var view = this.views[0],
			headerCt = view.headerCt;

		if (this.injectCheckbox !== false) {
			if (this.injectCheckbox == 'first') {
				this.injectCheckbox = 0;
			} else if (this.injectCheckbox == 'last') {
				this.injectCheckbox = headerCt.getColumnCount();
			}
			headerCt.add(this.injectCheckbox,  this.getHeaderConfig());
		}

		headerCt.on('headerclick', this.onHeaderClick, this);
	}
});

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
 * Grid scrollbars not working anymore with 4.0.2a (all right with 4.0.1).
 * Open a grid showing the scrollbar, go to a page without the scrollbar,
 * wait 30 seconds, go back to the page with the scrollbar: it will not
 * scroll the grid contents.
 * 
 * Should be fixed in 4.0.6, but we can't use it on an open source project,
 * so this is the workaround from gordonk66:
 * 
 * grid.on('scrollershow', function(scroller) {
 *     if (scroller && scroller.scrollEl) {
 *         scroller.clearManagedListeners(); 
 *         scroller.mon(scroller.scrollEl, 'scroll', scroller.onElScroll, scroller); 
 *     }
 * });
 * 
 * http://www.sencha.com/forum/showthread.php?137993-4.0.2-only-layout-fit-grid-scrollbar-when-used-does-not-scroll-content/page3
 */

Ext.grid.Panel.prototype.originalInitComponent = Ext.grid.Panel.prototype.initComponent;

Ext.grid.Panel.prototype.initComponent = function() {
	this.originalInitComponent(arguments);
	this.mon(this, 'scrollershow', function(scroller) {
		if (scroller && scroller.scrollEl) {
			scroller.clearManagedListeners(); 
			scroller.mon(scroller.scrollEl, 'scroll', scroller.onElScroll, scroller); 
		}
	});
}
