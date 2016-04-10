package com.xwh.robot.util;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 字符串处理工具类
 * Created by XWH on 2015/3/16.
 */
public class StringUtil {

    public static String intToIp(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }



    /**
     * 将字符串转化为时间戳
     *
     * @return
     */
    public static long getTimeFromString(String time) {
        try {
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(time);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis();
    }
    
    public static String urlEncode(String text){
    	String str = null;
    	try {
			str = URLEncoder.encode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	return str;
    }


    /**
     * 加密
     *
     * @param content 待加密字符串
     * @param key     密码
     * @return
     * @throws Exception
     */
    public static String encodeAES(String content, String key) {

        String str_encode = null;
        try {
            byte[] input = content.getBytes("utf-8");

            byte[] thedigest = encodeMD5(key).getBytes("utf-8");

            String iv = getIv();
            byte[] byte_iv = iv.getBytes();

            System.out.println(thedigest);
            SecretKeySpec skc = new SecretKeySpec(thedigest, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skc, new IvParameterSpec(byte_iv));

            byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
            int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
            ctLength += cipher.doFinal(cipherText, ctLength);

            String str_base64 = Base64.encodeToString(cipherText, Base64.DEFAULT).trim();
            // 带上iv再base64
            str_encode = Base64.encodeToString((iv + str_base64).getBytes(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return str_encode;
    }

    /**
     * AES 解密
     *
     * @throws Exception
     */
    public static String decodeAES(String encrypted, String key) {
        String str_decode = null;
        try {
            byte[] decode_base64 = Base64.decode(encrypted, Base64.DEFAULT);
            String str = new String(decode_base64);

            String str_iv = str.substring(0, 16);
            String content_base64 = str.substring(16);
            byte[] content = Base64.decode(content_base64, Base64.DEFAULT);

            byte[] thedigest = encodeMD5(key).getBytes("utf-8");
            SecretKeySpec skey = new SecretKeySpec(thedigest, "AES");
            Cipher dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            dcipher.init(Cipher.DECRYPT_MODE, skey, new IvParameterSpec(str_iv.getBytes()));

            byte[] clearbyte = dcipher.doFinal(content);

            str_decode = new String(clearbyte);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return str_decode;
    }

    /**
     * 获取随机IV
     *
     * @return
     */
    public static String getIv() {
        StringBuffer str = new StringBuffer();
        Random rdm = new Random();
        for (int i = 0; i < 16; i++) {
            char ch = (char) rdm.nextInt(128);
            str.append(ch);
        }

        return str.toString();
    }


    /**
     * MD5 编码
     */
    public static String encodeMD5(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");

            messageDigest.reset();

            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();

        return bytes2HexString(byteArray);
    }

    public static String bytes2HexString(byte[] byteArray) {
        if(byteArray==null){
            return "";
        }
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString();
    }

    public static String readStringFromFile(String path) {
        String str = "";
        try {
            File file = new File(path);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = null;
                while(null!=(line = br.readLine())){
                    str+=line;
                }
                br.close();
            }
        } catch (FileNotFoundException e) {
            str = null;
        } catch (IOException e) {
            str = null;
        }
        return str;

    }

    public static boolean writeStringTOFile(String str, String path) {
        boolean bool = true;
        try {
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(str);
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            bool = false;
        } catch (IOException e) {
            bool = false;
        }
        return bool;

    }

}
