package org.multibit.mbm.db.dao.hibernate;

import org.junit.Test;
import org.multibit.mbm.db.DatabaseLoader;
import org.multibit.mbm.db.dto.*;
import org.multibit.mbm.db.dao.CustomerDao;
import org.multibit.mbm.test.BaseIntegrationTests;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Integration test to verify the Hibernate annotations of the DTOs against a generated schema
 */
@ContextConfiguration(locations = {"/spring/test-mbm-context.xml"})
public class HibernateCustomerDaoIntegrationTest extends BaseIntegrationTests {

  @Resource(name= "hibernateCustomerDao")
  CustomerDao testObject;

  @Test
  public void testPersistAndFind() {

    Role customerRole = DatabaseLoader.buildRoleCustomer();
    User aliceUser = DatabaseLoader.buildCustomerAlice(customerRole);

    Customer expected = aliceUser.getCustomer();

    // Persist with insert
    int originalCustomerRows = countRowsInTable("customers");
    testObject.saveOrUpdate(expected);

    // Session flush: Expect an insert in customers only
    int updatedCustomerRows = countRowsInTable("customers");
    assertThat("Expected session flush for first insert", updatedCustomerRows, equalTo(originalCustomerRows+1));

    // Perform an update to the Customer that cascades to an insert in ContactMethod (but not secondary)
    expected=testObject.saveOrUpdate(expected);
    testObject.flush();

    // Session flush: Expect no change to customers, insert into contact_method_details
    // Note that contactMethodDetail is now a different instance from the persistent one
    updatedCustomerRows = countRowsInTable("customers");
    assertThat("Unexpected data in customers", updatedCustomerRows, equalTo(originalCustomerRows+1));

    // Perform an update to the Customer that cascades to an insert in secondary ContactMethod
    // due to an addition to the linked reference
    expected=testObject.saveOrUpdate(expected);
    testObject.flush();

    // Session flush: Expect no change to customers, contact_method_details, insert into contact_method_secondary_details
    updatedCustomerRows = countRowsInTable("customers");
    assertThat("Unexpected data in customers", updatedCustomerRows, equalTo(originalCustomerRows+1));

  }

}
