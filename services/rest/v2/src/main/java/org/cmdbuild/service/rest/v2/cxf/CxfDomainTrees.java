package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newNode;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;

import java.util.ArrayList;
import java.util.Collection;

import org.cmdbuild.logic.NavigationTreeLogic;
import org.cmdbuild.model.domainTree.DomainTreeNode;
import org.cmdbuild.service.rest.v2.DomainTrees;
import org.cmdbuild.service.rest.v2.model.Node;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;

import com.google.common.base.Function;

public class CxfDomainTrees implements DomainTrees {

	private final ErrorHandler errorHandler;
	private final NavigationTreeLogic logic;

	public CxfDomainTrees(final ErrorHandler errorHandler, final NavigationTreeLogic logic) {
		this.errorHandler = errorHandler;
		this.logic = logic;
	}

	@Override
	public ResponseMultiple<String> readAll(final String filter, final Integer limit, final Integer offset) {
		final Collection<String> elements = logic.get().keySet();
		final int total = elements.size();
		return newResponseMultiple(String.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(total) //
						.build())
				.build();
	}

	@Override
	public ResponseMultiple<Node> read(final String id) {
		final DomainTreeNode lol = logic.getTree(id);
		if (lol == null) {
			errorHandler.domainTreeNotFound(id);
		}
		return newResponseMultiple(Node.class) //
				.withElements(from(flat(lol)) //
						.transform(new Function<DomainTreeNode, Node>() {

							@Override
							public Node apply(final DomainTreeNode input) {
								return newNode() //
										.withId(input.getId()) //
										.withParent(input.getIdParent()) //
										.withMetadata("domain", input.getDomainName()) //
										.withMetadata("targetClass", input.getTargetClassName()) //
										.withMetadata("recursionEnabled", input.isEnableRecursion()) //
										.build();
							}

						})) //
				.build();
	}

	private Iterable<DomainTreeNode> flat(final DomainTreeNode element) {
		final ArrayList<DomainTreeNode> elements = newArrayList();
		flat(elements, element);
		return elements;
	}

	/**
	 * Adds the specified element and invokes itself recursively for each child.
	 */
	private void flat(final Collection<DomainTreeNode> elements, final DomainTreeNode element) {
		final Collection<DomainTreeNode> children = element.getChildNodes();
		elements.add(element);
		for (final DomainTreeNode child : children) {
			flat(elements, child);
		}
	}

}
