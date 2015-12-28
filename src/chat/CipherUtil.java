package chat;

import javax.xml.bind.DatatypeConverter; // for java 7. (in java 8, use java.util.Base64)

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Dongfang on 2015/12/15.
 */
public class CipherUtil {

    public static String encoderByMd5(String str)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {

        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        //加密后的字符串
        String newstr = DatatypeConverter.printBase64Binary(md5.digest(str.getBytes(StandardCharsets.UTF_8)));

        return newstr;
    }

    public static boolean checkPassword(String newpasswd, String oldpasswd)
            throws NoSuchAlgorithmException, UnsupportedEncodingException{

        if(encoderByMd5(newpasswd).equals(oldpasswd))
            return true;
        else
            return false;
    }
}


