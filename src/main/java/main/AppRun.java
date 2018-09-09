package main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AppRun {
    public static void main(String[] args) {
        Document doc;

        if (args.length < 1) {
            System.out.println("Specify the int number argument!");
            return;
        }

        try {
            doc = Jsoup.connect("https://yandex.ru/pogoda/" + args[0]).get();
        } catch (IOException e) {
            System.out.println("No such city found!");
            return;
            //e.printStackTrace();
        }

        printSomething("title:", getTitle(doc));
        printSomething("description:", getDescription(doc));
        printSomething("time:", getFactTime(doc));
        printSomething("fact temp:", getFactTemp(doc));
        printSomething("other temps:", getOtherTemps(doc));
    }

    private static Function<Elements, List<String>> getTextFromElements = x -> {
        List<String> strings = new ArrayList<>();
        for (Element e : x) {
            strings.add(e.text());
        }
        return strings;
    };

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
        System.out.println("> " + title.toUpperCase());
        System.out.println(content);
    }

    public static String getFactTemp(Document doc) {
        Elements factTemp = doc.getElementsByClass("fact__temp");
        return factTemp.select("span").text();
    }

    public static String getFactTime(Document doc) {
        Elements factTime = doc.getElementsByClass("fact__time");
        return factTime.text();
    }

    public static String getTitle(Document doc) {
        return doc.title();
    }

    public static String getDescription(Document doc) {
        return doc.getElementsByAttributeValue("name", "description").attr("content");
    }
}
