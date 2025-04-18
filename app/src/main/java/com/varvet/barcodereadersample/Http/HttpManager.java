package com.varvet.barcodereadersample.Http;

import java.io.BufferedReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStreamWriter;
import 	java.io.InputStreamReader;
import java.io.IOException;


public class HttpManager {
    public static String getData(RequestPackage requestPackage) {

        BufferedReader reader = null;
        String uri = requestPackage.getUrl();

        if (requestPackage.getMethod().equals("GET")) {
            uri += "?" + requestPackage.getEncodedParams();
            //As mentioned before, this only executes if the request method has been
            //set to GET
        }

        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(requestPackage.getMethod());

            if (requestPackage.getMethod().equals("POST")) {
                con.setDoOutput(true);
                OutputStreamWriter writer =
                        new OutputStreamWriter(con.getOutputStream());
                writer.write(requestPackage.getEncodedParams());
                writer.flush();
            }

            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }
}