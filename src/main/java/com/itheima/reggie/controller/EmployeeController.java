package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        log.info("登录用户{}",employee);
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if(emp == null){
            return R.error("没有此用户");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
    @PostMapping
    public  R<String> save(@RequestBody Employee employee, HttpServletRequest request){
        log.info("用户的基本信息：{}",employee.toString());
//        1. 设置默认密码 123456   存密文 e10adc3949ba59abbe56e057f20f883e
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        2. 设置默认状态 为启动
        employee.setStatus(1);
//        3. 创建时间 和修改时间 一致
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        4. 创建者 和修改者 一致
//        创建者id 已经在登陆的时候，存到Session，取出即可使用
        HttpSession session = request.getSession();
//        employee.setUpdateUser((Long) session.getAttribute("employee"));
//        employee.setCreateUser((Long) session.getAttribute("employee"));
//       5. 执行保存用户的操作
        boolean savestatus = employeeService.save(employee);
        return savestatus?R.success("添加用户成功"):R.error("添加用户失败，请稍后再试");

    }

    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name){
        log.info("page:"+page+"pageSize:"+pageSize);
        Page<Employee> employeePage=new Page<>(page,pageSize);
        QueryWrapper<Employee>  queryWrapper= new QueryWrapper<>();
        queryWrapper.like(name!=null,"name",name).or().like(name!=null,"username",name);
        employeeService.page(employeePage,queryWrapper);
        return R.success(employeePage);
    }
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("update 线程id为：{}",id);
        Long empId = (Long)request.getSession().getAttribute("employee");

//        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }
}