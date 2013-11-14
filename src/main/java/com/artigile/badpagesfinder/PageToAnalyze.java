package com.artigile.badpagesfinder;

/**
 * User: ioanbsu
 * Date: 11/14/13
 * Time: 2:11 PM
 */
public class PageToAnalyze {

    private String page;

    private String pageCameFrom;

    public PageToAnalyze() {
    }

    public PageToAnalyze(String page, String pageCameFrom) {
        this.page = page;
        this.pageCameFrom = pageCameFrom;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getPageCameFrom() {
        return pageCameFrom;
    }

    public void setPageCameFrom(String pageCameFrom) {
        this.pageCameFrom = pageCameFrom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageToAnalyze that = (PageToAnalyze) o;

        if (page != null ? !page.equals(that.page) : that.page != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return page != null ? page.hashCode() : 0;
    }
}
