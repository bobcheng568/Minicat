# Minicat

**讲解视频及简答题地址**：

链接: https://pan.baidu.com/s/1p3XCGenzr0WqkOyc5NW1iw 提取码: 43c7



## 简述Tomcat体系结构

**如果图片看不到，可以直接去看上面网盘链接里面文档**

![https://github.com/bobcheng568/Minicat/tree/master/Minicat/minicat/src/main/resources/static/Tomcat体系结构图.png](https://github.com/bobcheng568/Minicat/tree/master/Minicat/minicat/src/main/resources/static/Tomcat体系结构图.png)


从本质上讲，Tomcat是一个servlet/JSP容器。

Server代表整个容器(container)。它可以包含一个或多个Service，还可以包含一个GlobalNamingResources。

Service中可以含有一个或多个Connector，但只能含有一个Engine。这使得不同的Connector可以共享同一个Engine。同一个Server中的多个Service之间没有相关性。
Service的实现类StandardService调用容器（Container）接口，其实就是调用了Servlet Engine。
Container(容器)：Engine、Host、Context和Wrapper均继承自Container接口，所以他们都是容器。不过他们是有父子关系的，Engine是顶级容器，直接包含是Host容器，而Host容器又包含Context容器，所以Engine、Host和Context从大小上来说又构成父子关系,虽然它们都继承自Container接口。

Engine负责接收和处理来自它所属的Service中的所有Connector的请求。

Host表示一个虚拟主机，并和一个服务器的网络名关联。注意Engine中必须有一个Host的名字和Engine的defaultHost属性匹配。
Context表示在虚拟主机中运行的web应用程序。一个虚拟主机中能够运行多个Context，它们通过各自的Context Path进行相互区分。如果Context Path为""，那么该web应用为该虚拟主机的默认的web应用。

Connector将Service和Container连接起来，首先它需要注册到一个Service，它的作用就是把来自客户端的请求转发到Container。比较常见的两个是HTTP Connector和AJP Connector。
