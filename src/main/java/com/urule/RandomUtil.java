package com.urule;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.bstek.urule.model.library.action.annotation.ActionBean;
import com.bstek.urule.model.library.action.annotation.ActionMethod;
import com.bstek.urule.model.library.action.annotation.ActionMethodParameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
@ActionBean(name = "随机")
public class RandomUtil {
	@ActionMethod(name = "生成一个随机数")
	@ActionMethodParameter(names = { "最大整数" })
	public String random(int scope) {
		double r = Math.random();
		int rm = (int) (scope * r);
		return rm + "";
	}

	@ActionMethod(name = "讲一个笑话")
	public String getJoke() {
		String path = RandomUtil.class.getResource("/jokes.json").getPath();
		double r = Math.random();
		int rm = (int) (100 * r);
		if (rm > 85) {
			rm = 80;
		}
		String s = rm + "";
		try {
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			JsonParser parser = new JsonParser();
			JsonObject jsonObj = (JsonObject) parser.parse(br);
			br.close();
			String joke = jsonObj.get(s).getAsString();
			return joke;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "It's a joke！";
	}

	public static void main(String[] args) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		String path = RandomUtil.class.getResource("/jokes.json1.txt").getPath();
		FileInputStream fis = new FileInputStream(path);
		InputStreamReader isr = new InputStreamReader(fis, "utf8");
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		String numStr = "";
		while ((line = br.readLine()) != null) {
			if (StringUtils.hasText(line)) {
				String firstThreeChar = line.substring(0, 3);
				if (StringUtils.hasText(firstThreeChar) && firstThreeChar.indexOf(".") != -1) {
					numStr = firstThreeChar.substring(0, firstThreeChar.indexOf("."));
					line = line.substring(numStr.length() + 1);
				}
				if (map.containsKey(numStr)) {
					map.put(numStr, map.get(numStr) + line);
				} else {
					map.put(numStr, line);
				}
			}
		}
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
		String jsonRs = gson.toJson(map);
		System.out.println(jsonRs);
		Set<String> keys = map.keySet();
		for (String key : keys) {
			System.out.println(map.get(key));
		}
		br.close();
	}
}
