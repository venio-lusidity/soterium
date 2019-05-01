package com.lusidity.test.acs;

import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.domains.organization.Organization;
import com.lusidity.test.BaseTest;
import com.lusidity.test.domains.acs.security.UnitTestCredentials;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
       //TODO add config check
public class AccessControlTest extends BaseTest {

    private static final String PROVIDER= "x509";

    @BeforeClass
    public static
    void beforeClass() throws Exception
    {
        BaseTest.setTestConfig(BaseTest.getTestConfig());
        BaseTest.setClearStores(true);
        BaseTest.setInitialize(true);
        BaseTest.beforeClass();
    }

    @Override
    public
    boolean isDisabled()
    {
        return false;
    }

    @Test
    public void acs() throws Exception
    {

        UnitTestCredentials credentials = new UnitTestCredentials();
        // read write delete admin
        this.testPermissions(credentials, "3333333333", false, false, false, true, true);
        this.testPermissions(credentials, "4444444444", true, true, true, true, false);
        this.testPermissions(credentials, "5555555555", true, true , true, false, false);
    }

    private void testPermissions(UnitTestCredentials credentials, String identifier, boolean read, boolean write, boolean delete, boolean admin, boolean delContributor) throws Exception

    {
        Identity identity = Identity.get(AccessControlTest.PROVIDER, identifier);
        Assert.assertNotNull("There should be an identity in the config file that is" +
                                 " used to create the one requested.", identity);
        BasePrincipal principal = identity.getPrincipal();
        Assert.assertNotNull("The principal should not be null.", principal);

        principal.setCredentials(credentials);

        Assert.assertTrue("The user should be in the contributor group.", Group.isInGroup(principal, "contributor"));

        if(delContributor)
        {
            Group.remove(principal, "contributor");
            Assert.assertTrue("The user should not be in the contributor group.", !Group.isInGroup(principal, "contributor"));
        }

        boolean r = principal.canRead(false);
        boolean w = principal.canWrite(false);
        boolean d = principal.canDelete(false);
        boolean a = principal.isAdmin(false);

        Assert.assertEquals("Can read failed for " + identifier, r, read);
        Assert.assertEquals("Can write failed for " + identifier, w, write);
        Assert.assertEquals("Can delete failed for " + identifier, d, delete);
        Assert.assertEquals("Is admin failed for " + identifier, a, admin);
    }

    public static
    void makeOrganizations(Organization parent, int size, int depth, int on)
        throws Exception
    {
        if(on<depth)
        {
            for (int i=0; i<size; i++)
            {
                Organization child=new Organization();
                child.fetchTitle().setValue(String.format("org_%d_%d", (on), (i+1)));
                child.save();
                parent.getOrganizations().add(child);
                AccessControlTest.makeOrganizations(child, size, depth, (on+1));
            }
        }
    }

    public static
    void createOrganizations(int size, int depth)
        throws Exception
    {
        Organization global = Organization.getRoot();
        AccessControlTest.makeOrganizations(global, size, depth, 0);
    }
}
