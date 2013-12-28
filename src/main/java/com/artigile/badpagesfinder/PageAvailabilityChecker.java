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
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: ioanbsu
 * Date: 11/14/13
 * Time: 11:18 AM
 */
public class PageAvailabilityChecker implements Callable<PageCheckResult> {

    public static final Logger logger= Logger.getLogger(PageAvailabilityChecker.class.getName());

    public final String URL_PATTERN = "\\s*(?i)href\\s*=\\s*(\\\"([^\"]*)\\\"|'[^']*'|([^'\">\\s]+))";

    private String requestUrl;
    private String mainPageUrl;
    private String whereItCamefrom;


    public PageAvailabilityChecker(String requestUrl, String mainPageUrl, String whereItCamefrom) {
        this.requestUrl = requestUrl;
        this.mainPageUrl = mainPageUrl;
        this.whereItCamefrom = whereItCamefrom;
    }

    @Override
    public PageCheckResult call() throws Exception {
        PageCheckResult resultOfACall = new PageCheckResult();
        resultOfACall.setOriginUrl(requestUrl);
        resultOfACall.setWhereItCameFrom(whereItCamefrom);
        HttpURLConnection connection = null;
        try {
            connection = initConnection();
            int code = connection.getResponseCode();
            resultOfACall.setPageRequestCode(code);
            if (code >= 200 && code <= 300 && requestUrl.startsWith(mainPageUrl)) {
                connection.connect();
                resultOfACall.setPagesUrls(collectPagesUrls(connection));
            } else {
//                System.out.println("Skipping page analyze: " + requestUrl);
            }
        } catch (IOException e) {
            //e.printStackTrace();
        } catch (Exception e) {
           // e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return resultOfACall;
    }

    private Set<String> collectPagesUrls(HttpURLConnection connection) throws IOException {
        Set<String> externalPagesUrls = new HashSet<String>();
        Pattern urlPattern = Pattern.compile(URL_PATTERN);
        String pageStringRepresentation = CharStreams.toString(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8));
        Matcher urlMatcher = urlPattern.matcher(pageStringRepresentation);
        while (urlMatcher.find()) {
            String foundUrl = urlMatcher.group(2);
            if (foundUrl != null) {
                if (foundUrl.startsWith("/") || foundUrl.startsWith("#")) {
                    foundUrl = mainPageUrl + foundUrl;
                }
                externalPagesUrls.add(URLDecoder.decode(foundUrl, "UTF-8").replace(" ","+"));
            }
        }
        return externalPagesUrls;
    }

    private HttpURLConnection initConnection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(requestUrl).openConnection();
        //pretending that we are browser :)
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:6.0) Gecko/20100101 Firefox/6.0");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
        connection.setRequestProperty("Accept-Encoding", "sdch");
        connection.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        return connection;
    }
}
