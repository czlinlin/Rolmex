/**
 * 
 */
package com.mossle.H5.cms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.mossle.H5.work.WorkTaskUtils;

/**
 * @author Bing
 *
 */
@Service
public class BulletinService {

	private JdbcTemplate jdbcTemplate;

	public Map<String, Object> Bulletins(Map<String, Object> decryptedMap) {
		// {strPageSize=5, strPerCode=2, percode=2, method=Bulletins,
		// sign=e458da11bb70689df863747cb2020546, strPageIndex=1,
		// timestamp=}

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("percode") || !decryptedMap.containsKey("strPageIndex")
					|| !decryptedMap.containsKey("strPageSize")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			// 获取数据================================================
			long pageIndex = Long.valueOf(decryptedMap.get("strPageIndex").toString());
			long row_count = Long.valueOf(decryptedMap.get("strPageSize").toString());
			long offset = row_count * (pageIndex - 1);
			Object percode = decryptedMap.get("percode");

			// 查数据
			
			//20181109 chengze 替换视图
			//String sql = "SELECT * FROM v_h5_cms_article WHERE id in(SELECT cms_id FROM cms_range WHERE FIND_IN_SET(party_id,f_party_path(?))) and ((NOW() > NT07 and NOW() < NT08) OR NT07 is NULL )LIMIT ?,?";
			
			String sql = "SELECT * FROM ("+WorkTaskUtils.getH5CmsArticle()+")  t WHERE id in(SELECT cms_id FROM cms_range WHERE FIND_IN_SET(party_id,f_party_path(?))) and ((NOW() > NT07 and NOW() < NT08) OR NT07 is NULL )LIMIT ?,?";
			
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,
					new Object[] { percode, offset, row_count });

			// 返回======================================================================
			returnMap.put("bSuccess", "true");

			if (list.size() > 0) {
				returnMap.put("strMsg", "加载成功");
				returnMap.put("Bulletins", list);
			} else {
				returnMap.put("strMsg", "暂无数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
		}

		return returnMap;
	}

	public Map<String, Object> BulletinDetail(Map<String, Object> decryptedMap) {
		// strLID=

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strLID")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			// 获取数据================================================
			Object strLID = decryptedMap.get("strLID");

			// 查数据
			
			//20181109 chengze  替换视图
			
			//String sql = "SELECT * FROM v_h5_cms_article where id=?";
			
			String sql = "SELECT * FROM ("+WorkTaskUtils.getH5CmsArticle()+" ) t where id=?";
			
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, new Object[] { strLID });

			// 返回======================================================================
			returnMap.put("bSuccess", "true");

			if (list.size() > 0) {
				returnMap.put("strMsg", "加载成功");
				returnMap.put("BulletinDetail", list);
			} else {
				returnMap.put("strMsg", "暂无数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
		}

		return returnMap;
	}

	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
