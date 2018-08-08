package com.bazzi.pre.mapper;

import com.bazzi.core.generic.BaseMapper;
import com.bazzi.pre.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

	List<User> selectByLikeKey(@Param("keyword") String keyword);

}
