package com;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.urule.URuleUtil;

@RestController
@RequestMapping("/urobot/service")
public class ServerController {
//	private static final Log logger = LogFactory.getLog(ServerController.class);

	@RequestMapping(path = "/test", method = RequestMethod.GET)
	@ResponseBody()
	public Object multiAction(@RequestParam(required = true) String no) {
		Map<String, Object> rs = new HashMap<String, Object>();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("content", no);
		Map<String, Object>rs2 = URuleUtil.getAnswer(params);
		//"我不想说话，请不要打扰我！";;
		for(String key : rs2.keySet()){
			System.out.println(rs2.get(key));
		}
		System.out.println("回话："+rs2.get("back"));
		rs.putAll(rs2);
		return rs;
	}

	@RequestMapping(path = "/lgp", method = RequestMethod.GET)
	@ResponseBody()
	public Object loginPic(@RequestParam(required = true) String no, HttpServletResponse res) {
		Map<String, String> rs = new HashMap<String, String>();
		File qrcode = new File("qrcode.png");
		System.out.println(qrcode.getAbsolutePath());
		res.setContentType("image/png");
		try {
			InputStream input = null;
			if(qrcode.exists()){
				input = new FileInputStream(qrcode);
			}
			if (input != null)
				IOUtils.copy(input, res.getOutputStream());
			res.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rs;
	}

}