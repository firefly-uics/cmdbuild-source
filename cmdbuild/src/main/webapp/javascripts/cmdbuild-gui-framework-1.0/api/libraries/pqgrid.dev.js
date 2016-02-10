/**
 * ParamQuery Grid a.k.a. pqGrid v1.1.3
 * 
 * Copyright (c) 2012-2013 Paramvir Dhindsa
 * Released under GPL v3 license
 * http://paramquery.com/license
 * 
 */     	
(function($){
	"use strict";
	$.paramquery=($.paramquery==null)?{}:$.paramquery;	
	$.paramquery.xmlToArray=function(data,obj){
		var itemParent=obj.itemParent;
		var itemNames=obj.itemNames;
		var arr=[];
		var $items=$(data).find(itemParent);
		$items.each(function(i,item){
			var $item=$(item);
			var arr2=[];
			$(itemNames).each(function(j,itemName){
				arr2.push($item.find(itemName).text());	
			});
			arr.push(arr2);
		});
		return arr;
	};		
	$.paramquery.tableToArray=function(tbl){
		var $tbl=$(tbl);
		var colModel=[];
		var data=[];
		var cols=[];
		var widths=[];
		var $trfirst=$tbl.find("tr:first");
		var $trsecond=$tbl.find("tr:eq(1)");
		$trfirst.find("th,td").each(function(i,td){
			var $td=$(td);
			var title=$td.html();
			var width=$td.width();
			var dataType="string";
			var $tdsec=$trsecond.find("td:eq("+i+")");
			var val=$tdsec.text();
			var align=$tdsec.attr("align");
			val=val.replace(/,/g,"");
			if(parseInt(val)==val && (parseInt(val)+"").length == val.length){
				dataType="integer";
			}
			else if(parseFloat(val)==val ){
				dataType="float";
			}
			var obj={title:title,width:width,dataType:dataType,align:align,dataIndx:i};
			colModel.push(obj);
		});
		$tbl.find("tr").each(function(i,tr){
			if(i==0)return;
			var $tr=$(tr);
			var arr2=[];
			$tr.find("td").each(function(j,td){
				arr2.push($.trim($(td).html()));
			});
			data.push(arr2);
		});
		return {data:data,colModel:colModel};
	};
	$.paramquery.formatCurrency=function(val) {
        val = Math.round(val * 10) / 10;
        val = val + "";
        if (val.indexOf(".") == -1) {
            val = val + ".0";
        }
		var len = val.length;
		var fp=val.substring(0,len-2),
			lp=val.substring(len-2,len),
        	arr=fp.match(/\d/g).reverse(),
			arr2=[];
		for(var i=0;i<arr.length;i++){
			if(i>0 && i%3==0){
				arr2.push(",");	
			}
			arr2.push(arr[i]);
		}	
		arr2=arr2.reverse();
		fp=arr2.join("");
		return fp+lp;
    };	
})(jQuery);
/**
 * ParamQuery Pager a.k.a. pqPager
 */     	
(function($){
	"use strict";
var fnPG={};
fnPG.options={
	currentPage:0,
	totalPages:0,
	totalRecords:0,
	msg:"",
	rPPOptions:[10,20,30,40,50,100],
	rPP:20		
};
fnPG._regional={
	strPage:"Page {0} of {1}",
	strFirstPage:"First Page",
	strPrevPage:"Previous Page",
	strNextPage:"Next Page",
	strLastPage:"Last Page",
	strRefresh:"Refresh",	
	strRpp:"Records per page:",
	strDisplay:"Displaying {0} to {1} of {2} items."	
};
$.extend(fnPG.options,fnPG._regional);
fnPG._create=function(){
	var that=this,
		thisOptions=this.options;
	this.element.addClass("pq-pager").css({});
	this.first = $( "<button type='button' title='"+this.options.strFirstPage+"'></button>", {
	})
	.appendTo( this.element )
	.button({
		icons: {
			primary:"pq-page-first"
		},text:false
	}).bind("click.paramquery",function(evt){
		if(that.options.currentPage>1){
			if ( that._trigger( "change", evt, {
				curPage: 1
			} ) !== false ) {
				that.option( {currentPage:1} );
			}
		}					
	});
	this.prev=$( "<button type='button' title='"+this.options.strPrevPage+"'></button>")
	.appendTo( this.element )
	.button({icons:{primary:"pq-page-prev"},text:false}).bind("click",function(evt){
		if(that.options.currentPage>1){
			var currentPage=that.options.currentPage-1;
			if ( that._trigger( "change", evt,{
				curPage: currentPage
			} ) !== false ) {
				that.option( {currentPage:currentPage} );
			}						
		}
	});
	$("<span class='pq-separator'></span>").appendTo(this.element);
	this.pagePlaceHolder=$("<span class='pq-pageholder'></span>")
	.appendTo(this.element);
	$("<span class='pq-separator'></span>").appendTo(this.element);
	this.next=$( "<button type='button' title='"+this.options.strNextPage+"'></button>")
	.appendTo( this.element )
	.button({icons:{primary:"pq-page-next"},text:false}).bind("click",function(evt){
		var val=that.options.currentPage+1;
		if ( that._trigger( "change", evt, {curPage: val} ) !== false ) {			
			that.option( {currentPage:val} );
		}				
	});
	this.last=$( "<button type='button' title='"+this.options.strLastPage+"'></button>")
	.appendTo( this.element )
	.button({icons:{primary:"pq-page-last"},text:false}).bind("click",function(evt){
		var val=that.options.totalPages;
		if ( that._trigger( "change", evt, {curPage: val} ) !== false ) {			
			that.option( {currentPage:val} );
		}									
	});
	$("<span class='pq-separator'></span>").appendTo(this.element);
	this.$strRpp = $("<span>"+this.options.strRpp+" </span>")
	.appendTo(this.element);
	this.$rPP=$("<select></select>")
	.appendTo(this.element)
	.change(function(evt){
		var val=$(this).val();
		if (that._trigger("change", evt,{rPP: val}) !== false) {
			that.options.rPP=val;
		}
	});
	$("<span class='pq-separator'></span>").appendTo(this.element);
	this.$refresh=$("<button type='button' title='"+this.options.strRefresh+"'></button>")
	.appendTo(this.element)
	.button({icons:{primary:"pq-refresh"},text:false}).bind("click",function(evt){		
		if ( that._trigger( "refresh", evt ) !== false ) {			
		}				
	});
	$("<span class='pq-separator'></span>").appendTo(this.element);
	this.$msg=$("<span class='pq-pager-msg'></span>")
	.appendTo( this.element );
	this._refresh();
};
fnPG._refreshPage=function(){
	var that=this;
	this.pagePlaceHolder.empty();
	var strPage=this.options.strPage;
	var arr=strPage.split(" ");
	var str="";
	$(arr).each(function(i,ele){
		str+="<span>"+ele+"</span>";
	});
	strPage=str.replace("<span>{0}</span>","<span class='textbox'></span>");
	strPage=strPage.replace("<span>{1}</span>", "<span class='total'></span>");	
	var $temp=$( strPage ).appendTo(this.pagePlaceHolder);
	this.page=$( "<input type='text' tabindex='0' />")
		.replaceAll("span.textbox", $temp)
	.bind("change",function(evt){
		var $this=$(this);
		var val=$this.val();
		if(isNaN(val)||val<1){
			$this.val(that.options.currentPage);
			return false;
		}
		val=parseInt(val);
		if(val>that.options.totalPages){
			$this.val(that.options.currentPage);
			return false;						
		}
		if ( that._trigger( "change", evt, {
			curPage: val
		}) !== false ) {
			that.option( {currentPage:val} );
		}				
		else{
			$this.val(that.options.currentPage);
			return false;												
		}				
	});
	this.$total=$temp.filter("span.total");
};
fnPG._refresh=function(){
	this._refreshPage();
	var sel=(this.$rPP);
	var thisOptions=this.options;
	this.$strRpp.text(thisOptions.strRpp);
	this.first.attr("title",thisOptions.strFirstPage);
	this.prev.attr("title",thisOptions.strPrevPage);
	this.next.attr("title",thisOptions.strNextPage);
	this.last.attr("title",thisOptions.strLastPage);
	this.$refresh.attr("title",thisOptions.strRefresh);
	sel.empty();
	var opts = this.options.rPPOptions;
	for(var i=0;i<opts.length;i++){
        var opt=document.createElement("option");
        opt.text=opts[i];
        opt.value=opts[i];
        opt.setAttribute("value",opts[i]);
        opt.innerHTML=opts[i];
		sel.append(opt);
	}				
	sel.find("option[value="+this.options.rPP+"]").attr("selected",true);
	if(this.options.currentPage>=this.options.totalPages){
		this.next.button({disabled:true});
		this.last.button({disabled:true});
	}
	else{
		this.next.button({disabled:false});
		this.last.button({disabled:false});					
	}
	if(this.options.currentPage<=1){
		this.first.button({disabled:true});
		this.prev.button({disabled:true});
	}		
	else{
		this.first.button({disabled:false});
		this.prev.button({disabled:false});					
	}
	this.page.val(this.options.currentPage);
	this.$total.text(this.options.totalPages);		
	if(this.options.totalRecords>0){
		var rPP = this.options.rPP;
		var currentPage = this.options.currentPage;
		var totalRecords = this.options.totalRecords;
		var begIndx = (currentPage-1)*rPP;
		var endIndx = currentPage*rPP;
		if(endIndx>totalRecords){
			endIndx = totalRecords;
		}
		var strDisplay=this.options.strDisplay;
		strDisplay=strDisplay.replace("{0}",begIndx+1);
		strDisplay=strDisplay.replace("{1}",endIndx);
		strDisplay=strDisplay.replace("{2}",totalRecords);
		this.$msg.html(strDisplay);			
	}
	else{
		this.$msg.html("");
	}
};
fnPG._destroy=function(){
	this.element.empty().removeClass("pq-pager").enableSelection();
};
fnPG._setOption=function(key,value){
	if(key=="currentPage"||key=="totalPages")value=parseInt(value);	
	$.Widget.prototype._setOption.call( this, key, value );				
};
fnPG._setOptions=function(){
	$.Widget.prototype._setOptions.apply( this, arguments );
	this._refresh();				
};
	$.widget("paramquery.pqPager",fnPG);
	$.paramquery.pqPager.regional={};
	$.paramquery.pqPager.regional['en']=fnPG._regional;
	$.paramquery.pqPager.setDefaults=function(obj){
		for(var key in obj){
			fnPG.options[key]=obj[key];
		}
		$.widget("paramquery.pqPager",fnPG);
		$(".pq-pager").each(function(i,pager){
			$(pager).pqPager("option",obj);
		});
	};		
})(jQuery);
/**
	direction:'vertical'
		ele.html("<div class='left-btn pq-sb-btn'></div>\
				var new_top = clickY-top_this;						
				that.$slider.css("top",clickY-top_this-that.$slider.height());
			return this;
	}			
		this._setSliderBgLength();
		this.scroll_space =this.length-34-this.slider_length;
		this._setSliderBgLength();
	}
	}
			that._updateCurPosAndTrigger(evt,top);
	}
                curPage: that.dataModel.curPage
        var tblClass = 'pq-grid-table ';
                buffer.push("<td style='width:" + wd + "px;' ></td>");
                    continue;
                buffer.push("<td style='width:" + wd + "px;' pq-top-col-indx=" + col + "></td>");
                buffer.push("<td style='width:" + wd + "px;'></td>");
                if (row == finalRow) {
                        that.tables.push({$tbl: $tbl, cont: objP.$cont[0]});
                customData = objP.customData;
                dataModel: that.dataModel,
        var cls = "pq-td-div";
        var that = this.that,
        }
                continue;
            if (column.align == "right") {
            if (column.align == "right") {
        this.isDirty = false;
            var indx = this.indexOf(objP);
        }
        for (var i = 0, len = data.length; i < len; i++) {
            var indx = this.indexOf(objP);
            var $td = that.getCell({rowIndxPage: rowIndxPage, colIndx: colIndx});
        }
        minWidth: 50,
        this.element.empty(); 
        this.element.css('height', "");
        for (var i = 1; i < $tds.length; i++) {
        this._refreshHeader();
        this._refreshWidths();
        this.element.empty().addClass('pq-grid ui-widget ui-widget-content' + (this.options.roundCorners ? ' ui-corner-all' : ''))
        if (!this.options.bottomVisible) {
        this.$cont_o = $("div.pq-cont-right", this.$grid_right);
                }, 'scrollBar setNumEles stuff')
                    that._refreshHeaderSortIcons();
        if (window.opera) {
            var colIndx = this.getColIndxFromDataIndx(DM.sortIndx);
        }
        this._createSelectedRowsObject();
        if (evt.wheelDelta) {
            num = evt.detail * -1 / 3;
        var objP = that.getCellIndices($td);
            }
                $td = that._bringCellIntoView({rowIndxPage: rowIndxPage, colIndx: colIndx});
                if (obj.curPage != undefined) {
            this._refreshDataFromDataModel();
                if (that.colModel[i].hidden) {
        }
            if (DM.curPage > DM.totalPages) {
            }
            this.xhr.abort();
                    DM.data = retObj.data;
                        that._refreshSortingDataAndView({sorting: true});
        }, '_generateTables');
        this._setScrollVNumEles(true);
        })
        this._setScrollVNumEles(true);
        this._refreshPager();
            this.sCells.removeAll({raiseEvent: true});
            } else {
            $.Widget.prototype._setOption.call(this, key, value);
            this._refreshWidths();
        }
        }
            this.refreshRequired = false;
            this.generateLoading();
        }
        if (this.refreshRequired) {
            this._refresh(); 
        }
            var ht = $tr[0].offsetHeight - 4;
            this.$div_focus = $("<div class='pq-cell-selected-border'></div>")
            this.$div_focus.css({
                var $trs = this.$tbl.children().children("tr");
            var cur_pos = colIndx - this.freezeCols - this._calcNumHiddenUnFrozens(colIndx);
                var $tds = $td.parent("tr").children("td");
        if (type == 'row') {
        }
                obj.data = this.data;
            this._setGridFocus();
            this._removeCellRowOutline();
            }
            return false;
                $cell.children().focus();
            var dataCell = column.getEditCellData({
        var rowIndxPage = parseInt($tr.attr("pq-row-indx"));
    }
    }
                var obj;
                if (obj == null) {
        }
                    rowIndx = obj.rowIndx, rowIndxPage = rowIndx - offset;
                        rowIndx = obj.rowIndx,
                    return;
        for (var i = this.freezeCols; i < len; i++) {
        this.$hscroll.pqScrollBar("option", "length", wd);
            htSB = 0;
    };
        }
            cols++;
        $pQuery_drag.draggable("option", 'containment', [cont_left, 0, cont_right, 0]);
            }
                }
                }
                    }
                        k++;
                }
            str += "<tr>";
            if (this.numberCell) {
                    continue;
                    colIndx = "pq-grid-col-indx='" + col + "'";
            }
            }
            });
                that._getDragHelper(evt, ui);
                for (var i = 0; i < that.tables.length; i++) {
        }
                continue;
            if (column.align == "right") {
            if (column.align == "right") {
                    var $td = $tr.find("td[pq-dataIndx='" + dataIndx + "']");
                if (column.align == "right") {
                if (column.align == "right") {
                var val2 = (obj2[dataIndx] + "").replace(/,/g, "");
    }