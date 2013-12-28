package com.artigile.badpagesfinder;

import java.util.HashSet;
import java.util.Set;

/**
 * User: ioanbsu
 * Date: 11/14/13
 * Time: 11:20 AM
 */
public class PageCheckResult {

    private String originUrl;

    private String whereItCameFrom="";

    private Set<String> pagesUrls=new HashSet<String>();

    private int pageRequestCode =-1;

    public PageCheckResult() {
    }

    public PageCheckResult(String originUrl, Set<String> pagesUrls, int pageRequestCode) {
        this.originUrl = originUrl;
        this.pagesUrls = pagesUrls;
        this.pageRequestCode = pageRequestCode;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public Set<String> getPagesUrls() {
        return pagesUrls;
    }

    public void setPagesUrls(Set<String> pagesUrls) {
        this.pagesUrls = pagesUrls;
    }

    public int getPageRequestCode() {
        return pageRequestCode;
    }

    public void setPageRequestCode(int pageRequestCode) {
        this.pageRequestCode = pageRequestCode;
    }

    public String getWhereItCameFrom() {
        return whereItCameFrom;
    }

    public void setWhereItCameFrom(String whereItCameFrom) {
        this.whereItCameFrom = whereItCameFrom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageCheckResult that = (PageCheckResult) o;

        if (originUrl != null ? !originUrl.equals(that.originUrl) : that.originUrl != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return originUrl != null ? originUrl.hashCode() : 0;
    }
}
