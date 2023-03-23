package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.entity.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface DishMapper extends BaseMapper<Dish> {
    Page<DishDto> getDishDtoList(Page<DishDto> page, String name);
}