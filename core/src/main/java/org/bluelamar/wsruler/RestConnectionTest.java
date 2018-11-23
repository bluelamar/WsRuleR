/**
 * 
 */
package org.bluelamar.wsruler;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

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
		
		System.out.println("setupbrclass: make conn pool");
		connPool = new QueueConnPool();
		String baseUrl = "http://localhost:5984/";
		RestConnection connCloner = new CdbRestConnection(SvcName, baseUrl);
		CdbConnCredFactory creds = new CdbConnCredFactory("_session", "wsruler", "oneringtorule");
		/* FIX
		 * creds.setLoginFactory(new ConnLoginFactory() {
			@Override
			public Object buildLogin(String user, String passwd) {
				return new Object() {
					public String name = user;
					public String password = passwd;
				};
			}
		}); */
		connPool.setConnectionCloner(connCloner, creds);
		System.out.println("setupbrclass: set conn pool");
		IdFactory idf = new ShortIdFactory();
		String id = idf.makeId(baseUrl);
		System.out.println("setupbrclass: use=" + baseUrl + " make-id=" + id);
		long lret = ((ShortIdFactory)idf).makeId64(baseUrl.getBytes("UTF-8"));
		System.out.println("setupbrclass: use=" + baseUrl + " make64-id=" + Long.toHexString(lret));
		
		baseUrl = "http://localhost:5985/";
		id = idf.makeId(baseUrl);
		System.out.println("setupbrclass: use=" + baseUrl + " make-id=" + id);
		lret = ((ShortIdFactory)idf).makeId64(baseUrl.getBytes("UTF-8"));
		System.out.println("setupbrclass: use=" + baseUrl + " make64-id=" + Long.toHexString(lret));
		
		baseUrl = "http://mocalhost:5984/";
		id = idf.makeId(baseUrl);
		System.out.println("setupbrclass: use=" + baseUrl + " make-id=" + id);
		lret = ((ShortIdFactory)idf).makeId64(baseUrl.getBytes("UTF-8"));
		System.out.println("setupbrclass: use=" + baseUrl + " make64-id=" + Long.toHexString(lret));
		
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
	 * Test method for {@link org.bluelamar.CdbRestConnection#RestConnection()}.
	 */
	//@Test
	public void testRestConnection() {
		try {
			RestConnection conn = connPool.getConnection(SvcName);
			System.err.println("testrestconn: got a conn");
			
			// lets get something
			// http://localhost:5984/_node/nonode@nohost/_config
			System.out.println("get _config:");
			Map<String, Object> resp = conn.get("_node/nonode@nohost/_config", null);
			for (String key: resp.keySet()) {
				System.out.println("key=" + key + " val=" + resp.get(key));
			}
	/* FIX 		
	 // once created, if tried again will get a 412 http error code
			System.out.println("create db=gruff");
			// curl --cookie "cdbcookies" http://localhost:5984/scruff -X PUT
			int pret = conn.put("gruff", "", null);
			System.out.println("create db=tuff got ret=" + pret);
			
			System.out.println("Returns the database information:");
			// curl http://localhost:5984/stuff
			resp = conn.get("tuff", null);
			for (String key: resp.keySet()) {
				System.out.println("key=" + key + " val=" + resp.get(key));
			}
*/
			System.out.println("Creates a new document with generated ID if _id is not specified:");
			//curl -H "Content-Type: application/json" http://localhost:5984/stuff -X POST -d '{"name":"bud","age":99}'
			java.util.UUID uuid = java.util.UUID.randomUUID();
			String _id = "alphabet"; // uuid.toString();
			String docObj = "{\"name\":\"jason\",\"age\":59, \"_id\":\"" + _id + "\"}";
			Map<String, Object> docMap = new HashMap<>();
			docMap.put("name", "jason");
			docMap.put("age", 59);
			docMap.put("_id", _id);
			Object ret = conn.post("tuff", docMap, null);
			System.out.println("created jason: instance=" + ret.getClass().getName() + " ret=" + ret + " with _id=" + _id);
			
			System.out.println("Get the db id=" + _id);
			//curl http://localhost:5984/stuff/592ccd646f8202691a77f1b1c5004496
			resp = conn.get("tuff/" + _id, null);
			for (String key: resp.keySet()) {
				System.out.println("key=" + key + " val=" + resp.get(key));
			}
			
			// search for the doc
			Object res = conn.searchEqual("tuff", "name", "jason");
			if (res == null) {
				System.out.println("search for tuff with name=jason empty results");
			} else {
				java.util.List<Object> list = (java.util.List<Object>)res;
				for (Object obj: list) {
					System.out.println("search found: " + obj);
				}
			}
			
			//"rev":"1-f4fca40b7c8f5707f56dc94d0cf4d214"
			resp.put("age", 60);
			System.out.println("Update a document jason id=: "+resp.get("_id"));
			//curl --cookie "cdbcookies" -H "Content-Type: application/json" http://localhost:5984/stuff/592ccd646f8202691a77f1b1c5004496 -X PUT -d '{"name":"sam","age":42,"_rev":"1-3f12b5828db45fda239607bf7785619a"}'
			//String repObj = "{\"name\":\"wilbur\",\"age\":12}";
			java.util.Map<String,String> args = new java.util.HashMap<>();
			args.put("rev", resp.get("_rev").toString());
			ret = conn.put("tuff/" + resp.get("_id"), resp, args);
			System.out.println("tuff with jason ret=" + ret);
			
			System.out.println("Get again the db id=" + resp.get("_id"));
			//curl http://localhost:5984/stuff/592ccd646f8202691a77f1b1c5004496
			resp = conn.get("tuff/"+ resp.get("_id"), null);
			for (String key: resp.keySet()) {
				System.out.println("key=" + key + " val=" + resp.get(key));
			}
			
			System.out.println("Try to find wilbur records:");
			String selector = "{ \"selector\": {" +
					"\"age\": {\"$gt\": 5}}," +
					"\"fields\": [\"_id\", \"_rev\", \"name\", \"age\"]}";
			Object retObj = conn.post("tuff/_find", selector, null);
			Map<String,Object> entity = (Map<String,Object>)retObj;
			retObj = entity.get("docs");
			if (retObj != null && retObj instanceof java.util.List) {
				int cnt =0;
				java.util.List<Object> objList = (java.util.List)retObj;
				for (Object obj: objList) {
					System.out.println("got resp type=" + obj.getClass().getName() + " val=" + obj);
					
					if (cnt % 2 == 0) {
						Map<String, Object> xobj = (Map<String,Object>)obj;
						Object id = xobj.get("_id");
						String path = "tuff/" + id;
						
						// lets get that doc to see the rev
						System.out.println("get the doc before deleting: " + path);
						resp = conn.get(path, null);
						for (String key: resp.keySet()) {
							System.out.println("key=" + key + " val=" + resp.get(key));
						}
						
						Object rev = xobj.get("_rev");
						Map<String, String> delArgs = new java.util.HashMap<>();
						delArgs.put("rev", rev.toString());
						System.out.println("delete rev=" + rev + " id=" + path);
						int retCode = conn.delete(path, delArgs);
						System.out.println("delete ret=" + retCode + " id=" + path);
					}
				}
				// conn.delete path/id ?rev="xxx"
			}
			
		} catch (ConnException ex) {
			System.err.println("testRestConnection got exc=" + ex);
		}
	}

	/**
	 * Test method for {@link org.bluelamar.CdbRestConnection#setSvcName(java.lang.String)}.
	 */
	//@Test
	public void testSetSvcName() {
		// FIX @todo fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.bluelamar.CdbRestConnection#post(java.lang.String, java.lang.Object)}.
	 */
	//@Test
	public void testPost() {
		// FIX @todo fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.bluelamar.CdbRestConnection#put(java.lang.String, java.lang.Object)}.
	 */
	//@Test
	public void testPut() {
		// FIX @todo fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.bluelamar.CdbRestConnection#get(java.lang.Class, java.lang.String, java.util.Map)}.
	 */
	//@Test
	public void testGetClassOfTStringMapOfStringString() {
		// FIX @todo fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.bluelamar.CdbRestConnection#get(java.lang.String, java.util.Map)}.
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
