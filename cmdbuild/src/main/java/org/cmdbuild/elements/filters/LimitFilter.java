package org.cmdbuild.elements.filters;

public class LimitFilter implements Cloneable {

	private static final long serialVersionUID = 1L;

	private Integer offset;
	private Integer limit;

	public LimitFilter(Integer offset, Integer limit){
		this.offset = offset;
		this.limit = limit;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (offset != null) {
			sb.append(" OFFSET ").append(offset);
		}
		if (limit != null) {
			sb.append(" LIMIT ").append(limit);
		}
		return sb.toString();
	}
	
	@Override
	public Object clone() {
		return new LimitFilter(this.offset,this.limit);
	}
}
