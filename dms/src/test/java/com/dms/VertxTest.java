package com.dms;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VertxTest {

    private static Logger logger = LoggerFactory.getLogger(VertxTest.class);


    private static final int DELAY_SEC = 7;
    private static int totalRequests = 0;

    private static Vertx vertx;
    private static class InstagramRange {

        int followersFrom = 0;
        int followersTo = 10;

        int likesFrom = 0;
        int likesTo = 10;

        int commentsFrom = 0;
        int commentsTo = 10;

        int activityFrom = 0;
        int activityTo = 10;

        int step = 5;

    }


    private static Set<String> totalRecords = new LinkedHashSet<>();

    public static Set<String> readCsvLines(String response) throws IOException {
        CSVParser parser = CSVParser.parse(response, CSVFormat.newFormat(';').withQuote('\"'));

        return parser.getRecords().stream().map(CSVRecord::toString).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static void main(String[] args) {

        vertx = Vertx.vertx();
        scheduleContentDownload(vertx, new InstagramRange());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {

                logger.info("SAVE RECORDS TO FILE");
                try {
                    FileUtils.writeLines(new File("/Users/VolodymyrD/git/private/dms-prototype-app/dms/src/test/java/com/dms/tmpfile.csv"),totalRecords);
                } catch (IOException e) {
                    logger.error("Can't write lines", e);
                };
            }
        });
    }


    public static void scheduleContentDownload(Vertx vertx, InstagramRange range) {

        WebClient client = WebClient.create(Vertx.vertx(), getOptions());

        for (int follower = range.followersFrom; follower < range.followersTo; follower += range.step) {

            String followerRange = getRangeWithStep(follower, range.step);

            for (int like = range.likesFrom; like < range.likesTo; like += range.step) {

                String likRange = getRangeWithStep(like, range.step);

                for (int comments = range.commentsFrom; comments < range.commentsTo; comments += range.step) {

                    String commentsRange = getRangeWithStep(comments, range.step);

                    for (int activity = range.activityFrom; activity < range.activityTo; activity += range.step) {

                        ++totalRequests;

                        String activityRange = getRangeWithStep(activity, range.step);

                        vertx.setTimer(TimeUnit.SECONDS.toMillis(totalRequests * DELAY_SEC), handler -> downloadContent(client, followerRange, likRange, commentsRange, activityRange));
                    }
                }
            }
        }


        logger.info("Going to do total requests:" + totalRequests);
        logger.info("Full download will be finished in " + TimeUnit.SECONDS.toHours(totalRequests * DELAY_SEC) + "; hours");
    }


    private static String getRangeWithStep(int curr, int step) {
        return "" + curr + "-" + (curr + step);
    }

    /**
     * @param client
     * @param followerRange formant "x-y"
     * @param likesRange    format "x-y"
     * @param commentsRange format "x-y"
     * @param activityRange format "x-y"
     * @return
     */
    public static void downloadContent(WebClient client, String followerRange, String likesRange, String commentsRange, String activityRange) {
        HttpRequest<Buffer> getRequest = client.get(443, "pro.livedune.ru", "/influence/instagram");

        logger.info("Performing request with range followers[" + followerRange + "]; avg_likes[" + likesRange + "]; avg_comments[" + commentsRange + "], avg_activity[" + activityRange + "]");

        prepareHeader(getRequest)

                .addQueryParam("BusinessInstagram[followers_min]", followerRange)
                .addQueryParam("BusinessInstagram[avg_likes_min]", likesRange)
                .addQueryParam("BusinessInstagram[avg_comments_min]", commentsRange)
                .addQueryParam("BusinessInstagram[avg_activity_min]", activityRange)


                .addQueryParam("BusinessInstagram[description]", "")
                .addQueryParam("fields[]", "instagram_id")
                .addQueryParam("fields[]", "name")
                .addQueryParam("fields[]", "url")
                .addQueryParam("fields[]", "city")
                .addQueryParam("fields[]", "followers")
                .addQueryParam("fields[]", "following")
                .addQueryParam("fields[]", "photos")
                .addQueryParam("fields[]", "avg_likes")
                .addQueryParam("fields[]", "avg_comments")
                .addQueryParam("fields[]", "engagement")
                .addQueryParam("fields[]", "full_name")
                .addQueryParam("fields[]", "site")
                .addQueryParam("fields[]", "marks")
                .addQueryParam("fields[]", "bio")
                .addQueryParam("fields[]", "email")
                .addQueryParam("separator", "semicolon")
                .addQueryParam("export", "true")

                .send(resp -> {
                    if (resp.succeeded()) {
                        try {
                            String stringBody = resp.result().bodyAsString();

                            if (StringUtils.isNotBlank(stringBody) && !StringUtils.contains(stringBody, "По вашему запросу ничего не найдено")) {
                                Set<String> currentLines = readCsvLines(stringBody);

                                totalRecords.addAll(currentLines);
                                logger.info("Found records with request" + currentLines.size());
                                logger.info("Found total records = " + totalRecords.size());
                            } else {
                                logger.info("Found 0 records");
                            }
                            totalRequests--;
                            logger.info("Left do request:" + totalRequests);

                            if (totalRequests == 0) {
                                //download finished, will trigger shutdown hook and do persistence to file
                                logger.info("Download finished");
                                client.close();
                                vertx.close();
                                System.exit(0);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();

                        }
                    }
                });

    }

    public static WebClientOptions getOptions() {

        WebClientOptions options = new WebClientOptions();
        options.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36");
        options.setKeepAlive(false);
        options.setSsl(true);
        options.setTrustAll(true);
        options.setFollowRedirects(true);
        options.setMaxRedirects(15);
        options.setTryUseCompression(true);

        return options;

    }

    public static HttpRequest<Buffer> prepareHeader(HttpRequest<Buffer> httpRequest) {
        return httpRequest.putHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .putHeader("Content-Type", "text/csv;charset=UTF-8")
                .putHeader("accept-language", "en-US,en;q=0.9")
                .putHeader("cookie", "__cfduid=de5ad513ea7c20b6ec6bb9b205e75e3121521479030; _ga=GA1.2.1092994729.1521479053; _gid=GA1.2.1674094757.1521479053; _ym_uid=1521479053360980336; _ym_isad=2; _ga_cid=1092994729.1521479053; PHPSESSID=fo4ua39ovpi1eipu3babjj8ug5; b1837c52c5280deec7863eca69128541=d61d1f9fc105fcd41394ac87ed6db8517e50c6c5a%3A4%3A%7Bi%3A0%3Bs%3A6%3A%22222612%22%3Bi%3A1%3Bs%3A5%3A%22Guest%22%3Bi%3A2%3Bi%3A2592000%3Bi%3A3%3Ba%3A0%3A%7B%7D%7D; authorized=1; _gat=1");
    }

    public static MultiMap getFormParams() {
        MultiMap form = MultiMap.caseInsensitiveMultiMap();
        form.set("RegisterForm[username]", "kseniya.maifat@wedao.art");
        form.set("RegisterForm[password]", "Wedaoart2018");
        return form;

    }


}
