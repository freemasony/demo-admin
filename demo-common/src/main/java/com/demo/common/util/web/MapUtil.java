package com.demo.common.util.web;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by a on 14-12-4.
 */
public class MapUtil {
    public static String printMap(Map map) {
        if (map == null || map.size() == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        for (Object key : map.keySet()) {
            try {
                String[] value = (String[])map.get(key);
                StringBuilder str = new StringBuilder();
                if(!key.toString().equals("topicId")){//有个接口，ios客户端传了两次topicId这个参数，为了日志打印不出现两次，就只能在这先做限制了。
                    for(String v:value) {
                        str.append(v);
                        str.append(",");
                    }
                }
                String tmp=value[0];
                if(value!=null&&value.length>1&&!key.toString().equals("topicId"))
                    tmp=str.toString().substring(0, str.toString().length()-1);

                sb.append(key).append(":").append((value==null||value.length==0)?"":tmp).append(",");
            }catch(Exception e) {
                sb.append(key).append(":").append(map.get(key)).append(",");
            }
        }
        if (sb.length()>=1)
            sb.delete(sb.length()-1, sb.length());
        return sb.toString();
    }

	private static String getParameterMap(Map<String, String[]> map)
	{
		StringBuilder sb = new StringBuilder();
		Set<Map.Entry<String, String[]>> set = map.entrySet();
		Iterator<Map.Entry<String, String[]>> it = set.iterator();
		while (it.hasNext())
		{
			Map.Entry<String, String[]> entry = it.next();
			sb.append(entry.getKey() + ":");
			for (String i : entry.getValue())
			{
				sb.append(i).append(" ");
			}
			sb.append(",");
		}
		return sb.toString();
	}

    public static Map MapValueArrayToObject(Map map) {
        if (map == null || map.size() == 0)
            return null;
        Map newMap = new HashMap();
        for (Object key : map.keySet()) {
            try {
                String[] value = (String[])map.get(key);
                StringBuilder str = new StringBuilder();
                for(String v:value) {
                    str.append(v);
                    str.append(",");
                }

                String tmp=value[0];
                if(value!=null||value.length>1)
                    tmp=str.toString().substring(0, str.toString().length()-1);

                newMap.put(key,(value==null||value.length==0)?"":tmp);
            }catch(Exception e) {
                newMap.put(key,map.get(key));
            }
        }
        return newMap;
    }

}
