package org.cmdbuild.dao.driver.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

public class EntryTypeCommands {

	private final JdbcTemplate jdbcTemplate;

	EntryTypeCommands(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<DBClass> findAllClasses() {
		final ClassTreeBuilder rch = new ClassTreeBuilder();
		jdbcTemplate.query("SELECT table_id, _cm_cmtable(table_id) AS table_name, _cm_parent_id(table_id) AS parent_id FROM _cm_class_list() AS table_id", rch);
		return rch.getResult();
	}

	private class ClassTreeBuilder implements RowCallbackHandler {

		private class ClassAndParent {
			public final DBClass dbClass;
			public final Object parentId;

			ClassAndParent(final DBClass dbClass, final Object parentId) {
				this.dbClass = dbClass;
				this.parentId = parentId;
			}
		}

		private final Map<Object, ClassAndParent> classMap = new HashMap<Object, ClassAndParent>();

		@Override
		public void processRow(ResultSet rs) throws SQLException {
        	final Long id = (Long) rs.getLong("table_id");
            final String name = rs.getString("table_name");
            final Long parentId = (Long) rs.getObject("parent_id");
            final List<DBAttribute> attributes = findEntryTypeAttributes(id);
            final DBClass dbClass = new DBClass(name, id, attributes);
            classMap.put(id, new ClassAndParent(dbClass, parentId));
		}

		public List<DBClass> getResult() {
			return linkClasses();
		}

		private List<DBClass> linkClasses() {
			final List<DBClass> allClasses = new ArrayList<DBClass>();
			for (final ClassAndParent cap : classMap.values()) {
				final DBClass child = cap.dbClass;
				if (cap.parentId != null) {
					final DBClass parent = classMap.get(cap.parentId).dbClass;
					child.setParent(parent);
				}
				allClasses.add(child);
			}
			return allClasses;
		}
	}

	private List<DBAttribute> findEntryTypeAttributes(final long entryTypeId) {
		final List<DBAttribute> entityTypeAttributes = jdbcTemplate.query(
				"SELECT attribute_name FROM _cm_attribute_list(?) AS attribute_name",
				new Object[] { entryTypeId },
		        new RowMapper<DBAttribute>() {
		            public DBAttribute mapRow(ResultSet rs, int rowNum) throws SQLException {
		                final String name = rs.getString("attribute_name");
		                return new DBAttribute(name);
		            }
		        });
		return entityTypeAttributes;
	}

	public DBClass createClass(final String name, final DBClass parent) {
		final String parentName = (parent != null) ? parent.getName() : null;
		final long id = jdbcTemplate.queryForInt(
				"SELECT cm_create_class(?, ?, ?)",
				new Object[] { name, parentName, createClassComment(name) }
			);
		final List<DBAttribute> attributes = findEntryTypeAttributes(id);
		final DBClass newClass = new DBClass(name, id, attributes);
		newClass.setParent(parent);
		return newClass;
	}

	private String createClassComment(final String name) {
		return String.format("DESCR: %s|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class", name);
	}

	public void deleteClass(final DBClass dbClass) {
		jdbcTemplate.queryForObject(
				"SELECT cm_delete_class(?)",
				Object.class,
				new Object[]{ dbClass.getName() }
			);
		dbClass.setParent(null);
	}
}
