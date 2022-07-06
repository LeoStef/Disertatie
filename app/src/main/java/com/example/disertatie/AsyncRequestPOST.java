package com.example.disertatie;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AsyncRequestPOST extends AsyncTask<String, Void, String> {



    @Override


    protected String doInBackground(String... strings) {
        HttpURLConnection con = null;
        try {
            URL url = new URL(strings[0]);
            con = (HttpURLConnection)url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type","application/json; utf-8");
            //con.setRequestProperty("Accept","application/json");
            //create json with data to send to the db
            JSONObject json = new JSONObject();
            json.accumulate("android_id", strings[1]);
            json.accumulate("iv",strings[2]);
            json.accumulate("key",strings[3]);
            String jsonString = json.toString();
            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(jsonString);
            wr.flush();
            //Log.d("test","POST request sent hopefully");
            //Log.d("test","Json content:" + jsonString);
            //Log.d("test","input content:" + wr);
            int responseCode = con.getResponseCode();
            InputStream inputStream = con.getInputStream();
            StringBuilder builder = new StringBuilder();
            Reader reader = new BufferedReader(new InputStreamReader(inputStream,
                    Charset.forName(StandardCharsets.UTF_8.name())));
            int c = 0;
            while ((c = reader.read()) != -1){
                builder.append((char) c);
            }
            String response = builder.toString();
            Log.d("test","Response code is: " + responseCode);
            return response;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }finally{
            if(con != null){
                con.disconnect();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
