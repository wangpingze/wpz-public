package cn.xiaomi.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ConfigProcess {
	/**
	 * 读取配置文件
	 * 
	 * @return 返回bool值，标志是否读取文件成功
	 */
	public static HashMap<String, String>  readConfig(String configPath) {

		System.out.println(new java.util.Date() + "\tRead config...");
		HashMap<String, String> configs = new java.util.HashMap<String, String>();
		try {
			BufferedReader tr = new BufferedReader(new InputStreamReader(
					new FileInputStream(configPath), Encoding.utf8));

			
			String aline;
			while ((aline = tr.readLine()) != null) {
				aline = aline.trim();
				if (!aline.equals("")) {
					if (aline.startsWith("//")) {
						continue;
					}
					String[] kk = aline.split("::=", -1);
					if (kk.length != 2) {
						continue;
					}
					configs.put(kk[0].trim(), StringProcess.trim(kk[1], '\"',
							';', ' ', '\t', '“', '”','；'));
				}
			}
			tr.close();
		} catch (Exception exp) {
			return null;
		}
		return configs;

	}
}