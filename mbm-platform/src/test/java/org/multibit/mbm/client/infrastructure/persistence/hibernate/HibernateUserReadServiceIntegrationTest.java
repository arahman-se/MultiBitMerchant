package org.multibit.mbm.client.infrastructure.persistence.hibernate;

import com.google.common.base.Optional;
import org.junit.Test;
import org.multibit.mbm.client.common.pagination.PaginatedList;
import org.multibit.mbm.client.domain.model.model.ContactMethod;
import org.multibit.mbm.client.domain.model.model.ContactMethodDetail;
import org.multibit.mbm.client.domain.model.model.User;
import org.multibit.mbm.client.domain.model.model.UserBuilder;
import org.multibit.mbm.client.domain.repositories.UserReadService;
import org.multibit.mbm.testing.BaseIntegrationTests;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Integration test to verify the Hibernate annotations of the DTOs against a generated schema
 */
@ContextConfiguration(locations = {"/spring/test-mbm-context.xml"})
public class HibernateUserReadServiceIntegrationTest extends BaseIntegrationTests {

  @Resource(name= "hibernateUserDao")
  UserReadService testObject;

  /**
   * Verifies User creation and updates work against ContactMethods 
   */
  @Test
  public void testPersistAndFind() {

    String apiKey="abc123";
    String secretKey="1234-5678";

    User expected = UserBuilder.newInstance()
      .withApiKey(apiKey)
      .withSecretKey(secretKey)
      .build();
    expected.setApiKey(apiKey);

    // Persist with insert
    int originalUserRows = countRowsInTable("users");
    int originalContactMethodDetailRows = countRowsInTable("contact_method_details");
    int originalContactMethodDetailSecondaryRows = countRowsInTable("contact_method_secondary_details");
    testObject.saveOrUpdate(expected);

    // Session flush: Expect an insert in users only
    int updatedUserRows = countRowsInTable("users");
    int updatedContactMethodDetailRows = countRowsInTable("contact_method_details");
    int updatedContactMethodDetailSecondaryRows = countRowsInTable("contact_method_secondary_details");
    assertThat("Expected session flush for first insert", updatedUserRows, equalTo(originalUserRows+1));
    assertThat("Unexpected data in contact_method_details", updatedContactMethodDetailRows, equalTo(originalContactMethodDetailRows));
    assertThat("Unexpected data in contact_method_secondary_details", updatedContactMethodDetailSecondaryRows, equalTo(originalContactMethodDetailSecondaryRows));

    // Perform an update to the User that cascades to an insert in ContactMethod (but not secondary)
    ContactMethodDetail contactMethodDetail = new ContactMethodDetail();
    contactMethodDetail.setPrimaryDetail("test@example.org");
    expected.setContactMethodDetail(ContactMethod.EMAIL, contactMethodDetail);
    expected=testObject.saveOrUpdate(expected);
    testObject.flush();

    // Session flush: Expect no change to users, insert into contact_method_details
    // Note that contactMethodDetail is now a different instance from the persistent one
    updatedUserRows = countRowsInTable("users");
    updatedContactMethodDetailRows = countRowsInTable("contact_method_details");
    updatedContactMethodDetailSecondaryRows = countRowsInTable("contact_method_secondary_details");
    assertThat("Unexpected data in users", updatedUserRows, equalTo(originalUserRows+1));
    assertThat("Expected data in contact_method_details", updatedContactMethodDetailRows, equalTo(originalContactMethodDetailRows+1));
    assertThat("Unexpected data in contact_method_secondary_details", updatedContactMethodDetailSecondaryRows, equalTo(originalContactMethodDetailSecondaryRows));

    // Perform an update to the User that cascades to an insert in secondary ContactMethod
    // due to an addition to the linked reference
    contactMethodDetail = expected.getContactMethodDetail(ContactMethod.EMAIL);
    contactMethodDetail.getSecondaryDetails().add("test2@example.org");
    expected=testObject.saveOrUpdate(expected);
    testObject.flush();

    // Session flush: Expect no change to users, contact_method_details, insert into contact_method_secondary_details
    updatedUserRows = countRowsInTable("users");
    updatedContactMethodDetailRows = countRowsInTable("contact_method_details");
    updatedContactMethodDetailSecondaryRows = countRowsInTable("contact_method_secondary_details");
    assertThat("Unexpected data in users", updatedUserRows, equalTo(originalUserRows+1));
    assertThat("Unexpected data in contact_method_details", updatedContactMethodDetailRows, equalTo(originalContactMethodDetailRows+1));
    assertThat("Unexpected data in contact_method_secondary_details", updatedContactMethodDetailSecondaryRows, equalTo(originalContactMethodDetailSecondaryRows+1));

    // Query against the "openId"
    Optional<User> actual=testObject.getByApiKey("abc123");

    // Session flush: Expect no change to users, contact_method_details, contact_method_secondary_details
    updatedUserRows = countRowsInTable("users");
    updatedContactMethodDetailRows = countRowsInTable("contact_method_details");
    updatedContactMethodDetailSecondaryRows = countRowsInTable("contact_method_secondary_details");
    assertThat("Unexpected data in users",updatedUserRows, equalTo(originalUserRows+1));
    assertThat("Unexpected data in contact_method_details",updatedContactMethodDetailRows, equalTo(originalContactMethodDetailRows+1));
    assertThat("Unexpected data in contact_method_secondary_details", updatedContactMethodDetailSecondaryRows, equalTo(originalContactMethodDetailSecondaryRows+1));

    assertThat(actual.get(),equalTo(expected));

  }

  /**
   * Verifies that data loading occurred as expected
   */
  @Test
  public void testUsersAndRoles() {

    Optional<User> aliceCustomer = testObject.getByApiKey("alice123");
    
    assertTrue("Expected pre-populated data", aliceCustomer.isPresent());
    assertThat("Unexpected number of Roles",aliceCustomer.get().getUserRoles().size(), equalTo(1));

  }

  /**
   * Verifies that paging works as expected
   */
  @Test
  public void testPaging() {

    PaginatedList<User> users = testObject.getPaginatedList(1, 1);

    // Trent the admin is built first
    assertNotNull("Expected pre-populated data (page 1)",users);
    assertThat("Unexpected number of Users",users.list().size(), equalTo(1));
    assertThat("Unexpected ordering of Users",users.list().get(0).getUsername(), equalTo("trent"));

    users = testObject.getPaginatedList(1, 2);

    // Alice the customer is built second
    assertNotNull("Expected pre-populated data (page 2)",users);
    assertThat("Unexpected number of Users",users.list().size(), equalTo(1));
    assertThat("Unexpected ordering of Users",users.list().get(0).getUsername(), equalTo("cameron"));

  }

}
