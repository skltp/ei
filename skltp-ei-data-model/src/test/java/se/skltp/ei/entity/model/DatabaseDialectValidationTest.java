/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.entity.model;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseDialectValidationTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "org.hibernate.dialect.SQLServerDialect",
            "org.hibernate.dialect.PostgreSQLDialect",
            "org.hibernate.dialect.MySQLDialect",
            "org.hibernate.dialect.MariaDBDialect",
            "org.hibernate.dialect.H2Dialect"
    })
    void testEngagementEntityMetadataForDialect(String dialectClass) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, dialectClass)
                .build();

        try {
            MetadataSources sources = new MetadataSources(registry);
            sources.addAnnotatedClass(Engagement.class);

            MetadataImplementor metadata = (MetadataImplementor) sources.buildMetadata();
            assertNotNull(metadata, "Metadata should be built successfully for dialect: " + dialectClass);

            Database database = metadata.getDatabase();
            Table engagementTable = database.getDefaultNamespace().locateTable(Identifier.toIdentifier("engagement_index_table"));
            assertNotNull(engagementTable, "Table engagement_index_table should exist in metadata for dialect: " + dialectClass);

            // Verify primary key column
            Column idColumn = engagementTable.getColumn(Identifier.toIdentifier("id"));
            assertNotNull(idColumn, "Column 'id' should exist");
            assertTrue(idColumn.getSqlType(metadata) != null && !idColumn.getSqlType(metadata).isEmpty(),
                    "SQL type for 'id' must be resolved under " + dialectClass);

            // Verify search index columns
            Column residentIdColumn = engagementTable.getColumn(Identifier.toIdentifier("registered_resident_id"));
            assertNotNull(residentIdColumn, "Column 'registered_resident_id' should exist");

            Column serviceDomainColumn = engagementTable.getColumn(Identifier.toIdentifier("service_domain"));
            assertNotNull(serviceDomainColumn, "Column 'service_domain' should exist");

            Column categorizationColumn = engagementTable.getColumn(Identifier.toIdentifier("categorization"));
            assertNotNull(categorizationColumn, "Column 'categorization' should exist");

            // Verify temporal columns
            Column creationTimeColumn = engagementTable.getColumn(Identifier.toIdentifier("creation_time"));
            assertNotNull(creationTimeColumn, "Column 'creation_time' should exist");

            Column mostRecentContentColumn = engagementTable.getColumn(Identifier.toIdentifier("most_recent_content"));
            assertNotNull(mostRecentContentColumn, "Column 'most_recent_content' should exist");

        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
