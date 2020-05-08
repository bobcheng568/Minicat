package server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bobcheng
 * @date 2020/5/6
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Wrapper {

    private HttpServlet servlet;

}
