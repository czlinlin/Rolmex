package com.mossle.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;

public class AESUtils {

	/**
	 * aes 加密
	 * 
	 * @param data
	 * @return
	 */
	public static String encryptData(String data, String KEY, String IV) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			int blockSize = cipher.getBlockSize();
			byte[] dataBytes = data.getBytes();
			int plaintextLength = dataBytes.length;
			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
			}
			byte[] plaintext = new byte[plaintextLength];
			System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
			SecretKeySpec keyspec = new SecretKeySpec(KEY.getBytes(), "AES");
			IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			byte[] encrypted = cipher.doFinal(plaintext);
			return new String(Base64.encodeBase64(encrypted));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * aes 解密
	 * 
	 * @param data
	 *            密文
	 * @return
	 */
	public static String decryptData(String data, String KEY, String IV) {

		try {
			byte[] encrypted1 = Base64.decodeBase64(data.getBytes());
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec keyspec = new SecretKeySpec(KEY.getBytes(), "AES");
			IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original, "UTF-8");
			return originalString.trim();
		} catch (Exception e) {
			System.out.println(data);
			e.printStackTrace();
		}
		return "";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println(
				"解密:" + decryptData("fhxQkWXEwKGHJunQJRY6QQ==", "qwertyuiop789456", "qazwsx!)(@1029#$").trim());
		// System.out.println("解密:" +
		// decryptData("lrcyaE1bDRB+Qa0R3QaarilZqAgAbfdKEnaFtcOsWst3hbFvtK25taWgS3Wjw7qqd2Z1gnh1Ct6i/9OU651eLIXbNmfFZCT9JawFpw1uIH/Bz7RNshY7g8Q43xRqmBgNGMOe1/W/A9G/fU8pXlSDNy9PtNwex0hMNYTLPonXLPJvUSBwDNxCnnbV9bIxrIys").trim());

		// System.out.println("解密:" +
		// decryptData("lc7l8zUEo+OY+N6JXF62KYutnXUDn7NtiOOF6UWk89H98O1LuGsjhD1LZ0+WXCmUppl30rnhZ6axg0ZVVW5kqsemUcnMkDDXckv14jeDwdUE0q/OpAG2tHr77h/LFYW0ZVtgB8D42GhoaA+NmPgLV0mkFe4dqSVnw2/rtz3a/INaHswaY5o26yjPr6cc668XWsbyhoYAzNEaxAAdLpehFLtFl7egmAVl6h6uce5z/cpvDPyXo+HIp6GoZN9mRwgiBJAySjIRMJwarLZvVghshzK+S4lxTt8HJ2G90tCaycQ=").trim());
		// System.out.println("解密:" +
		// decryptData("kuyE5IQM2CRQWWn8TRwHFOhBtxCLSK/ZGeDii4pWaSDmkUw8IRIZ98BJW05SoT1QcBaM+wnQbS1J1rYcvegVgq7G3/uEPjvNY3SPjPf2wfuM8gMRthj5sXnLNCxq8kdHm/knnW7TSHNb1/+S5hFsib8yPS2YBHnK96ZxIqpN4Y9h2PpxqdJPnhLaMxHevPx8EYHSdJAtmcv+wFlCaEvuRPkj8TKWcSyR0qpoAYGoO1Ggus9YXOjET1hUw0b6JXiVUZ7Cl1vThzYMtc4uqZjm/1TWLHbe9HFludo6mwrnU+Z4vYZFVuo7aDqeSyzq1r+9zbxmcxy/y8YvDKsP33CNPlJa82+o8hSTfkp5txwYcLKdWxLrrITCYsyagZpbIynpUiCcs162v9LA6/65DwkSXw==").trim());

		// System.out.println("解密:" +
		// decryptData("2CO1fI+GoN4l68PQlu94OnFV1Syk63QBoClGcagIRLsLHKdZunLnA6UqCbq98Q3P+MiZEqU8rGWLQVGUGgFzD8yRsHjN9p1xOsfjQuhDiqzbTRHF7NjkRh7enhHUAlE4PwijJjF5a3gnf3JXsjXbUdyfTlAxTmiQIVAS4Mi/DoHIHupWf6boLXkG4sRECg3lPm/xCoLf2NJF6NlmJ7q+Df6CgeKv3y2E3ebEIXiVEE6SIrmsU7FWq/MIgZgpPtKnbYk7tZ/3n0URaKYi/fA4Dw==").trim());
		// System.out.println("解密:" +
		// decryptData("KJToQ2XsR5K+MANUd3T3jWLSzsITvNVZ6nGRIyEZLmiO5RS9Ev3yUeNh/32zxC6TanJ7nw99kJz1zxHQJNw+3N8HSNMU+EfRS4kDcI5k22JMTr40knQcoymzxiqPRwnFXTYxvo53ytiJWnLumVPtnYK4O5ymr+kekaAeIoveKFuh+ZDMgxx35Erab8M7V9wjcLtghqj29ovWa0xg2T4msRfzId1TZ1alZVLG2aKjd/Xp3gicMUYDLDFWT1FoQuTl47AAElV3KLm6Jd0OHJHGrELirUDv4CukcuX4oxr0tvInh/ZXpLhIEpetbHjvySVodXRBSABl9cloLaSFhtCs6do7LL22Rx3r0wkT0VpSWFPNpTiQm8zUEMHQi5k2qVxgA7rjDHfHpChL59+TwmuX1Lok+N1l6QrbVyJgg04ki9I=").trim());

	}
}
