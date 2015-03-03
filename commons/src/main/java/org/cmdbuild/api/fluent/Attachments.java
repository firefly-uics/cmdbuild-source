package org.cmdbuild.api.fluent;

public interface Attachments {

	Iterable<AttachmentDescriptor> fetch();

	SelectedAttachments selectByName(String... names);

	// TODO add later
	// SelectedAttachments selectByRegex(String regex);

	SelectedAttachments selectAll();

	void upload(Attachment... attachments);

}
