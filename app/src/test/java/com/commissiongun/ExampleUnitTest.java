package com.commissiongun;

import com.commissiongun.yore.commission.Admin;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    Admin admin;
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Before
    public void login() {
        admin = new Admin("admin", "12345");
        admin.login();
    }


}