package main;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simplest program that displays information about the weather on the console from the site yandex.ru/pogoda/...
 *
 * Using: Jsoup, Apache HttpClient.
 *
 * Command line parameters: city code, for example 51 for Samara, 55 for Tyumen and so on.
 *
 * @author Denis Tyurin
 *
*/
public class AppRun {
    public static void main(String[] args) {
        Document doc;

        if (args.length < 1) {
            System.out.println("Specify the int number argument!");
            return;
        }

        //doc = getDocumentByJsoup("https://yandex.ru/pogoda/" + args[0]);
        doc = getDocumentByApacheHttpClient("https://yandex.ru/pogoda/" + args[0]);

        if (doc == null) {
            System.out.printf("City with code '%s' is not found or something else does not work out!\n", args[0]);
            return;
        }

        printSomething("title:", getTitle(doc));
        printSomething("description:", getDescription(doc));
        printSomething("time:", getFactTime(doc));
        printSomething("fact temp:", getFactTemp(doc));
        printSomething("other temps:", getOtherTemps(doc));
    }

    private static Document getDocumentByJsoup(String url) {
        Document doc;

        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            return null;
        }
        return doc;
    }

    private static Document getDocumentByApacheHttpClient(String url) {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response;

        try {
            response = client.execute(request);
        } catch (IOException e) {
            return null;
        }

        StringBuilder textView = new StringBuilder();
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            while ((line = br.readLine()) != null) {
                textView.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return Jsoup.parse(textView.toString());
    }

    /**
    * This method combines two ArrayList<String> into Map<String, String>, where the first List is the key of Map,
    * and the second List - the value of the Map
    */
    private static String getOtherTemps(Document doc) {
        Map<String, String> stringStringMap = new LinkedHashMap<>();
        int i = 0;
        for (Element e : doc.getElementsByClass("term__label")) {
            if (!e.text().equals("")) {
                stringStringMap.put(Integer.toString(i++), e.text());
            }
        }
        i = 0;
        for (Element e : doc.getElementsByClass("term__value")) {
            String s = stringStringMap.get(Integer.toString(i));
            if (s != null) {
                stringStringMap.put(s, e.text());
                stringStringMap.remove(Integer.toString(i++));
            }
        }
        return stringStringMap.toString();
    }

    private static void printSomething(String title, String content) {
        System.out.println(">> " + title.toUpperCase());
        System.out.printf("%s\n\n", content);
    }

    private static String getFactTemp(Document doc) {
        Elements factTemp = doc.getElementsByClass("fact__temp");
        return factTemp.select("span").text();
    }

    private static String getFactTime(Document doc) {
        Elements factTime = doc.getElementsByClass("fact__time");
        return factTime.text();
    }

    private static String getTitle(Document doc) {
        return doc.title();
    }

    private static String getDescription(Document doc) {
        return doc.getElementsByAttributeValue("name", "description").attr("content");
    }
}
