package org.cmdbuild.legacy.dms;

import java.util.Comparator;

public class AttachmentBeanComparator implements Comparator<AttachmentBean>{
	
	public int compare(AttachmentBean arg0, AttachmentBean arg1) {
		return arg0.getName().compareTo(arg1.getName());
	}

}
