package mSearch.tool;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class MVHttpClient {
    private final static MVHttpClient ourInstance = new MVHttpClient();
    //private static final Cache cache = new Cache(new File("./cache"), 1024L * 1024L * 200L);
    private final OkHttpClient httpClient;
    private final OkHttpClient copyClient;

    private MVHttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(100, 1, TimeUnit.SECONDS))
                .build();
        //.cache(cache).build();
        httpClient.dispatcher().setMaxRequests(100);

        copyClient = httpClient.newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS).build();
    }

    public static MVHttpClient getInstance() {
        return ourInstance;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public OkHttpClient getReducedTimeOutClient() {
        return copyClient;
    }
}
