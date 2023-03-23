package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 * filterName：过滤器在对象容器中的名字
 * urlPatterns：匹配路径
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public boolean check(String[] urls, String requestURI) {
        for(String url : urls){
            if(PATH_MATCHER.match(url,requestURI))
                return true;
        }
        log.info("Flase");
        return false;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;  //获取session中值
        HttpServletResponse response = (HttpServletResponse) servletResponse; //重定向
        long id = Thread.currentThread().getId();
        log.info("LoginFilter 线程id为：{}",id);

        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();

//        {}占位符
        log.info("接受到的请求路径:{}", requestURI);
//        filterChain.doFilter(servletRequest,servletResponse);

        //定义不需要处理的请求路径--白名单
        String[] urls = new String[]{
                "/employee/login",  // 登陆页 直接放行
                "/employee/logout",//注销页 直接放行
                "/backend/**", //所有的pc端的 前端资源 放行
                "/front/**", //所有的移动端 前端资源 放行
                "/common/**",
                "/user/login",
                "/user/sendMsg",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs",

        };


        boolean check = check(urls, requestURI);

        if (check) {
            //         filterChain.doFilter 放行
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }


        HttpSession session = request.getSession();
        Object employee = session.getAttribute("employee");
        Object user=session.getAttribute("user");

        if (employee != null) {

            BaseContext.setCurrentId((Long) employee);
            log.info("用户已经登录，直接放行");

            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }
}