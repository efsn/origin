package org.codeyn.util.random;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 随机数生成器
 */
public class SecurityRandom {
	
	/**随机数产生的算法 */
	private static final String ALGORITHM = "SHA1PRNG";

	//安全检查修改
	private static SecureRandom randomNumberGenerator;
	
	public static SecureRandom getInstance(){
		synchronized(SecurityRandom.class){
			if (randomNumberGenerator == null){
				try {
					randomNumberGenerator = SecureRandom.getInstance(ALGORITHM);
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e.getCause());
				}
			}		
			return randomNumberGenerator;
		}
	}
	
	public static SecureRandom getInstance(long seed){
		try {
			SecureRandom random = SecureRandom.getInstance(ALGORITHM);
			random.setSeed(seed);
			return random;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getCause());
		}		
	}

}
