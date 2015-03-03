package org.cmdbuild.api.fluent;

public interface SelectedAttachments {

	Iterable<AttachmentDescriptor> selected();

	Iterable<Attachment> download();

	void copyTo(CardDescriptor destination);

	void copyTo(ProcessInstanceDescriptor destination);

	void moveTo(CardDescriptor destination);

	void moveTo(ProcessInstanceDescriptor destination);

	void delete();

}
