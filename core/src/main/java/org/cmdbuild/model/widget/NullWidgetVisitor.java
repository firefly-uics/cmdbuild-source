package org.cmdbuild.model.widget;

public class NullWidgetVisitor implements WidgetVisitor {

	private static final NullWidgetVisitor INSTANCE = new NullWidgetVisitor();

	public static NullWidgetVisitor getInstance() {
		return INSTANCE;
	}

	private NullWidgetVisitor() {
		// use factory method
	}

	@Override
	public void visit(final Calendar widget) {
	}

	@Override
	public void visit(final CreateModifyCard widget) {
	}

	@Override
	public void visit(final LinkCards widget) {
	}

	@Override
	public void visit(final ManageEmail widget) {
	}

	@Override
	public void visit(final ManageRelation widget) {
	}

	@Override
	public void visit(final OpenAttachment widget) {
	}

	@Override
	public void visit(final OpenNote widget) {
	}

	@Override
	public void visit(final OpenReport widget) {
	}

	@Override
	public void visit(final Ping widget) {
	}

	@Override
	public void visit(final WebService widget) {
	}

	@Override
	public void visit(final PresetFromCard widget) {
	}

	@Override
	public void visit(final Workflow widget) {
	}

	@Override
	public void visit(final NavigationTree widget) {
	}

	@Override
	public void visit(final Grid widget) {
	}

}
