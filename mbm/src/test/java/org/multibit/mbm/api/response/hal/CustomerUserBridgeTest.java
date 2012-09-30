package org.multibit.mbm.api.response.hal;

import com.google.common.base.Optional;
import com.theoryinpractise.halbuilder.spi.Resource;
import org.junit.Before;
import org.junit.Test;
import org.multibit.mbm.db.DatabaseLoader;
import org.multibit.mbm.db.dto.Role;
import org.multibit.mbm.db.dto.User;
import org.multibit.mbm.test.BaseResourceTest;
import org.multibit.mbm.test.FixtureAsserts;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

public class CustomerUserBridgeTest extends BaseResourceTest {

  @Before
  public void setUpDefaultRequestInfo() throws Exception {
    setUpUriInfo(Optional.<String>absent());
    setUpHttpHeaders(Optional.<List<MediaType>>absent());
  }

  @Test
  public void representUserAsJson() throws Exception {

    Role customerRole = DatabaseLoader.buildRoleCustomer();
    User aliceUser = DatabaseLoader.buildCustomerAlice(customerRole);

    CustomerUserBridge testObject = new CustomerUserBridge(uriInfo,principal);

    Resource resource = testObject.toResource(aliceUser);

    FixtureAsserts.assertRepresentationMatchesJsonFixture("a User can be marshalled to JSON", resource, "fixtures/hal/user/expected-user-by-customer.json");

  }

  @Test
  public void representUserAsXml() throws IOException {

    Role customerRole = DatabaseLoader.buildRoleCustomer();
    User aliceUser = DatabaseLoader.buildCustomerAlice(customerRole);

    CustomerUserBridge testObject = new CustomerUserBridge(uriInfo,principal);

    Resource resource = testObject.toResource(aliceUser);

    FixtureAsserts.assertRepresentationMatchesXmlFixture("a User can be marshalled to XML", resource, "fixtures/hal/user/expected-user-by-customer.xml");

  }

}
