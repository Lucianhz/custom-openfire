package org.jivesoftware.openfire.plugin.rest.utils;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;


public class RSAUtils {
	
	public static final String PUBLIC_KEY= "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDTDP/P4fapVgUPEeeNDBv1iyqiWyuJTPMM4extfGXstm5xL7k95FtpcWpIwi81emQxM3X6y5tdHHwNn/VgcpKta8P35q5FBeMYkNg97SSH6wAYA36hSxjEkjRLiv/c82Dcu8P/2rhSpjXfWB9imOgZy9g3B3WhmsG8YymJAhjJlwIDAQAB";
	public static final String PRIVATE_KEY = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBANMM/8/h9qlWBQ8R540MG/WLKqJbK4lM8wzh7G18Zey2bnEvuT3kW2lxakjCLzV6ZDEzdfrLm10cfA2f9WBykq1rw/fmrkUF4xiQ2D3tJIfrABgDfqFLGMSSNEuK/9zzYNy7w//auFKmNd9YH2KY6BnL2DcHdaGawbxjKYkCGMmXAgMBAAECgYBgLARGDooAEBquC1Kgi2wSMCB1a2YjYtU+gZTjL1Si0V7yZPTcpzqgifYMwbARhnwxj2mrpyxc+aXt7345PPxpBYwDH+QAZwKyRpFYg95NgvP00vvMAqAqlIPQFoPsJHG7YLG0/4046HUuf4FJRhTsQF1jKVLxorYJzQEYjYHEAQJBAP4U0BNyDL0+6dRAiOvi0mkx4dctTBziSWUKZXa8XRVpNYk5H7OjbB0R4L3lJzM3x7EZm+zSkSzpLkbFUA3ZgcECQQDUpQAV1N74T3pvevsdc8JAcEMks0UlmtWFgV0/Ojf+oOdvDYkd8RlfHnLeq3Jm3rpMCasGbD7Ov7D9pNftx/FXAkEA3hy9pTIfVtSHvsfHqkX34IP9xIhRsDJVLOIAvuJ9kPkPFu17/CLRoTv+tqJ7OTf69qPHfii5RoR1suJMUD8jQQJBAMK1Rwu2fGfwFpMHj/Ja8a6hXMm5IQKa8RKq7qAbhfQwj1nfkgMJpgqzzcjYQguxu/IuFBzwdt5HJiBKlbFTmEcCQQCYwCeqdPX3OGU0yH/w4cI0zc28O0JfeHn+qum4IJabZ3awiQEVOqDtRVMnuCpAinEAvgAmHelFJfjNA3UC3JA8";
	/**
	 * 加密算法RSA
	 */
	public static final String KEY_ALGORITHM = "RSA";

	/**
	 * 签名算法
	 */
	public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

	/**
	 * RSA最大加密明文大小
	 */
	private static final int MAX_ENCRYPT_BLOCK = 117;

	/**
	 * RSA最大解密密文大小
	 */
	private static final int MAX_DECRYPT_BLOCK = 128;

	/**
	 * <p>
	 * 生成密钥对(公钥和私钥)
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> genKeyPair() throws Exception {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyPairGen.initialize(1024);
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		Map<String, String> keyMap = new HashMap<String,String>(2);
		keyMap.put("RSAPublicKey", Base64.encodeBase64String(publicKey.getEncoded()));
		keyMap.put("RSAPrivateKey", Base64.encodeBase64String(privateKey.getEncoded()));
		return keyMap;
	}

	/**
	 * <p>
	 * 用私钥对信息生成数字签名
	 * </p>
	 * 
	 * @param data
	 *            已加密数据
	 * @param privateKey
	 *            私钥(BASE64编码)
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String sign(byte[] data, String privateKey) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(privateKey);
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(privateK);
		signature.update(data);
		return Base64.encodeBase64String(signature.sign());
	}

	/**
	 * <p>
	 * 校验数字签名
	 * </p>
	 * 
	 * @param data
	 *            已加密数据
	 * @param publicKey
	 *            公钥(BASE64编码)
	 * @param sign
	 *            数字签名
	 * 
	 * @return
	 * @throws Exception
	 * 
	 */
	public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(publicKey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		PublicKey publicK = keyFactory.generatePublic(keySpec);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(publicK);
		signature.update(data);
		return signature.verify(Base64.decodeBase64(sign));
	}

	/**
	 * <P>
	 * 私钥解密
	 * </p>
	 * 
	 * @param encryptedData
	 *            已加密数据
	 * @param privateKey
	 *            私钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static String decryptByPrivateKey(String str, String privateKey) throws Exception {
		byte[] encryptedData = Base64.decodeBase64(str);
		byte[] keyBytes = Base64.decodeBase64(privateKey);
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateK);
		int inputLen = encryptedData.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return new String(decryptedData);
	}

	/**
	 * <p>
	 * 公钥解密
	 * </p>
	 * 
	 * @param encryptedData
	 *            已加密数据
	 * @param publicKey
	 *            公钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static String decryptByPublicKey(String str, String publicKey) throws Exception {
		byte[] encryptedData = Base64.decodeBase64(str);
		byte[] keyBytes = Base64.decodeBase64(publicKey);
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key publicK = keyFactory.generatePublic(x509KeySpec);
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, publicK);
		int inputLen = encryptedData.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return new String(decryptedData);
	}

	/**
	 * <p>
	 * 公钥加密
	 * </p>
	 * 
	 * @param data
	 *            源数据
	 * @param publicKey
	 *            公钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static String encryptByPublicKey(String str, String publicKey) throws Exception {
		byte[] data = str.getBytes();
		byte[] keyBytes = Base64.decodeBase64(publicKey);
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key publicK = keyFactory.generatePublic(x509KeySpec);
		// 对数据加密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicK);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return new String(Base64.encodeBase64String(encryptedData));
	}

	/**
	 * <p>
	 * 私钥加密
	 * </p>
	 * 
	 * @param data
	 *            源数据
	 * @param privateKey
	 *            私钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static String encryptByPrivateKey(String str, String privateKey) throws Exception {
		byte[] data = str.getBytes();
		byte[] keyBytes = Base64.decodeBase64(privateKey);
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateK);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		
		return new String(Base64.encodeBase64String(encryptedData));
	}
	public static void main(String[] args) throws Exception{
		//Map<String,String> map = genKeyPair();
		//System.out.println(map.get("RSAPublicKey"));
		//System.out.println(map.get("RSAPrivateKey"));
		//String password = "qweqweqweqwe123123";
		//byte[] p = password.getBytes();
		//System.out.println(new String(Base64.encodeBase64String(encryptByPrivateKey(p, PRIVATE_KEY))));
		String ss = encryptByPrivateKey("asdasdad",PRIVATE_KEY);
		System.out.println(decryptByPublicKey("beRL76reHVc5RD7ICpKor0iSGYkffD9bQshqV3k46C10H51fRcInmcxX7kJYsCKkzvMPFW4zJUKMMgXH5GgG4o+iwxEThVGq7UR/pJPq2JMwrwleSa0m8oRkG7z/aK/V6XIyLAPimrOVZQBkI0l3uE3EVAqEjJpFqkI2xnjgEA1zjgxJXlzQDoAjOKPqb8u3WfHqog0m2ctwn07Ru7clSlMiD4BoKWi0De4v93atpL1pQKTUl76PQYLtaoBnD4J3aAsCgJpVcJKx/Xfp4vY2h8z5wG/3DhwOLvF8RANyRVI0p/05BvlL7CN+1Hn71H5IjnTIlNMDMCdSC9Pt7+udXQ==", PUBLIC_KEY));
	}
}
