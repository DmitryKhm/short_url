package com.example.shortener.client;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

public class ShortenerClient {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter short code: ");
        String code = sc.nextLine().trim();
        String reqUrl = "http://localhost:8080/api/r/" + code;
        HttpURLConnection conn = (HttpURLConnection) new URL(reqUrl).openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("GET");
        int rc = conn.getResponseCode();
        if (rc == 302) {
            String loc = conn.getHeaderField("Location");
            System.out.println("Redirecting to: " + loc);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(loc));
            } else {
                System.out.println("Desktop not supported — open URL manually.");
            }
        } else {
            System.out.println("Link unavailable. Server returned: " + rc);
            try (InputStream in = conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream();
                 Scanner s = new Scanner(in)) {
                while (s.hasNextLine()) System.out.println(s.nextLine());
            } catch (Exception ignored) {}
        }
    }
}
