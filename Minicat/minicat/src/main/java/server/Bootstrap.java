package server;

import lombok.Data;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Minicat的主类
 */
@Data
public class Bootstrap {

    /**
     * 定义socket监听的端口号
     */
    private int port;

    /**
     * Minicat 的程序启动入口
     *
     * @param args
     */
    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        try {
            // 启动Minicat
            bootstrap.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Minicat启动需要初始化展开的一些操作
     */
    public void start() throws Exception {
        // 加载解析相关的配置，server.xml, web.xml
        loadServer();
        // 定义一个线程池
        int corePoolSize = 10;
        int maximumPoolSize = 50;
        long keepAliveTime = 100L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(50);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,
                handler
        );

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("=====>>>Minicat start on port：" + port);
        // 多线程改造（使用线程池）
        while (true) {
            RequestProcessor requestProcessor = new RequestProcessor(serverSocket, mapper);
            threadPoolExecutor.execute(requestProcessor);
        }
    }

    private Mapper mapper = new Mapper();

    private void loadServer() {
        InputStream serverConf = this.getClass().getClassLoader().getResourceAsStream("server.xml");
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(serverConf);
            Element serverElement = document.getRootElement();
            List<Element> serviceNodes = serverElement.selectNodes("//Service");
            serviceNodes.forEach(serviceNode -> parseService(saxReader, serviceNode));
        } catch (Exception e) {

        }
    }

    private void parseService(SAXReader saxReader, Element serviceNode) {
        Element connectorElement = (Element) serviceNode.selectSingleNode("Connector");
        port = Integer.parseInt(connectorElement.attributeValue("port"));
        Element engineElement = (Element) serviceNode.selectSingleNode("Engine");
        Element hostElement = (Element) engineElement.selectSingleNode("Host");
        String hostName = hostElement.attributeValue("name");
        mapper.addHost(hostName, new Host());
        File appBaseDir = new File(hostElement.attributeValue("appBase"));
        File[] webApps = appBaseDir.listFiles();
        Arrays.stream(webApps)
                .filter(webApp -> webApp.isDirectory() && !webApp.isHidden())
                .forEach(webApp -> loadWebApp(saxReader, hostName, webApp));
    }

    private void loadWebApp(SAXReader saxReader, String hostName, File webApp) {
        try {
            String contextName = webApp.getName();
            mapper.addContext(hostName, contextName, new Context());
            File[] webConfArr = webApp.listFiles((dir, name) -> "web.xml".equals(name));
            if (Objects.isNull(webConfArr) || webConfArr.length == 0) {
                return;
            }
            File webConf = webConfArr[0];
            FileInputStream fileInputStream = new FileInputStream(webConf);
            Document webDoc = saxReader.read(fileInputStream);
            Element rootElement = webDoc.getRootElement();
            List<Element> servletNodes = rootElement.selectNodes("//servlet");
            servletNodes.forEach(element -> parseServlet(hostName, webApp, contextName, rootElement, element));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void parseServlet(String hostName, File webApp, String contextName, Element rootElement, Element element) {
        try {
            Element servletNameElement = (Element) element.selectSingleNode("servlet-name");
            String servletName = servletNameElement.getStringValue();
            Element servletClassElement = (Element) element.selectSingleNode("servlet-class");
            String servletClass = servletClassElement.getStringValue();
            Element servletMapping = (Element) rootElement.selectSingleNode("/web-app/servlet-mapping[servlet-name='" + servletName + "']");
            String urlPattern = servletMapping.selectSingleNode("url-pattern").getStringValue();
            URLClassLoader loader = new URLClassLoader(new URL[] { webApp.toURL()});
            Class<?> clazz = loader.loadClass(servletClass);
            mapper.addWrapper(hostName, contextName, urlPattern, new Wrapper((HttpServlet) clazz.newInstance()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
