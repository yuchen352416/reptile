package com.striveonger.demo.reptile;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.hutool.core.util.CharsetUtil.CHARSET_UTF_8;

/**
 * 盗墓笔记小说网
 * http://www.dmbj.cc/
 */
public class TombRobbery {

    public void run(String url, String path) {
        HttpRequest request = HttpUtil.createGet(url);
        HttpResponse response = request.execute();
        String body = response.body();
        Map<String, Map<String, String>> directorys = analyzeDirectory(body);
        for (Map.Entry<String, Map<String, String>> volume : directorys.entrySet()) {
            // 写入卷名
            FileUtil.appendString(volume.getKey() + "\n\n", path, CHARSET_UTF_8);
            // 遍历卷中章节
            for (Map.Entry<String, String> chapter : volume.getValue().entrySet()) {
                FileUtil.appendString(chapter.getKey() + "\n", path, CHARSET_UTF_8);
                FileUtil.appendString(analyzeChapterContent(chapter.getValue()), path, CHARSET_UTF_8);
                System.out.println(chapter.getKey());
            }
        }

    }

    /**
     * 解析目录
     * @param body
     * @return
     */
    private Map<String, Map<String, String>> analyzeDirectory(String body) {
        Document doc = Jsoup.parse(body);
        Elements elements = doc.select("div.title");
        Map<String, Map<String, String>> directorys = new LinkedHashMap<>();
        for (Element element : elements) {
            Map<String, String> directory = element.nextElementSibling().select("li").stream().collect(Collectors.toMap(Element::text, x -> x.getElementsByTag("a").attr("href"), (x, y) -> x, LinkedHashMap::new));
            directorys.put(element.select("H3").text(), directory);
        }
        return directorys;
    }

    /**
     * 解析章节内容
     * @param url
     * @return
     */
    private String analyzeChapterContent(String url) {
        HttpRequest request = HttpUtil.createGet(url);
        HttpResponse response = request.execute();
        String body = response.body();
        Document doc = Jsoup.parse(body);
        return doc.select(".m-post").select("p").stream().map(Element::text).collect(Collectors.joining("\n")) + "\n\n";
    }


}
