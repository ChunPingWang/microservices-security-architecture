package com.ecommerce.logistics.domain.value_objects;

/**
 * Enum representing supported logistics carriers.
 */
public enum Carrier {

    BLACK_CAT("黑貓宅急便", "https://www.t-cat.com.tw/Inquire/Trace.aspx?BillID={trackingNumber}"),
    HSINCHU_LOGISTICS("新竹物流", "https://www.hct.com.tw/search/searchgoods_detail.aspx?no={trackingNumber}"),
    SEVEN_ELEVEN("7-11 超商取貨", "https://eservice.7-11.com.tw/e-tracking/search.aspx?odno={trackingNumber}"),
    FAMILY_MART("全家超商取貨", "https://www.famiport.com.tw/Web_Famiport/page/queryResult.aspx?sn={trackingNumber}"),
    HI_LIFE("萊爾富超商取貨", "https://www.hilife.com.tw/serviceCenter/queryLogistics.aspx"),
    POST_OFFICE("中華郵政", "https://postserv.post.gov.tw/pstmail/main_mail.html?targetTranId=DC002&{trackingNumber}"),
    SF_EXPRESS("順豐速運", "https://www.sf-express.com/tw/tc/dynamic_function/waybill/#search/bill-number/{trackingNumber}");

    private final String displayName;
    private final String trackingUrlPattern;

    Carrier(String displayName, String trackingUrlPattern) {
        this.displayName = displayName;
        this.trackingUrlPattern = trackingUrlPattern;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTrackingUrlPattern() {
        return trackingUrlPattern;
    }

    /**
     * Generates the tracking URL for a given tracking number.
     */
    public String getTrackingUrl(String trackingNumber) {
        return trackingUrlPattern.replace("{trackingNumber}", trackingNumber);
    }
}
