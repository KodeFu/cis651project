package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by acer on 03.05.2017.
 */

/**
 * Revised by Mehmet on 5/4/2017.
 */
public class HTTP_METHODS {

    // Download an image using HTTP Get Request
    public static Bitmap downloadImageUsingHTTPGetRequest(String urlString) {
        Bitmap image=null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = httpConnection.getInputStream();
                image = getImagefromStream(stream);
            }
            httpConnection.disconnect();
        }
        catch (UnknownHostException e1) {
            Log.d("MyDebugMsg", e1.getMessage());
            e1.printStackTrace();
        }
        catch (Exception ex) {
            Log.d("MyDebugMsg", ex.getMessage());
            ex.printStackTrace();
        }
        return image;
    }

    // Get an image from the input stream
    private static Bitmap getImagefromStream(InputStream stream) {
        Bitmap bitmap = null;
        if(stream!=null) {
            bitmap = BitmapFactory.decodeStream(stream);
            try {
                stream.close();
            }catch (IOException e1) {
                Log.d("MyDebugMsg", "IOException in getImagefromStream()");
                e1.printStackTrace();
            }
        }
        return bitmap;
    }


    // Download JSON data (string) using HTTP Get Request
    public static String downloadJSONusingHTTPGetRequest(String urlString) {
        String jsonString=null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = httpConnection.getInputStream();
                jsonString = getStringfromStream(stream);
            }
            httpConnection.disconnect();
        }  catch (UnknownHostException e1) {
            Log.d("MyDebugMsg", e1.getMessage());
            e1.printStackTrace();
        } catch (Exception ex) {
            Log.d("MyDebugMsg", ex.getMessage());
            ex.printStackTrace();
        }
        return jsonString;
    }

    // Get a string from an input stream
    private static String getStringfromStream(InputStream stream){
        String line, jsonString=null;
        if (stream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder out = new StringBuilder();
            try {
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                reader.close();
                jsonString = out.toString();
            } catch (IOException ex) {
                Log.d("MyDebugMsg", "IOException in downloadJSON()");
                ex.printStackTrace();
            }
        }
        return jsonString;
    }

    // Load JSON string from a local file (in the asset folder)
    public static String loadJSONFromAsset(Context context, String fileName) {
        String json = null, line;
        try {
            InputStream stream = context.getAssets().open(fileName);
            if (stream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder out = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                reader.close();
                json = out.toString();
            }
        } catch (IOException ex) {
            Log.d("MyDebugMsg", "IOException in loadJSONFromAsset()");
            ex.printStackTrace();
        }
        return json;
    }


    // Send json data via HTTP POST Request
    public static Boolean sendHttPostRequest(String urlString, JSONObject json){
        boolean result=false;
        HttpURLConnection httpConnection = null;
        try {
            URL url = new URL(urlString);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setDoOutput(true);
            //httpConnection.setRequestMethod("POST");
            httpConnection.addRequestProperty("content-type","application/json");
            httpConnection.setChunkedStreamingMode(0);
            OutputStreamWriter out = new OutputStreamWriter(httpConnection.getOutputStream());
            out.write(json.toString());
            out.close();
            Log.d("MyDebugMsg",json.toString());
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.d("MyDebugMsg:PostRequest", line);  // for debugging purpose
                }
                reader.close();
                Log.d("MyDebugMsg:PostRequest", "POST request returns ok");
                result=true;
            }
            else{
                Log.d("MyDebugMsg:PostRequest", "POST request returns error");
                result=false;
            }
        } catch (Exception ex) {
            Log.d("MyDebugMsg", "Exception in sendHttpPostRequest");
            ex.printStackTrace();
            result=false;
        }

        if (httpConnection != null) httpConnection.disconnect();
        return result;
    }
    // Send json data via HTTP Put Request
    public static Boolean sendHttPutRequest(String urlString, JSONObject json){
        boolean result=false;
        HttpURLConnection httpConnection = null;
        try {
            URL url = new URL(urlString);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("PUT");
            httpConnection.addRequestProperty("content-type","application/json");
            httpConnection.setChunkedStreamingMode(0);
            OutputStreamWriter out = new OutputStreamWriter(httpConnection.getOutputStream());
            out.write(json.toString());
            out.close();
            Log.d("MyDebugMsg",json.toString());
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.d("MyDebugMsg:PostRequest", line);  // for debugging purpose
                }
                reader.close();
                Log.d("MyDebugMsg:PostRequest", "POST request returns ok");
                result=true;
            }
            else{
                Log.d("MyDebugMsg:PostRequest", "POST request returns error");
                result=false;
            }
        } catch (Exception ex) {
            Log.d("MyDebugMsg", "Exception in sendHttpPostRequest");
            ex.printStackTrace();
            result=false;
        }

        if (httpConnection != null) httpConnection.disconnect();
        return result;
    }
    // Send json data via HTTP Delete Request
    public static Boolean sendHttDeleteRequest(String urlString){
        String jsonString=null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("DELETE");
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = httpConnection.getInputStream();
                jsonString = getStringfromStream(stream);
            }
            httpConnection.disconnect();
            return true;
        }  catch (UnknownHostException e1) {
            Log.d("MyDebugMsg", e1.getMessage());
            e1.printStackTrace();
            return false;
        } catch (Exception ex) {
            Log.d("MyDebugMsg", ex.getMessage());
            ex.printStackTrace();
            return false;
        }

    }
}

