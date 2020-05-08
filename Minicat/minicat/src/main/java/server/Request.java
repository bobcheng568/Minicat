package server;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

/**
 * 把请求信息封装为Request对象（根据InputSteam输入流封装）
 */
@Data
@NoArgsConstructor
public class Request {

    private String host;
    private String context;
    private String url;  // 例如 /,/index.html
    private String method; // 请求方式，比如GET/POST
    private InputStream inputStream;  // 输入流，其他属性从输入流中解析出来

    /**
     * 构造器，输入流传入
     *
     * @param inputStream
     * @throws IOException
     */
    public Request(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        // 从输入流中获取请求信息
        int count = 0;
        while (count == 0) {
            count = inputStream.available();
        }
        byte[] bytes = new byte[count];
        inputStream.read(bytes);

        String inputStr = new String(bytes);
        // 获取第一行请求头信息
        String[] lines = inputStr.split("\r\n");
        String firstLineStr = lines[0];  // GET /demo1/lagou HTTP/1.1
        String secondLineStr = lines[1];  // Host: localhost:8080
        this.host = secondLineStr.replace("Host: ", "").split(":")[0];
        String[] strings = firstLineStr.split(" ");
        this.method = strings[0];
        String substring = strings[1].substring(1);
        int i = substring.indexOf("/");
        this.context = substring.substring(0, i);
        this.url = substring.substring(i);

        System.out.println("=====>>host:" + host);
        System.out.println("=====>>context:" + context);
        System.out.println("=====>>url:" + url);
        System.out.println("=====>>method:" + method);
    }

}
