/**
 * 
 */
package org.bluelamar.wsruler;

import java.util.Map;

/**
 * @author mark
 *
 */
public class RestConnectionTest {

	static final String SvcName = "cdb";
	static ConnPool connPool;
	
	/**
	 * @throws java.lang.Exception
	 */
	//@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
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
			
			// lets get something
			// http://localhost:5984/_node/nonode@nohost/_config
			System.out.println("get _config:");
			Map<String, Object> resp = conn.get("_node/nonode@nohost/_config", null);
			for (String key: resp.keySet()) {
				System.out.println("key=" + key + " val=" + resp.get(key));
			}
			/* good
			System.out.println("create db=tuff");
			// curl --cookie "cdbcookies" http://localhost:5984/scruff -X PUT
			int ret = conn.put("tuff", "");
			System.out.println("create db=tuff got ret=" + ret);
			*/
			System.out.println("Returns the database information:");
			// curl http://localhost:5984/stuff
			resp = conn.get("tuff", null);
			for (String key: resp.keySet()) {
				System.out.println("key=" + key + " val=" + resp.get(key));
			}

			System.out.println("Creates a new document with generated ID if _id is not specified:");
			//curl -H "Content-Type: application/json" http://localhost:5984/stuff -X POST -d '{"name":"bud","age":99}'
			java.util.UUID uuid = java.util.UUID.randomUUID();
			String _id = uuid.toString();
			String docObj = "{\"name\":\"jason\",\"age\":59, \"_id\":\"" + _id + "\"}";
			int ret = conn.post("tuff", docObj, null);
			System.out.println("created jason: ret=" + ret + " with _id=" + _id);
			
			System.out.println("Get the db id=" + _id);
			//curl http://localhost:5984/stuff/592ccd646f8202691a77f1b1c5004496
			resp = conn.get("tuff/" + _id, null);
			for (String key: resp.keySet()) {
				System.out.println("key=" + key + " val=" + resp.get(key));
			}
			
			//"rev":"1-f4fca40b7c8f5707f56dc94d0cf4d214"
			System.out.println("Update a document_id=5ddd840a-383d-494c-aaf2-b87b00e5c262 specifying rev:1-f4fca40b7c8f5707f56dc94d0cf4d214:");
			//curl --cookie "cdbcookies" -H "Content-Type: application/json" http://localhost:5984/stuff/592ccd646f8202691a77f1b1c5004496 -X PUT -d '{"name":"sam","age":42,"_rev":"1-3f12b5828db45fda239607bf7785619a"}'
			String repObj = "{\"name\":\"wilbur\",\"age\":12,\"_rev\":\"1-f4fca40b7c8f5707f56dc94d0cf4d214\"}";
			ret = conn.put("tuff/5ddd840a-383d-494c-aaf2-b87b00e5c262", repObj);
			System.out.println("tuff with wilbur ret=" + ret);
			
			System.out.println("Get again the db id=5ddd840a-383d-494c-aaf2-b87b00e5c262 :");
			//curl http://localhost:5984/stuff/592ccd646f8202691a77f1b1c5004496
			resp = conn.get("tuff/5ddd840a-383d-494c-aaf2-b87b00e5c262", null);
			for (String key: resp.keySet()) {
				System.out.println("key=" + key + " val=" + resp.get(key));
			}
			
			
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
