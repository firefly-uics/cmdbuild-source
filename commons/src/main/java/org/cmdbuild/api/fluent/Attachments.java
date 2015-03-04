package org.cmdbuild.api.fluent;

public interface Attachments {

	Iterable<AttachmentDescriptor> fetch();

	void upload(Attachment... attachments);

	SelectedAttachments selectByName(String... names);

	// TODO add later
	// SelectedAttachments selectByRegex(String regex);

	SelectedAttachments selectAll();

}
