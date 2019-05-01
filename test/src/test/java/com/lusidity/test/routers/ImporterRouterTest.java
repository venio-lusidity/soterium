

package com.lusidity.test.routers;

import com.lusidity.framework.text.StringX;
import com.lusidity.test.BaseTest;
import com.lusidity.workers.importer.ImporterRouter;
import org.junit.AfterClass;
import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public
class ImporterRouterTest extends BaseTest
{

	// Overrides
	@Override
	public boolean isDisabled()
	{
		return false;
	}

	// Methods
	@AfterClass
	public static void afterClass(){

		String[] files = {"compliance_meta_data.xml", "patch_meta_data.xml", "software_meta_data.xml"};
		for(String f: files)
		{
			Path pathFrom = Paths.get(String.format("data/ascc/backup/%s", f));
			Path pathTo = Paths.get(String.format("data/ascc/%s", f));
			try{
				Files.copy(pathFrom, pathTo, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		BaseTest.afterClass();
	}

	public void check(File meta, Class expected, String type){
		try(FileInputStream inputStream = new FileInputStream(meta)){
			Assert.assertTrue("The type should start with ImporterRouter", StringX.startsWith(type, "ImporterRouter"));

			ImporterRouter router = new ImporterRouter(meta, type);
			boolean submit = router.handle(inputStream);

			Assert.assertTrue("The handler failed", submit);
			Assert.assertTrue("The type is blank.", !StringX.isBlank(router.getType()));
			Assert.assertNotNull("The file is null", router.getFile());
			Assert.assertTrue("The file does not exist.", router.getFile().exists());

			// delete previous file;
			meta.delete();
			meta = router.getFile();
			type = router.getType();

			Assert.assertTrue("The file is missing.", router.getFile().exists());

			Class actual=router.getImporterClass();

			Assert.assertEquals("The importers do not match.", expected, actual);

			Assert.assertNotNull("The file is null", router.getFile());
			Assert.assertTrue("The file does not exist.", router.getFile().exists());
			// clean up.
			//noinspection ResultOfMethodCallIgnored
			router.getFile().delete();
		}
		catch (Exception ex){
			Assert.fail(ex.getMessage());
		}
	}
}
