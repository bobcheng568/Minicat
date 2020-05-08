package server;

import lombok.AllArgsConstructor;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

@AllArgsConstructor
public class RequestProcessor extends Thread {

    private Socket socket;
    private Mapper mapper;

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            // 封装Request对象和Response对象
            Request request = new Request(inputStream);
            Response response = new Response(socket.getOutputStream());
            Wrapper wrapper = mapper.getWrapper(request);
            // 静态资源处理
            if (Objects.isNull(wrapper)) {
                response.outputHtml(request.getUrl());
                socket.close();
                return;
            }
            // 动态资源servlet请求
            HttpServlet httpServlet = wrapper.getServlet();
            httpServlet.service(request, response);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
