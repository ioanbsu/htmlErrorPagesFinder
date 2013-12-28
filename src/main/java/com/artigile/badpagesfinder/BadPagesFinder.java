package com.artigile.badpagesfinder;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Date: 11/9/13
 * Time: 10:07 AM
 *
 * @author ioanbsu
 */

public class BadPagesFinder {

    public static final Logger logger = Logger.getLogger(BadPagesFinder.class.getName());

    public static final int MAX_THREAD_COUNT = 10;
    public static final ExecutorService PROPOSALS_RE_INDEXER_THREAD = Executors.newFixedThreadPool(MAX_THREAD_COUNT);

    public static final int MAX_ANALYZED_PAGES = 5000;
    private static final String HOME_PAGE_STR = "HOME";
    private static final String TAB_SIGN = "   ";
    private static final int MAX_TIMEOUT_TO_WAIT = 5;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        if (args.length != 1) {
            logger.info("Please provide the website url that you want to analyze");
        }
        String websiteUrl = args[0];
        logger.info("====================================================================");
        logger.info("Starting analyzing " + websiteUrl);
        logger.info("====================================================================");


        BadPagesFinder badPagesFinder = new BadPagesFinder();
        badPagesFinder.analyzeWebsite(websiteUrl);

    }

    private void analyzeWebsite(String websiteMainPage) throws InterruptedException, ExecutionException {
        Set<String> alreadyInQueueSet = new HashSet<String>();
        Set<PageCheckResult> analyzedPages = new HashSet<PageCheckResult>();
        ArrayDeque<PageToAnalyze> pagesToRequestQueue = new ArrayDeque<PageToAnalyze>();
        List<Future<PageCheckResult>> resultsList = new ArrayList<Future<PageCheckResult>>();
        pagesToRequestQueue.add(new PageToAnalyze(websiteMainPage, HOME_PAGE_STR));
        do {
            if (analyzedPages.size() > MAX_ANALYZED_PAGES) {
                logger.info("Limit of analyzed pages reached: " + MAX_ANALYZED_PAGES);
                break;
            }
            PageAvailabilityChecker pageAvailabilityChecker;
            for (int i = 0; i < MAX_THREAD_COUNT && i < pagesToRequestQueue.size(); i++) {
                PageToAnalyze nextPageToAnalyze = pagesToRequestQueue.removeFirst();
                if (!analyzedPages.contains(new PageCheckResult(nextPageToAnalyze.getPage(), null, -1))) {
                    pageAvailabilityChecker = new PageAvailabilityChecker(nextPageToAnalyze.getPage(), websiteMainPage, nextPageToAnalyze.getPageCameFrom());
                    resultsList.add(PROPOSALS_RE_INDEXER_THREAD.submit(pageAvailabilityChecker));
                }
            }
            for (Future<PageCheckResult> pageCheckResultFuture : resultsList) {
                PageCheckResult pageAnalyzeResult;
                try {
                    pageAnalyzeResult = pageCheckResultFuture.get(MAX_TIMEOUT_TO_WAIT, TimeUnit.SECONDS);
                    Set<PageToAnalyze> nonQueuedPages = new HashSet<PageToAnalyze>();
                    for (String pageInNewResult : pageAnalyzeResult.getPagesUrls()) {
                        if (!alreadyInQueueSet.contains(pageInNewResult)) {
                            nonQueuedPages.add(new PageToAnalyze(pageInNewResult, pageAnalyzeResult.getOriginUrl()));
                            alreadyInQueueSet.add(pageInNewResult);
                        }
                    }
                    pagesToRequestQueue.addAll(nonQueuedPages);
                    analyzedPages.add(pageAnalyzeResult);
                    if (pageAnalyzeResult.getPageRequestCode() >= 400) {
                        printPagePath(analyzedPages, pageAnalyzeResult, TAB_SIGN);
                    }
                } catch (TimeoutException e) {
                    logger.info("timeout");
                    //   e.printStackTrace();
                }

            }
            resultsList.clear();
        } while (!pagesToRequestQueue.isEmpty());

        PROPOSALS_RE_INDEXER_THREAD.shutdown();
        logger.info("=======================================================");
        logger.info("=======================================================");
        logger.info("=======================================================");

    }

    private void printPagePath(Set<PageCheckResult> analyzedPages, PageCheckResult pageToStartFrom, String tab) {
        if (HOME_PAGE_STR.equals(pageToStartFrom.getWhereItCameFrom())) {
            System.out.println(tab + HOME_PAGE_STR);
            System.out.println("=======================================================================");
            return;
        }
        for (PageCheckResult analyzedPage : analyzedPages) {
            if (pageToStartFrom.getWhereItCameFrom().equals(analyzedPage.getOriginUrl())) {
                System.out.println(tab + " " + pageToStartFrom.getPageRequestCode() + ": " + pageToStartFrom.getOriginUrl());
                printPagePath(analyzedPages, analyzedPage, tab + TAB_SIGN);
            }
        }
    }

}
