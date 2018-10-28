/**
 * 
 */
package org.bluelamar.wsruler;

import java.math.BigInteger;

/**
 * Create short ID for given objects. Default is to create 4 byte ID.
 * Set System.property "wsruler.IdSize" with value = "8" to get 8 byte ID.
 * Given 32 bit hash, makes 4 byte ID with hex chars.
 * Given 64 bit hash, makes 8 byte ID with hex chars.
 */
public class ShortIdFactory implements IdFactory {

	static final String BITSIZE_SYS_PROP = "wsruler.IdSize";
	static final String BITSIZE_VAL32 = "4";
	static final String BITSIZE_VAL64 = "8";
	
	static final long FNV_PRIME_32 = 16777619; // 0x1000193
	static final long FNV_OFFSET_BASIS_32 = 2166136261L; // 0x811C9DC5
	
	static final long FNV_PRIME_64 = 1099511628211L;
	static final BigInteger FNV_OFFSET_BASIS_64 = new BigInteger("14695981039346656037", 10); // 0xCBF29CE484222325;
	
	boolean make32hash = true;
	
	/**
	 * 
	 */
	public ShortIdFactory() {
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
			System.out.println("ShortIDF: failed to get utf-8 bytes. use default");
			bytes = obj.toString().getBytes();
		}
		
		if (make32hash) {
			int ret = (int)makeId32(bytes);
			return Integer.toHexString(ret);
		}
		
		// 64 bit hash
		long ret = makeId64(bytes);
		return Long.toHexString(ret);
	}
	
	public long makeId32(final byte[] bytes) {
		long hash = FNV_OFFSET_BASIS_32;
		System.out.println("makeid32: ");
		for (byte byte_of_data: bytes) {
			System.out.print(" " + byte_of_data);
			hash ^= byte_of_data;
			hash *= FNV_PRIME_32;
		}
		System.out.println();
		return hash;
	}
	
	public long makeId64(final byte[] bytes) {
		BigInteger hash = FNV_OFFSET_BASIS_64;
		System.out.println("makeid64: ");
		for (byte byte_of_data: bytes) {
			System.out.print(" " + byte_of_data);
			hash = hash.multiply(new BigInteger(Integer.toString(byte_of_data), 10));
			hash = hash.xor(new BigInteger(Long.toString(FNV_PRIME_64), 10));
		}
		System.out.println();
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
