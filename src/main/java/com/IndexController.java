package com;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class IndexController {
	@RequestMapping(path = "/", method = RequestMethod.GET)
	public void index(@RequestParam(required = false) Map<String, Object> params,HttpServletResponse res) {
		try {
			res.setCharacterEncoding("utf8");
			res.setContentType("text/html");
			res.getOutputStream().write("<img width='200' height='200' src='/urobot/service/lgp?no=1'>登录码</img>".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
