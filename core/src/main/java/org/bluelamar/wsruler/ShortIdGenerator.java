/**
 * 
 */
package org.bluelamar.wsruler;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create short ID for given objects. Default is to create 4 byte ID.
 * Set System.property "wsruler.IdSize" with value = "8" to get 8 byte ID.
 * Given 32 bit hash, makes 4 byte ID with hex chars.
 * Given 64 bit hash, makes 8 byte ID with hex chars.
 * 
 * Fowler–Noll–Vo hash implementation
 */
public class ShortIdGenerator implements IdGenerator {
	
	private static final Logger LOG = LoggerFactory.getLogger(ShortIdGenerator.class);

	static final String BITSIZE_SYS_PROP = "wsruler.id_size";
	static final String BITSIZE_VAL32 = "4";
	static final String BITSIZE_VAL64 = "8";
	
	static final long FNV_PRIME_32 = 16777619; // 0x1000193
	static final long FNV_OFFSET_BASIS_32 = 2166136261L; // 0x811C9DC5
	
	static final long FNV_PRIME_64 = 1099511628211L;
	static final BigInteger FNV_OFFSET_BASIS_64 = new BigInteger("14695981039346656037", 10); // 0xCBF29CE484222325;
	
	boolean make32hash = true;
	final SecureRandom random = new SecureRandom();
	
	/**
	 * 
	 */
	public ShortIdGenerator() {
		// read sys prop to see if should be 64 bit rather than default 32 bit
		String bitSize = System.getProperty(BITSIZE_SYS_PROP, BITSIZE_VAL32);
		if (bitSize.equals(BITSIZE_VAL64)) {
			make32hash = false;
		}
	}

	/* (non-Javadoc)long
	 * @see org.bluelamar.wsruler.IdFactory#makeId(java.lang.Object)
	 */
	public String makeId(Object obj) {

		byte[] bytes;
		try {
			bytes = obj.toString().getBytes("UTF-8"); 
        } catch (java.io.UnsupportedEncodingException e) {
			LOG.debug("makeId: failed to get utf-8 bytes. use default");
			bytes = obj.toString().getBytes();
		}

		byte saltBytes[] = new byte[20];
		random.nextBytes(saltBytes);
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		try {  
		    baos.write(bytes);
	        baos.write(saltBytes);
	        bytes = baos.toByteArray();
		} catch (java.io.IOException ex) {
	    	LOG.debug("makeId: failed to add salt to object: " + ex);
	    }
		
		if (make32hash) {
			long ret = (long)makeId32(bytes);
			return Integer.toHexString((int)ret);
		}
		
		// 64 bit hash
		Long ret = makeId64(bytes);
		return Long.toHexString(ret);
	}
	
	// FNV-1a
	public Long makeId32(final byte[] bytes) {
		Long hash = FNV_OFFSET_BASIS_32;
		for (byte byte_of_data: bytes) {
			hash ^= byte_of_data;
			hash *= FNV_PRIME_32;
		}
		return hash;
	}
	
	// FNV-1
	public long makeId64(final byte[] bytes) {
		BigInteger hash = FNV_OFFSET_BASIS_64;
		for (byte byte_of_data: bytes) {
			hash = hash.multiply(new BigInteger(Integer.toString(byte_of_data), 10));
			hash = hash.xor(new BigInteger(Long.toString(FNV_PRIME_64), 10));
		}
		return hash.longValue();
	}

	/* (non-Javadoc)
	 * @see org.bluelamar.wsruler.IdFactory#makeId(java.lang.Object[])
	 */
	public String makeId(Object[] objs) {
		
		StringBuilder sb = new StringBuilder();
		for (Object obj: objs) {
			sb.append(obj.toString());
		}
		
		return makeId(sb.toString());
	}

}
