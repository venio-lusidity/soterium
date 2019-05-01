package com.lusidity.test.framework;

import com.lusidity.framework.text.StringX;
import org.junit.Assert;
import org.junit.Test;

public class StringXTest {
    @Test
    public void capitalCasing(){
        this.test("CvssMetric", "Cvss Metric");
        this.test("IAVM", "IAVM") ;
        this.test("alllower", "alllower");
    }

    private void test(String actual, String expected){
        actual = StringX.insertSpaceAtCapitol(actual);
        Assert.assertTrue(String.format("The strings do not match, %s : %s.", actual, expected), StringX.equals(actual, expected));
    }
}
