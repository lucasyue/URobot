package com;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.scienjus.smartqq.client.SmartQQClient;

@RestController
@RequestMapping("/")
public class IndexController {

	@RequestMapping(path = "/", method = RequestMethod.GET)
	public void index(@RequestParam(required = false) Map<String, Object> params, HttpServletResponse res) {
		try {
			res.setCharacterEncoding("utf8");
			res.setContentType("text/html");
			String loginDiv = "<div id='loginDiv'><img id='loginImg' width='200' height='200' src='/urobot/service/lgp?no=1'></img><div style='text-align:center;width:200;'>扫码登录</div></div>"
					+ "<div><a href='/urule/frame' style='text-decoration:none;'>设计交谈</a></div>"
					+ getJob();
			res.getOutputStream().write(loginDiv.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(path = "/watchLogin", method = RequestMethod.GET)
	@ResponseBody
	public Object watchLogin(@RequestParam(required = false) Map<String, Object> params, HttpServletResponse res) {
		Map<String, Object> rs = new HashMap<String, Object>();
		if (SmartQQClient.LOGIN_FLAG) {
			rs.put("lgSuc", true);
		}
		res.setContentType("application/json");
		return rs;
	}

	private String getJob() {
		String job = "<script>var job = setInterval(function(){"
				+ "fetch('/watchLogin',{method:'GET',headers:{Accept:'application/json',}}).then(function(res){"
				+ "debugger;if(res.ok){"
				+ "res.json().then(json=>{"
				+ "if(json.lgSuc){"
				+ "document.getElementById('loginDiv').innerHTML='登录成功，服务已启用，<a href=\"javascript:window.opener=null;op=window.open(\"\",\"_self\");op.close();\">关闭</a>！';stopJob();"
				+ "}"				
				+ "});"
				+ "}"
				+ "}"
				+ ")"
				+ "}, 3000);"
				+ "function stopJob(){clearInterval(job);}"
				+ "</script>";
		return job;
	}
}
