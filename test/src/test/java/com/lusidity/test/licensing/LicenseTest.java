package com.lusidity.test.licensing;

import com.lusidity.license.LicenseManager;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class LicenseTest
{
	private static final File LIC_DIR=new File("/mnt/xdata/work/projects/soterium/resource/keys/licensing/issued/license");
	private static final File PUB_FILE=new File("/mnt/xdata/work/projects/soterium/resource/keys/licensing/publicCert.der");
	private static final File PRV_FILE=new File("/mnt/xdata/work/projects/soterium/resource/keys/licensing/privateKey.der");
	@Test
	public void createKey()
		throws Exception
	{
		DateTime future = DateTime.now().plusDays(15);
		Long days = 20L;
		LicenseManager licenseManager = new LicenseManager(LicenseTest.PUB_FILE, LicenseTest.PRV_FILE);
		File bad = licenseManager.create(
			LicenseTest.LIC_DIR,
			"bad",
			DateTime.now().minusDays(1),
			LicenseManager.AccessLevel.community,
			"web", false,
			"services", false
		);

		File good = licenseManager.create(
			LicenseTest.LIC_DIR,
			"good",
			DateTime.now().plusDays(30),
			LicenseManager.AccessLevel.enterprise,
			"web", true,
			"services", false,
			"cbac", days,
			"auth", future,
			"client", "yes"
		);

		LicenseManager blm = new LicenseManager(null, LicenseTest.PRV_FILE, bad);
		LicenseManager glm = new LicenseManager(null, LicenseTest.PRV_FILE, good);

		Assert.assertFalse("The license should be expired.", blm.isLicenseValid());
		Assert.assertTrue("The license should not be expired", glm.isLicenseValid());

		LicenseManager.AccessLevel bal = blm.getAccessLevel();
		Assert.assertEquals("The access levels do not match.", LicenseManager.AccessLevel.community, bal);

		LicenseManager.AccessLevel gal = glm.getAccessLevel();
		Assert.assertEquals("The access levels do not match.", LicenseManager.AccessLevel.enterprise, gal);

		this.check(glm, "web", true);
		this.check(glm, "services", false);
		this.check(glm, "cbac", days);
		this.check(glm, "auth", future);
		this.check(glm, "client", "yes");
	}

	private void check(LicenseManager manager, String key, Object expected)
	{
		Object actual = null;
		if(expected instanceof String){
			actual = manager.getString(key);
		}
		else if(expected instanceof DateTime){
			actual = manager.getDateTime(key);
		}
		else if(expected instanceof Boolean){
			actual = manager.getBoolean(key);
		}
		else if(expected instanceof Long){
			actual = manager.getLong(key);
		}

		Assert.assertEquals(String.format(
			"The key %s did not have the expected value.", expected.toString()),
			(null==expected) ? null : expected.toString(),
			(null==actual) ? actual : actual.toString());
	}
}
