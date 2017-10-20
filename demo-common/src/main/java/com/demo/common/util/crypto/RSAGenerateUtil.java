package com.cfs.common.util.crypto;

import org.apache.commons.codec.binary.Base64;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;

public class RSAGenerateUtil
{

	/**
	 * 生成公钥和私钥
	 */
	public static HashMap<String, Object> getKeys() throws NoSuchAlgorithmException
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		keyPairGen.initialize(1024);
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		map.put("public", publicKey);
		map.put("private", privateKey);
		return map;
	}

	public static void main(String[] args) throws NoSuchAlgorithmException
	{
		HashMap<String, Object> map = RSAGenerateUtil.getKeys();
		//生成公钥和私钥
		RSAPublicKey publicKey = (RSAPublicKey) map.get("public");
		RSAPrivateKey privateKey = (RSAPrivateKey) map.get("private");
		System.out.println("getPublicExponent:" + publicKey.getPublicExponent());
		System.out.println("getAlgorithm:" + publicKey.getAlgorithm());
		System.out.println("getFormat:" + publicKey.getFormat());
		System.out.println("getEncoded:" + publicKey.getEncoded());
		System.out.println("getEncoded:" + Base64.encodeBase64String(publicKey.getEncoded()));
		System.out.println("getModulus:" + publicKey.getModulus());
		System.out.println("getModulus().bitLength:" + publicKey.getModulus().bitLength());

		System.out.println("----------------------------");
		System.out.println("getPrivateExponent:" + privateKey.getPrivateExponent());
		System.out.println("getAlgorithm:" + privateKey.getAlgorithm());
		System.out.println("getFormat:" + privateKey.getFormat());
		System.out.println("getEncoded:" + privateKey.getEncoded());
		System.out.println("getEncoded:" + Base64.encodeBase64String(privateKey.getEncoded()));
		System.out.println("getModulus:" + privateKey.getModulus());
		System.out.println("getModulus().bitLength:" + privateKey.getModulus().bitLength());
		System.out.println("----------------------------");

	}
}
