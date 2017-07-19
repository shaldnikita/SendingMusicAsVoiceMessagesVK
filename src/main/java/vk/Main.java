/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vk;

import com.google.gson.JsonParser;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author shaldnikita
 */
public class Main {

    private static final String CODE_PATH
            = "https://oauth.vk.com/authorize?client_id=6081095&display=wap&redirect_uri=https://oauth.vk.com/blank.html&scope=docs,messages,offline&response_type=code&v=5.65";
    private static final String CODE = "430393e53c83b5921b";
    private String token;
    private JsonParser parser = new JsonParser();

    public static void main(String[] args) throws IOException, ApiException, ClientException, Exception {

        Main m = new Main();
        m.init("a.mp3", 129540597);

    }

    public void init(String file_name, int id) throws Exception {
        setToken();
        
        String serverJson = uploadFile();

        String urlToUpload = parser.parse(serverJson).getAsJsonObject().getAsJsonObject("response").get("upload_url").getAsString();

        System.out.println(urlToUpload);
        StringBuilder response_sb = new StringBuilder();
        try {
            MultipartUtility multipart = new MultipartUtility(urlToUpload, "UTF-8");
            multipart.addFilePart("file", new File(file_name));
            List<String> response = multipart.finish();
            response.forEach((line) -> {
                response_sb.append(line);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        String file = parser.parse(response_sb.toString()).getAsJsonObject().get("file").getAsString();
        System.out.println(file);
        String uploaded = saveFile(file);
        System.out.println(uploaded);

        int owner_id = parser.parse(uploaded).getAsJsonObject().getAsJsonArray("response").get(0).getAsJsonObject().get("owner_id").getAsInt();
        int media_id = parser.parse(uploaded).getAsJsonObject().getAsJsonArray("response").get(0).getAsJsonObject().get("id").getAsInt();
        System.out.println(sendMessage(owner_id, media_id, id));
    }

    private void setToken() throws ApiException, ClientException, FileNotFoundException, IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("token.txt");
                Scanner sc = new Scanner(is)) {
            if(sc.hasNext())
                token = sc.next();          
        }
        if (token==null) {
            System.out.println("GETTING NEW TOKEN");
            TransportClient transportClient = HttpTransportClient.getInstance();
            VkApiClient vk = new VkApiClient(transportClient);
            UserAuthResponse authResponse = vk.oauth()
                    .userAuthorizationCodeFlow(6081095, "m7WSYFBkXtvFNClcbutv", "https://oauth.vk.com/blank.html", CODE)
                    .execute();
            UserActor actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
            token = actor.getAccessToken();           
            try(PrintWriter writer = new PrintWriter(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"token.txt")){
                writer.print(token);
            }
        }
    }

    private String saveFile(String arg) throws IOException {
        HttpPost httpPost
                = new HttpPost("https://api.vk.com/method/docs.save");
        List<NameValuePair> params = new ArrayList<>();

        params.add(new BasicNameValuePair("file", arg));
        params.add(new BasicNameValuePair("access_token", token));
        params.add(new BasicNameValuePair("v", "5.65"));

        httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));

        HttpClient client = HttpClients.createDefault();
        HttpResponse httpResponse = client.execute(httpPost);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));) {
            return br.readLine();
        }
    }

    private String uploadFile() throws IOException {

        HttpGet httpGet = new HttpGet("https://api.vk.com/method/docs.getUploadServer?access_token=" + token + "&type=audio_message&v=5.65");
        HttpClient client = HttpClients.createDefault();
        HttpResponse httpResponse = client.execute(httpGet);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));) {
            return br.readLine();
        }
    }

    private String sendMessage(int owner_id, int media_id, int id) throws IOException {
        HttpGet httpGet = new HttpGet("https://api.vk.com/method/messages.send?user_id=" + id + "&attachment=doc" + owner_id + "_" + media_id + "&access_token=" + token + "&v=5.65");
        HttpClient client = HttpClients.createDefault();
        HttpResponse httpResponse = client.execute(httpGet);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));) {
            return br.readLine();
        }
    }
}
