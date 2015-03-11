package se.skltp.ei.scale

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import groovy.sql.Sql
import java.util.Random
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.FileSystems
import java.nio.file.StandardCopyOption

/**
 * Generate test data for Engagement Index Update, based on a test EI database with 10 million rows.
 * Input database can be running locally.
 * xml test data is written to data folder.
 * Files can be pasted into SoapUI.
 * SoapUI can be run against Engagement Index in skltp box environment.
 * 
 * @author Martin Flower
 */
class Generator {

    // --- --------------------------------------------------------------------
        
    void generateTestFiles() {

        // registered resident id - occurrences in database (10 million)
        //   19%119 - 4138
        //   19%219 - 4138
        //   19%319 - 4124
        //   19%419 - 4138
        //   19%519 - 4138
        
        int messageSize = 1000
        
        // --- test1 - new
        writeXmlFromSql (new File('data/test1.update1.xml'), eiDB, "select * from engagement_index_table where registered_resident_id like '19%119' limit ${messageSize}")
        writeXmlFromSql (new File('data/test1.update2.xml'), eiDB, "select * from engagement_index_table where registered_resident_id like '19%219' limit ${messageSize}")
        writeXmlFromSql (new File('data/test1.update3.xml'), eiDB, "select * from engagement_index_table where registered_resident_id like '19%319' limit ${messageSize}")
        writeXmlFromSql (new File('data/test1.update4.xml'), eiDB, "select * from engagement_index_table where registered_resident_id like '19%419' limit ${messageSize}")
        writeXmlFromSql (new File('data/test1.update5.xml'), eiDB, "select * from engagement_index_table where registered_resident_id like '19%519' limit ${messageSize}")
        
        // --- test2 - delete
        writeXmlFromSql (new File('data/test2.delete1.xml'), eiDB, "select * from engagement_index_table where registered_resident_id like '19%119' limit ${messageSize}", true)
        writeXmlFromSql (new File('data/test2.delete2.xml'), eiDB, "select * from engagement_index_table where registered_resident_id like '19%219' limit ${messageSize}", true)
        writeXmlFromSql (new File('data/test2.delete3.xml'), eiDB, "select * from engagement_index_table where registered_resident_id like '19%319' limit ${messageSize}", true)
        writeXmlFromSql (new File('data/test2.delete4.xml'), eiDB, "select * from engagement_index_table where registered_resident_id like '19%419' limit ${messageSize}", true)
        writeXmlFromSql (new File('data/test2.delete5.xml'), eiDB, "select * from engagement_index_table where registered_resident_id like '19%519' limit ${messageSize}", true)
        
        // --- test3 - recreate
        Files.copy(Paths.get('data/test1.update1.xml'), Paths.get('data/test3.recreate1.xml'), StandardCopyOption.REPLACE_EXISTING )
        Files.copy(Paths.get('data/test1.update2.xml'), Paths.get('data/test3.recreate2.xml'), StandardCopyOption.REPLACE_EXISTING )
        Files.copy(Paths.get('data/test1.update3.xml'), Paths.get('data/test3.recreate3.xml'), StandardCopyOption.REPLACE_EXISTING )
        Files.copy(Paths.get('data/test1.update4.xml'), Paths.get('data/test3.recreate4.xml'), StandardCopyOption.REPLACE_EXISTING )
        Files.copy(Paths.get('data/test1.update5.xml'), Paths.get('data/test3.recreate5.xml'), StandardCopyOption.REPLACE_EXISTING )
        
        // --- test4 - create
        writeXmlFromEngagements(new File('data/test4.create1.xml'), getCreateEngagements(messageSize, 'urn:riv:test:domain-scaletest1'))
        writeXmlFromEngagements(new File('data/test4.create2.xml'), getCreateEngagements(messageSize, 'urn:riv:test:domain-scaletest2'))
        writeXmlFromEngagements(new File('data/test4.create3.xml'), getCreateEngagements(messageSize, 'urn:riv:test:domain-scaletest3'))
        writeXmlFromEngagements(new File('data/test4.create4.xml'), getCreateEngagements(messageSize, 'urn:riv:test:domain-scaletest4'))
        writeXmlFromEngagements(new File('data/test4.create5.xml'), getCreateEngagements(messageSize, 'urn:riv:test:domain-scaletest5'))
    }
    
    // --- --------------------------------------------------------------------

    // local database with 10 million rows    
    // def eiDB = Sql.newInstance("jdbc:mysql://localhost:3306/ei10miljoner", "root", "", "com.mysql.jdbc.Driver")
    
    // mats database with 250 million rows
    def eiDB = Sql.newInstance("jdbc:mysql://192.168.0.135:3306/ei250miljoner", "newuser", "password", "com.mysql.jdbc.Driver")
    
    // database data:

    //   registeredResidentIdentification   19132508, ..
    //   serviceDomain                      urn:riv:test:domain0, 16
    //   categorization                     category0, category1, category2, category3
    //   logicalAddress                     VE-ID-814, .., VE-ID-9495
    //   businessObjectInstanceIdentifier   41607, .., 194252
    
    //   clinicalProcessInterestId          NA
    //   sourceSystem                       system
    //   dataController                     dataController
    //   mostRecentContent                  2015-03-06 12:56:05, ..
    

    // Generate xml for update or delete using data from sql - key matches key fields in database
    def writeXmlFromSql(File xmlFile, Sql eiDB, String sqlQuery, boolean delete = false) {
        
        def mostRecentContent = (new Date()).format("yyyyMMddHHmmss")
        
        def xml = new StreamingMarkupBuilder()
        Writable writer = xml.bind {
            mkp.declareNamespace(urn     : "urn:riv:itintegration:registry:1" )
            mkp.declareNamespace(soapenv : "http://schemas.xmlsoap.org/soap/envelope/" )
            mkp.declareNamespace(urn1    : "urn:riv:itintegration:engagementindex:UpdateResponder:1" )
            mkp.declareNamespace(urn2    : "urn:riv:itintegration:engagementindex:1" )
            'soapenv:Envelope' {
                'soapenv:Header' {
                    'urn:LogicalAddress' ('5565594230')
                }
                'soapenv:Body' {
                    'urn1:Update' {
                        eiDB.eachRow (sqlQuery as String) { row ->
                            'urn1:engagementTransaction' {
                                'urn2:deleteFlag' (delete)
                                'urn2:engagement' {
                                    // --- key fields
                                    'urn2:registeredResidentIdentification'  row.registered_resident_id
                                    'urn2:serviceDomain'                     row.service_domain
                                    'urn2:categorization'                    row.categorization
                                    'urn2:logicalAddress'                    row.logical_address
                                    'urn2:businessObjectInstanceIdentifier'  row.business_object_instance_id
                                    // --- non-key fields
                                    'urn2:clinicalProcessInterestId'         'scale'
                                    'urn2:sourceSystem'                      'ScaleTest'
                                    'urn2:dataController'                    'UpdateController'
                                    'urn2:mostRecentContent'                 mostRecentContent
                                }
                            }
                        }
                    }
                }
            }
        }
        xmlFile.write XmlUtil.serialize(writer)
    }

    // --- --------------------------------------------------------------------
    // Insert new data into Engagement Index (key fields do not match)    
    
    Random random = new Random()
    
    Date createDateStart = Date.parse("yyyyMMdd", "20010101")
    Date createDateEnd   = Date.parse("yyyyMMdd", "20991231")
    def createDateDaysRange = createDateEnd - createDateStart
    
    Engagement[] getCreateEngagements(int requiredNumber, String domain) {
        def engagements = []
        requiredNumber.times() {
            engagements << new Engagement(
                // --- five key fields - first one holds new value 
                registeredResidentIdentification:"${(createDateStart + random.nextInt(createDateDaysRange)).format("yyyyMMdd")}${random.nextInt(9999)}", // new registered resident identification
                serviceDomain                   :domain,                          // new domain
                categorization                  :"category${random.nextInt(3)}",  // existing category
                logicalAddress                  :"VE-ID-${random.nextInt(9495)}", // existing logical address
                businessObjectInstanceIdentifier:random.nextInt(194252),          // existing business object instance identifier
                // --- non-key fields 
                clinicalProcessInterestId       :"NA",
                sourceSystem                    :"HSA-ID-987654321",
                dataController                  :"ScaleTest",
                mostRecentContent               :(new Date()).format("yyyyMMddHHmmss")
                )
        }
        return engagements
    }

    // --- --------------------------------------------------------------------
    // Generate xml from a list of Engagements
    
    def writeXmlFromEngagements(File xmlFile, Engagement[] engagements, boolean delete = false) {

        def xml = new StreamingMarkupBuilder()
        Writable writer = xml.bind {
            mkp.declareNamespace(urn     : "urn:riv:itintegration:registry:1" )
            mkp.declareNamespace(soapenv : "http://schemas.xmlsoap.org/soap/envelope/" )
            mkp.declareNamespace(urn1    : "urn:riv:itintegration:engagementindex:UpdateResponder:1" )
            mkp.declareNamespace(urn2    : "urn:riv:itintegration:engagementindex:1" )
            'soapenv:Envelope' {
                'soapenv:Header' {
                    'urn:LogicalAddress' ('5565594230')
                }
                'soapenv:Body' {
                    'urn1:Update' {
                        engagements.each { engagement ->
                            'urn1:engagementTransaction' {
                                'urn2:deleteFlag' (delete)
                                'urn2:engagement' {
                                    'urn2:registeredResidentIdentification'  engagement.registeredResidentIdentification
                                    'urn2:serviceDomain'                     engagement.serviceDomain
                                    'urn2:categorization'                    engagement.categorization
                                    'urn2:logicalAddress'                    engagement.logicalAddress
                                    'urn2:businessObjectInstanceIdentifier'  engagement.businessObjectInstanceIdentifier
                                    'urn2:clinicalProcessInterestId'         engagement.clinicalProcessInterestId
                                    'urn2:sourceSystem'                      engagement.sourceSystem
                                    'urn2:dataController'                    engagement.dataController
                                    'urn2:mostRecentContent'                 engagement.mostRecentContent
                                }
                            }    
                        }
                    }
                }
            }
        }
        xmlFile.write XmlUtil.serialize(writer) // pretty-print xml
    }
}
