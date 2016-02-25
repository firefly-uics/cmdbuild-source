package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DOMAIN;
import static org.cmdbuild.service.rest.v2.constants.Serialization.RECURSION_ENABLED;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TARGET_CLASS;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newNode;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;

import java.util.ArrayList;
import java.util.Collection;

import org.cmdbuild.logic.NavigationTreeLogic;
import org.cmdbuild.logic.data.access.filter.json.JsonParser;
import org.cmdbuild.logic.data.access.filter.model.Element;
import org.cmdbuild.logic.data.access.filter.model.Filter;
import org.cmdbuild.logic.data.access.filter.model.Parser;
import org.cmdbuild.model.domainTree.DomainTreeNode;
import org.cmdbuild.service.rest.v2.DomainTrees;
import org.cmdbuild.service.rest.v2.cxf.filter.DomainTreeNodeElementPredicate;
import org.cmdbuild.service.rest.v2.model.Node;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CxfDomainTrees implements DomainTrees {

	private final ErrorHandler errorHandler;
	private final NavigationTreeLogic logic;

	public CxfDomainTrees(final ErrorHandler errorHandler, final NavigationTreeLogic logic) {
		this.errorHandler = errorHandler;
		this.logic = logic;
	}

	@Override
	public ResponseMultiple<String> readAll(final String filter, final Integer limit, final Integer offset) {
		final Predicate<DomainTreeNode> predicate;
		if (isNotBlank(filter)) {
			final Parser parser = new JsonParser(filter);
			final Filter filterModel = parser.parse();
			final Optional<Element> element = filterModel.attribute();
			if (element.isPresent()) {
				predicate = new DomainTreeNodeElementPredicate(element.get());
			} else {
				predicate = alwaysTrue();
			}
		} else {
			predicate = alwaysTrue();
		}
		final Collection<String> elements = logic.get(predicate).keySet();
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
										.withMetadata(DOMAIN, input.getDomainName()) //
										.withMetadata(TARGET_CLASS, input.getTargetClassName()) //
										.withMetadata(RECURSION_ENABLED, input.isEnableRecursion()) //
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
