package com.cfs.common.util.crypto;


import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * Created with IntelliJ IDEA. User: Suhua Date: 14-5-7 Time: 上午11:14 To change this template use File | Settings | File Templates.
 */
public class RSAUtil
{

	private static final String KEY_ALGORITHM = "RSA";
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final int KEY_SIZE = 1024;

	public static final
	String
			DEVICE_RSA_PUBLIC_KEY =
			"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdBVDVybzvADKDrtaIvIEQacbao3DETGw508WT4K31xPgMS2kTwO3iALUdbeHlhVDHXwEHJj4shfvRau4FkuY0sMbS+VeJQPMrl2SL3sRGd3ArXWJ308HUZPmRfufTSGW7WU0qvVCGmx+YxPvzdwvRBwEJzYX+xWD+VCUTELIpxwIDAQAB";
	public static final
	String
			DEVICE_RSA_PRIVATE_KEY =
			"MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJ0FUNXJvO8AMoOu1oi8gRBpxtqjcMRMbDnTxZPgrfXE+AxLaRPA7eIAtR1t4eWFUMdfAQcmPiyF+9Fq7gWS5jSwxtL5V4lA8yuXZIvexEZ3cCtdYnfTwdRk+ZF+59NIZbtZTSq9UIabH5jE+/N3C9EHAQnNhf7FYP5UJRMQsinHAgMBAAECgYB4CPxGsrYQLyJusFWoqhIwLeyqb49hQNrrSg0cgwH5g93r6Conw7EWuFu8Z6ftAgFCqEns4TteZf6flRLoh+ga5Qf++hrWa7CmxvKRYzso3CjZBnMMVoDu23Mk2NA/wTq0mMhKQgrFsLR3zXi69Efb6EVU0iM7IQqhtlXEU9UPgQJBAMske6OsvjSJ3TU0fQVjvPphJ3uzJObNB+mCKAsWFVpXH13uXSOyAtW5z4qikP4jL4s+OkXJ/9UsWppAp0EywxECQQDF4J5+MEx/OP71jAML65V9YH/4mMPs8yLgYnXWt/cca721hnr5JXnCMrTCKhYbSH1DwF9x3T5ruw237splBe9XAkAINTkpq9kjlk5xz+UdSqJgG3zU6rAbAz3GmZO0nvfN5qdFzFPHFXI34IIaP+dL3XAWWDVSjI7htiETCnm3kK2BAkAPyPdbwaaYj4dnNyAXF1f5hHUw857NjCfAFpqn4k0IK2Aa1vuAXIj2AO2Cf7D7xDCZ8wKqJqgQF//kKFxk6rBJAkEAqjBagb2jxKiwxReYPc6q17UfJEEMLg60RWIS+RJkzNht/jkVZwXOvTqDP/AZMSNA1R1AvNkTAW9joyKpQ73mhg==";


	/**
	 * 使用模和指数生成RSA公钥 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA /None/NoPadding】
	 *
	 * @param modulus        模
	 * @param publicExponent 指数
	 */
	public static RSAPublicKey getPublicKey(String modulus, String publicExponent)
	{
		try
		{
			BigInteger b1 = new BigInteger(modulus);
			BigInteger b2 = new BigInteger(publicExponent);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
			return (RSAPublicKey) keyFactory.generatePublic(keySpec);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 使用模和指数生成RSA私钥 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA /None/NoPadding】
	 *
	 * @param modulus         模
	 * @param privateExponent 指数
	 */
	public static RSAPrivateKey getPrivateKey(String modulus, String privateExponent)
	{
		try
		{
			BigInteger b1 = new BigInteger(modulus);
			BigInteger b2 = new BigInteger(privateExponent);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * 使用模生成私钥
	 */
	private static PrivateKey getPrivateKey() throws Exception
	{
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(RSAUtil.DEVICE_RSA_PRIVATE_KEY));
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		return keyFactory.generatePrivate(pkcs8KeySpec);
	}

	/**
	 * 使用模生成公钥
	 */
	private static PublicKey getPublicKey() throws Exception
	{
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(RSAUtil.DEVICE_RSA_PUBLIC_KEY));
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		return keyFactory.generatePublic(x509KeySpec);
	}

	/**
	 * 公钥加密
	 */
	public static String encryptByPublicKey(String data) throws Exception
	{
		return encryptByPublicKey(data, getPublicKey());
	}

	/**
	 * 私钥解密
	 */
	public static String decryptByPrivateKey(String data) throws Exception
	{
		return decryptByPrivateKey(data, getPrivateKey());
	}


	/**
	 * 公钥加密
	 */
	public static String encryptByPublicKey(String data, PublicKey publicKey) throws Exception
	{
		Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		// 模长
		int key_len = KEY_SIZE / 8;
		// 加密数据长度 <= 模长-11 128-11=117
		String[] datas = splitString(data, key_len - 11);
		StringBuffer encryptText = new StringBuffer();
		//如果明文长度大于模长-11则要分组加密

		ByteBuffer byteBuffer = ByteBuffer.allocate(datas.length * 128);
		for (String s : datas)
		{
			byte[] b = cipher.doFinal(s.getBytes(DEFAULT_CHARSET));
			byteBuffer.put(b);
		}
		encryptText.append(Base64.encodeBase64String(byteBuffer.array()));
		return encryptText.toString();

	}

	/**
	 * 私钥解密
	 */
	public static String decryptByPrivateKey(String data, PrivateKey privateKey) throws Exception
	{
		Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		//模长
		int key_len = KEY_SIZE / 8;
		byte[] bytes = Base64.decodeBase64(data);
		//如果密文长度大于模长则要分组解密
		StringBuffer decryptText = new StringBuffer();
		byte[][] arrays = splitArray(bytes, key_len);
		for (byte[] arr : arrays)
		{
			decryptText.append(new String(cipher.doFinal(arr), DEFAULT_CHARSET));
		}
		return decryptText.toString();

	}

	/**
	 * 拆分数组
	 */
	public static byte[][] splitArray(byte[] data, int len)
	{
		int x = data.length / len;
		int y = data.length % len;
		int z = 0;
		if (y != 0)
		{
			z = 1;
		}
		byte[][] arrays = new byte[x + z][];
		byte[] arr;
		for (int i = 0; i < x + z; i++)
		{
			arr = new byte[len];
			if (i == x + z - 1 && y != 0)
			{
				System.arraycopy(data, i * len, arr, 0, y);
			} else
			{
				System.arraycopy(data, i * len, arr, 0, len);
			}
			arrays[i] = arr;
		}
		return arrays;
	}


	/**
	 * 拆分字符串
	 */
	public static String[] splitString(String string, int len)
	{
		int x = string.length() / len;
		int y = string.length() % len;
		int z = 0;
		if (y != 0)
		{
			z = 1;
		}
		String[] strings = new String[x + z];
		String str = "";
		for (int i = 0; i < x + z; i++)
		{
			if (i == x + z - 1 && y != 0)
			{
				str = string.substring(i * len, i * len + y);
			} else
			{
				str = string.substring(i * len, i * len + len);
			}
			strings[i] = str;
		}
		return strings;
	}

}

