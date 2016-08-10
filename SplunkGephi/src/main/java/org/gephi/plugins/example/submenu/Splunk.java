/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.plugins.example.submenu;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;

public class Splunk {
    public void connection(){
        HttpURLConnection connection = null;  
        try {
          //Create connection
          String targetURL = "http://gesman-centos7x64-001/script/ipusers/py";
          URL url = new URL(targetURL);
          connection = (HttpURLConnection)url.openConnection();
          //将请求改为GET
          connection.setRequestMethod("GET");
          connection.setRequestProperty("Content-Type", 
              "application/x-www-form-urlencoded");

          //connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
          connection.setRequestProperty("Content-Language", "en-US");  

          connection.setUseCaches(false);



          //Get Response  
          InputStream is = connection.getInputStream();

          
          File file = new File("ipusers.gexf");
          BufferedReader reader=null;
          Writer writer = null;            
          try {
                reader = new BufferedReader(new InputStreamReader(is));
                writer = new OutputStreamWriter(new FileOutputStream(file));
                while (true) {
                    String line = reader.readLine();
                    if (line == null)
                        break;
                    
                    writer.write(line + "\n");
                }           
            } catch (IOException ex) {
                
            }finally{           
              reader.close();
              writer.close();
          }
         
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {

          if(connection != null) {
            connection.disconnect(); 
          }
        }
        }  
        }
    
        