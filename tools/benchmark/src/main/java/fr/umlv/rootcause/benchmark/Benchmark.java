package fr.umlv.rootcause.benchmark;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.IntStream;

public class Benchmark {

    public static void main(String[] args) throws InterruptedException {
        //use this to try out spamming rabbitmq with different parameters
        List<String> stringList = new ArrayList<>();
        int batchSize = 3000;
        IntStream.range(0, batchSize)
                .forEach(i -> stringList.add("2020-06-15\t12:49:50\tGET\t12:49:50\tGET\t12:49:50\tGET\t12:49:50\tGET\t12:49:50\tGET\t12:49:50\tGET\t12:49:50\tGET\t12:49:50\tGET\t12:49:50\tGET\t12:49:50\t12:49:50\t12:49:50\t12:49:50\t12:49:50\t12:49:50\t12:49:50\tGET\t12:49:50\tGET\t123.123.123.123\t123.123.123.123\t123.123.123.123123.123.123.123\t123.123.123.123\tGET\t12:49:50\tGET\t123.123.123.123\t123.123.123.123\t123.123.123.123123.123.123.123\t123.123.123.123\t123.123.123.123\t123.123.123.123\t123.123.123.123123.123.123.123\t2001:0db8:85a3:0000:0000:8a2e:0370:7334\t2001:0db8:85a3:0000:0000:8a2e:0370:7334\t2001:0db8:85a3:0000:0000:8a2e:0370:7334\t2001:0db8:85a3:0000:0000:8a2e::\t2001:0db8:85a3::8a2e:0370:7334\t2001:0db8:85a3::8a2e:0370:7334\t2001:0db8:85a3::8a2e:0370:7334\t2001:0db8:85a3::8a2e:0370:7334\t2001:0db8:85a3::8a2e:0370:7334\t2001:0db8:85a3::8a2e:0370:7334\t2001:0db8:85a3::8a2e:0370:7334\t2001:0db8:85a3::8a2e:0370:7334\t2001:0db8:85a3::8a2e:0370:7334\t2001:0db8:85a3::8a2e:0370:7334\t2001:0db8:85a3::8a2e:0370:7334\t2001:0db8:85a3::8a2e:0370:7334"));

        String URITarget = "http://localhost:8081/insertlog";
        int threadCount = 50;
        int delay = 500;

        sendToServer(stringList, threadCount, batchSize, delay, URITarget);
    }

    private static void sendToServer(List<String> stringSource,
                                     int threadCount,
                                     int batchSize,
                                     int millisDelay,
                                     String URITarget) throws InterruptedException {

        Thread[] threads = new Thread[threadCount];
        ArrayList<List<String>> subLists = new ArrayList<>();

        for (int i = 0; i < threadCount; i++){
            int offset = stringSource.size() / threadCount;
            subLists.add(stringSource.subList(offset*i, offset*(i+1)));
            int finalI = i;
            threads[i] = new Thread(() -> {
                BenchHTTPClient client;
                client = new BenchHTTPClient(batchSize, URITarget);
                client.sendToServer(subLists.get(finalI),millisDelay, true);
            });
        }

        for(Thread t : threads) {
            t.start();
            Thread.sleep(millisDelay/threadCount);
        }

        for(Thread t : threads) {
            t.join();
        }
    }
}

class BenchHTTPClient{
    private final HttpClient httpClient;
    private final int batchSize;
    private final URI sendURI;

    public BenchHTTPClient(int batchSize, String URITarget) {
        this.httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).followRedirects(HttpClient.Redirect.NORMAL).build();
        this.batchSize = batchSize;
        this.sendURI = URI.create(URITarget);
    }

    public void sendPost(URI uri, List<String> stringList) throws IOException, InterruptedException {
        String str = prepareRequest(stringList);
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(str))
                .header("Accept", "application/json").header("Content-Type", "application/json").build();
        HttpResponse<String> send = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if(send.statusCode() != 200) {
            System.out.println(send.statusCode());
        }
    }

    private String prepareRequest(List<String> stringList) {
        String prefix = "[{\"log\":\"";
        String separator = "\"},{\"log\":\"";
        String postfix = "\"}]";
        StringBuilder sb = new StringBuilder();

        sb.append(prefix);
        for (String s : stringList){
            sb.append(s.replace("\t","\\t"));
            sb.append(separator);
        }
        sb.setLength(sb.length() - separator.length());
        sb.append(postfix);
        return sb.toString();
    }

    public void sendToServer(List<String> stringList, int millisDelay, boolean loop) {
        do {
            try {
                if (stringList.size() > batchSize) {
                    for (List<String> list : cutList(stringList, batchSize)) {
                        this.sendPost(sendURI, list);
                        Thread.sleep(millisDelay);
                    }
                } else {
                    this.sendPost(sendURI, stringList);
                    Thread.sleep(millisDelay);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } while(loop);
    }
    private List<List<String>> cutList(List<String> originalList, int partitionSize) {
        List<List<String>> partitions = new ArrayList<>();
        for (int i = 0; i < originalList.size(); i += partitionSize) {
            partitions.add(originalList.subList(i,
                    Math.min(i + partitionSize, originalList.size())));
        }
        return partitions;
    }
}