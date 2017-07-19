/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

/**
 *
 * @author shaldnikita
 */
public class MainIT {

    public MainIT() {
    }

    @Test
    public void testMain() throws Exception {
        System.out.println(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"token.txt");
        try(PrintWriter writer = new PrintWriter(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"token.txt")){
                writer.print("123");
            }
        
    }

}
