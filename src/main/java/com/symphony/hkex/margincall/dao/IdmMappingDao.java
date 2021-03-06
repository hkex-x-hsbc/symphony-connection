package com.symphony.hkex.margincall.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class IdmMappingDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public String getSymphonyIDForCallTypeD() {
        return jdbcTemplate.queryForObject("select SYMPHONY_ID from t_idm_room_mapping_d where rownum < 2", String.class);
    }

    public List<Map<String, Object>> getSymphonyIDForCallTypeC(String... stockCode) {
        if (stockCode.length < 1) {
            return new ArrayList<>();
        }
        StringBuffer sql = new StringBuffer("select STOCK_CODE, SYMPHONY_ID from t_idm_room_mapping_c where stock_code in ( ");
        for (int i = 0; i < stockCode.length; i++) {
            if (i == 0) {
                sql.append("?");
            } else {
                sql.append(",?");
            }
        }
        sql.append(" )");
        return jdbcTemplate.queryForList(sql.toString(), stockCode);
    }

    public int saveHKEXChatBotSymphonyID(String symphonyID) {
        return jdbcTemplate.update("update t_idm_hkex_bot_mapping set symphony_id = ? ", symphonyID);
    }

    public String getHKEXChatBotSymphonyID() {
        return jdbcTemplate.queryForObject("select symphony_id from t_idm_hkex_bot_mapping", String.class);
    }


}
