/**
 * 
 */
package org.bluelamar.wsruler;

/* FIX
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
*/

/**
 * @author mark
 *
 */
public class RestConnectionTest {

	static final String SvcName = "cdb";
	static ConnPool connPool;
	
	//static java.io.PrintStream _out = System.out;
	//@Rule
    //public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
	
	//@Rule
	//public final AllowWriteToSystemOut allowWriteToSystemOut
	    //= new AllowWriteToSystemOut();
	
	/**
	 * @throws java.lang.Exception
	 */
	//@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//System.setOut(System.out);
		
		System.err.println("setupbrclass: make conn pool");
		connPool = new RRConnPool();
		String baseUrl = "http://localhost:5984/";
		Connection connCloner = new RestConnection(SvcName, baseUrl);
		ConnCreds creds = new ConnCreds("_session", "wsruler", "oneringtorule");
		connPool.setConnectionCloner(connCloner, creds);
		System.err.println("setupbrclass: set conn pool");
	}

	/**
	 * @throws java.lang.Exception
	 */
	//@AfterClass
	public static void tearDownAfterClass() throws Exception {
		connPool.shutdown();
	}

	/**
	 * @throws java.lang.Exception
	 */
	//@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	//@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.bluelamar.RestConnection#RestConnection()}.
	 */
	//@Test
	public void testRestConnection() {
		try {
			Connection conn = connPool.getConnection(SvcName);
			System.err.println("testrestconn: got a conn");
		} catch (ConnException ex) {
			System.err.println("testRestConnection got exc=" + ex);
		}
	}

	/**
	 * Test method for {@link org.bluelamar.RestConnection#setSvcName(java.lang.String)}.
	 */
	//@Test
	public void testSetSvcName() {
		// FIX @todo fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.bluelamar.RestConnection#post(java.lang.String, java.lang.Object)}.
	 */
	//@Test
	public void testPost() {
		// FIX @todo fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.bluelamar.RestConnection#put(java.lang.String, java.lang.Object)}.
	 */
	//@Test
	public void testPut() {
		// FIX @todo fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.bluelamar.RestConnection#get(java.lang.Class, java.lang.String, java.util.Map)}.
	 */
	//@Test
	public void testGetClassOfTStringMapOfStringString() {
		// FIX @todo fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.bluelamar.RestConnection#get(java.lang.String, java.util.Map)}.
	 */
	//@Test
	public void testGetStringMapOfStringString() {
		// FIX @todo fail("Not yet implemented");
	}

	public static void main(String[] args) {
		try {
			System.err.println("run test from main");
			setUpBeforeClass();
			RestConnectionTest rct = new RestConnectionTest();
			rct.testRestConnection();
			tearDownAfterClass();
		} catch (Exception ex) {
			System.err.println("test got exc: " + ex);
			ex.printStackTrace();
		}
	}
}
