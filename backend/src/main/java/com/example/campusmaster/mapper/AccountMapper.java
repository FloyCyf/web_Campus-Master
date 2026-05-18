package com.example.campusmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusmaster.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    @Select("SELECT * FROM account WHERE user_id = #{userId} AND deleted = 0")
    Account selectByUserId(@Param("userId") Long userId);
}
