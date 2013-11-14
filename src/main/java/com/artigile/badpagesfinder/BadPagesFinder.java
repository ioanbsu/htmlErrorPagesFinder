package com.artigile.badpagesfinder;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 11/9/13
 * Time: 10:07 AM
 *
 * @author ioanbsu
 */

public class BadPagesFinder {
    public final String URL_PATTERN = "\\s*(?i)href\\s*=\\s*(\\\"([^\"]*)\\\"|'[^']*'|([^'\">\\s]+))";
    private String mainPageUrl;
    private Logger logger = Logger.getLogger(BadPagesFinder.class.getName());


    private Set<String> checkedPages = new HashSet<>();

    public static void main(String[] args) {
        BadPagesFinder badPagesFinder = new BadPagesFinder(args[0]);
        badPagesFinder.startAnalyzing();
    }

    public BadPagesFinder(String mainPageUrl) {
        boolean testMainPageUrlCorrect = true;
        if (!testMainPageUrlCorrect) {
            throw new IllegalArgumentException("The specified website url is not correct");
        }
        this.mainPageUrl = mainPageUrl;
    }

    private void startAnalyzing() {
        analyzePages(mainPageUrl, mainPageUrl);
    }

    private void analyzePages(String pageUrl, String howToGet) {
        Set<String> urlsList = getAllLinksOnAPage(pageUrl, howToGet);
        if (checkedPages.size() > MAX_PAGES_TO_ANALYZE()) {
            return;
        }
        for (String newPageUrl : urlsList) {
            analyzePages(newPageUrl, howToGet + "->" + newPageUrl);
        }
    }

    private int MAX_PAGES_TO_ANALYZE() {
        return 10000;
    }

    private Set<String> getAllLinksOnAPage(String requestUrl, String howToGet) {
        Set<String> pagesUrls = new HashSet<>();
        HttpURLConnection connection = null ;
        try {
            connection = (HttpURLConnection) new URL(requestUrl).openConnection();
            //pretending that we are browser :)
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:6.0) Gecko/20100101 Firefox/6.0");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
            connection.setRequestProperty("Accept-Encoding", "sdch");
            connection.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            int code = connection.getResponseCode();
            if (code >= 400) {
                System.out.println(code + ": \n" + requestUrl + ": " + howToGet);
            } else if (requestUrl.startsWith(mainPageUrl)) {
                connection.connect();
                Pattern urlPattern = Pattern.compile(URL_PATTERN);
                String pageStringRepresentation = CharStreams.toString(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8));
                Matcher urlMatcher = urlPattern.matcher(pageStringRepresentation);
                while (urlMatcher.find()) {
                    String matchedUrl = urlMatcher.group(2);
                    if (matchedUrl != null) {
                        try {
                            String pageUrl = URLDecoder.decode(matchedUrl, "UTF-8").toLowerCase();
                            if (!checkedPages.contains(pageUrl)) {
                                pagesUrls.add(pageUrl);
                                checkedPages.add(pageUrl);
                            }
                        } catch (Exception e) {
                            logger.warning("The url was not decoded: " + matchedUrl);
                        }
                    }
                }
            }
        } catch (IOException e) {
            checkedPages.add(requestUrl);
        }
        return pagesUrls;
    }
}
