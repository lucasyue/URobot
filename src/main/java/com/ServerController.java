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

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/urobot/service")
public class ServerController {
//	private static final Log logger = LogFactory.getLog(ServerController.class);

	@SuppressWarnings("unchecked")
	@RequestMapping(path = "/multiaction", method = RequestMethod.POST)
	@ResponseBody()
	public Object multiAction(@RequestParam(required = true) Map<String, Object> params) {
		Map<String, String> rs = new HashMap<String, String>();
		String xxx = (String) params.get("xxx");
		if (xxx != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				params = mapper.readValue(xxx, Map.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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