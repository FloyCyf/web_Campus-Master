
package com.example.campusmaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusmaster.common.BusinessException;

/**
 * 通用Service实现类
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    /**
     * 根据ID查询，不存在则抛出异常
     */
    public T getByIdOrThrow(Long id) {
        T entity = getById(id);
        if (entity == null) {
            throw BusinessException.notFound("数据不存在");
        }
        return entity;
    }

    /**
     * 分页查询
     */
    public IPage<T> pageQuery(Page<T> page, Wrapper<T> queryWrapper) {
        return baseMapper.selectPage(page, queryWrapper);
    }
}
