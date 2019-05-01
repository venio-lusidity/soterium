import com.lusidity.*;
import com.lusidity.framework.text.StringX;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class ClientTest {

    private static String configPath = "/resource/config/client.json";
    private static String filePath = "/resource/data/enclave.xls";

    @Test
    public void importer() throws Exception {
        ClientConfiguration config = ClientConfiguration.create(ClientTest.configPath);
        File file = new File(UtilsX.getParentDirectory(), ClientTest.filePath);
        RmkImporterPostResponse ipr = RmkImporterPostResponse.post(config, file, RmkImporterPostResponse.DITRP);
        Assert.assertNotNull("The ImporterPostResponse should not be null.", ipr);
        Assert.assertTrue("The title should not be empty", !StringX.isBlank(ipr.getTitle()));
    }

    @Test
    public void authenticateAndRegister() throws Exception {

        // The AuthorizationResponse doesn't really do anything except to answer the following questions.
        // authorizationResponse.isAuthenticated()
        // authorizationResponse.isValidated()
        // authorizationResponse.isRegistered()
        // Each request is authenticated at the time of the request.

        ClientConfiguration config = ClientConfiguration.create(ClientTest.configPath);
        AuthorizationResponse authorizationResponse = AuthorizationResponse.authenticate(config);
        Assert.assertNotNull("The authorization response should not be null.", authorizationResponse);
        Assert.assertTrue("The API identity should be validated.", authorizationResponse.isValidated());

        // Since this test is disconnected from Soterium 
        // this could have been ran previously and already registered an account.
        // So checking for...
        // authorizationResponse.isAuthenticated()
        // and
        // authorizationResponse.isValidated()
        // Is not really relevant.

        if(!authorizationResponse.isRegistered()) {
            // now register the API key.
            RegisterResponse ipr = RegisterResponse.post(config);
            Assert.assertNotNull("The RegisterResponse should not be null.", ipr);
            Assert.assertTrue("The API identity should have been registered.", ipr.isRegistered());
        }

        authorizationResponse = AuthorizationResponse.authenticate(config);
        Assert.assertNotNull("The authorization response should not be null.", authorizationResponse);
        Assert.assertTrue("The API identity should not be authenticated as it needs to be approved.", !authorizationResponse.isAuthenticated());
        Assert.assertTrue("The API identity should be validated.", authorizationResponse.isValidated());
        Assert.assertTrue("The API identity should be registered.", authorizationResponse.isRegistered());
    }
}
