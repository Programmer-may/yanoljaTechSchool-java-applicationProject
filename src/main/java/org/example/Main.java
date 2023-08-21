package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.example.model.MyAddress;
import org.example.model.Pharmacy;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.*;
import java.util.*;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

        MyAddress keyword = new MyAddress();
        try {
            System.out.print("위치 키워드를 입력하세요 : ");
            keyword.setQuery(br.readLine());
            System.out.print("검색 반경을 입력하세요(1000:1km): ");
            keyword.setRadius(Integer.parseInt(br.readLine()));
            MyAddress locationKeyword = getlocation(keyword);
            bw.write("입력한 위치 키워드: " + keyword.getQuery());
            bw.newLine();
            bw.write("입력한 위치 키워드: " + keyword.getRadius());
            bw.newLine();

            if (locationKeyword != null) {
                List<Pharmacy> pharmacyList = getPharmacyList(locationKeyword);
                if (pharmacyList != null) {
                    Collections.sort(pharmacyList);
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n**약국 검색 결과**\n");
                    for (Pharmacy pharmacy : pharmacyList) {
                        sb.append("- 장소 URL(지도위치): " + pharmacy.getUrl() + "\n");
                        sb.append("- 상호명: " + pharmacy.getBusinessName() + "\n");
                        sb.append("- 주소: " + pharmacy.getAddress() + "\n");
                        sb.append("- 전화번호: " + pharmacy.getPhoneNumber() + "\n");
                        sb.append("- 거리(km): " + pharmacy.getDistance() / 1000 + "km\n");
                        sb.append("\n");
                    }
                    bw.write(sb.toString());
                    bw.flush();
                    while (true) {
                        System.out.print("kakaomap URL(장소 URL):");
                        String url = br.readLine();
                        if (url.equals("exit")) break;
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            try {
                                Desktop.getDesktop().browse(new URI(url));
                            } catch (IOException | URISyntaxException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("Desktop browsing is not supported on this platform.");
                        }
                    }
                }
            } else {
                bw.write("search failed");
            }
            bw.write("\n프로그램 종료");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                bw.flush();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static MyAddress getlocation(MyAddress keyword) {
        try {
            String query = keyword.getQuery().replaceAll("\\s", "");
            String requestURL = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + query;
            HttpClient client = HttpClientBuilder.create().build(); // HttpClient 생성
            HttpGet getRequest = new HttpGet(requestURL); //GET 메소드 URL 생성
            String personalApiKey = "751082c966e400b6acdc50efafa1ae57";
            getRequest.addHeader("Authorization", "KakaoAK " + personalApiKey);

            HttpResponse response = client.execute(getRequest);

            //Response 출력
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                String jsonResponse = EntityUtils.toString(entity);
                // JSON 데이터 파싱
                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONArray documents = jsonObject.getJSONArray("documents");
                JSONObject document = documents.getJSONObject(0);
                keyword.setLongitude(document.getString("x"));
                keyword.setLatitude(document.getString("y"));
                return keyword;
            } else {
                System.out.println("response is error : " + response.getStatusLine().getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Pharmacy> getPharmacyList(MyAddress locationKeyword) {
        try {
            String requestURL = "https://dapi.kakao.com/v2/local/search/category.json?category_group_code=PM9";
            String x = locationKeyword.getLongitude();
            String y = locationKeyword.getLatitude();
            String radius = String.valueOf(locationKeyword.getRadius());

            HttpClient client = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(requestURL + "&x=" + x + "&y=" + y + "&radius=" + radius);
            String personalApiKey = "751082c966e400b6acdc50efafa1ae57";
            getRequest.addHeader("Authorization", "KakaoAK " + personalApiKey);

            HttpResponse response = client.execute(getRequest);


            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                String jsonResponse = EntityUtils.toString(entity);

                // JSON 데이터 파싱
                JSONObject jsonObject = new JSONObject(jsonResponse);

                // 원하는 데이터 추출 (여기서는 documents 배열)
                JSONArray documents = jsonObject.getJSONArray("documents");
                List<Pharmacy> pharmacyList = new ArrayList<>();
                // documents 배열에서 필요한 정보 추출
                for (int i = 0; i < documents.length(); i++) {
                    JSONObject document = documents.getJSONObject(i);
                    Pharmacy pharmacy = new Pharmacy();
                    pharmacy.setUrl(document.getString("place_url"));
                    pharmacy.setBusinessName(document.getString("place_name"));
                    pharmacy.setAddress(document.getString("address_name"));
                    pharmacy.setPhoneNumber(document.getString("phone"));
                    pharmacy.setDistance(Float.valueOf(document.getString("distance")));
                    pharmacyList.add(pharmacy);
                }
                return pharmacyList;
            } else {
                System.out.println("response is error : " + response.getStatusLine().getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}