package com.alaka_ala.florafilm.ui.util.coreMatrix.api.model;

import com.google.gson.annotations.SerializedName;

public class BTSettings {

    @SerializedName("cacheSize")
    private long cacheSize;

    @SerializedName("connectionsLimit")
    private int connectionsLimit;

    @SerializedName("disableDHT")
    private boolean disableDHT;

    @SerializedName("disablePEX")
    private boolean disablePEX;

    @SerializedName("disableTCP")
    private boolean disableTCP;

    @SerializedName("disableUPNP")
    private boolean disableUPNP;

    @SerializedName("disableUTP")
    private boolean disableUTP;

    @SerializedName("disableUpload")
    private boolean disableUpload;

    @SerializedName("downloadRateLimit")
    private int downloadRateLimit;

    @SerializedName("enableDLNA")
    private boolean enableDLNA;

    @SerializedName("enableDebug")
    private boolean enableDebug;

    @SerializedName("enableIPv6")
    private boolean enableIPv6;

    @SerializedName("enableRutorSearch")
    private boolean enableRutorSearch;

    @SerializedName("forceEncrypt")
    private boolean forceEncrypt;

    @SerializedName("friendlyName")
    private String friendlyName;

    @SerializedName("peersListenPort")
    private int peersListenPort;

    @SerializedName("preloadCache")
    private int preloadCache;

    @SerializedName("readerReadAHead")
    private int readerReadAHead;

    @SerializedName("removeCacheOnDrop")
    private boolean removeCacheOnDrop;

    @SerializedName("responsiveMode")
    private boolean responsiveMode;

    @SerializedName("retrackersMode")
    private int retrackersMode;

    @SerializedName("sslCert")
    private String sslCert;

    @SerializedName("sslKey")
    private String sslKey;

    @SerializedName("sslPort")
    private int sslPort;

    @SerializedName("torrentDisconnectTimeout")
    private int torrentDisconnectTimeout;

    @SerializedName("torrentsSavePath")
    private String torrentsSavePath;

    @SerializedName("uploadRateLimit")
    private int uploadRateLimit;

    @SerializedName("useDisk")
    private boolean useDisk;

    // Getters
    public long getCacheSize() { return cacheSize; }
    public int getConnectionsLimit() { return connectionsLimit; }
    public boolean isDisableDHT() { return disableDHT; }
    public boolean isDisablePEX() { return disablePEX; }
    public boolean isDisableTCP() { return disableTCP; }
    public boolean isDisableUPNP() { return disableUPNP; }
    public boolean isDisableUTP() { return disableUTP; }
    public boolean isDisableUpload() { return disableUpload; }
    public int getDownloadRateLimit() { return downloadRateLimit; }
    public boolean isEnableDLNA() { return enableDLNA; }
    public boolean isEnableDebug() { return enableDebug; }
    public boolean isEnableIPv6() { return enableIPv6; }
    public boolean isEnableRutorSearch() { return enableRutorSearch; }
    public boolean isForceEncrypt() { return forceEncrypt; }
    public String getFriendlyName() { return friendlyName; }
    public int getPeersListenPort() { return peersListenPort; }
    public int getPreloadCache() { return preloadCache; }
    public int getReaderReadAHead() { return readerReadAHead; }
    public boolean isRemoveCacheOnDrop() { return removeCacheOnDrop; }
    public boolean isResponsiveMode() { return responsiveMode; }
    public int getRetrackersMode() { return retrackersMode; }
    public String getSslCert() { return sslCert; }
    public String getSslKey() { return sslKey; }
    public int getSslPort() { return sslPort; }
    public int getTorrentDisconnectTimeout() { return torrentDisconnectTimeout; }
    public String getTorrentsSavePath() { return torrentsSavePath; }
    public int getUploadRateLimit() { return uploadRateLimit; }
    public boolean isUseDisk() { return useDisk; }
}
