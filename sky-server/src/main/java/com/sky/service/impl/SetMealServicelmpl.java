package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SetMealServicelmpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;
    /**
     * 新增套餐
     * @param setmealDTO
     */
    public void saveWithDish(SetmealDTO setmealDTO){
        log.info("新增套餐,{}",setmealDTO);
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);

        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }
    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealDishMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }
    /**
     * 批量删除套餐
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids){
        log.info("批量删除套餐,{}",ids);
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if(StatusConstant.ENABLE == setmeal.getStatus()){
                // 套餐状态为启用状态，不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
            ids.forEach(setmealId -> {
                // 删除套餐表中数据
                setmealMapper.deleteById(setmealId);
                // 删除套餐菜品关联表中数据
                setmealDishMapper.deleteBySetmealId(setmealId);
            });
        });
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    public SetmealVO getById(Long id){
        Setmeal setmeal = setmealMapper.getById(id);
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 更新套餐
     * @param setmealDTO
     */
    @Transactional
    public void update(SetmealDTO setmealDTO){
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        // 更新套餐表
        setmealMapper.update(setmeal);
        // 删除套餐菜品关联表中数据
        Long setmealId = setmeal.getId();
        setmealDishMapper.deleteBySetmealId(setmealId);
        // 新增套餐菜品关联表中数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }
    /**
     * 启用或停用
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id){
        if(status == StatusConstant.ENABLE){
            //判断套餐是否有关联的停售菜品
            List<Dish> dishes = dishMapper.getBySetmealId(id);
            if(dishes != null && !dishes.isEmpty()){
                dishes.forEach(dish -> {
                    if(StatusConstant.DISABLE == dish.getStatus()){
                        throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }
}
