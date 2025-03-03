package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private static final String PATTERN = "dish:category:*";

    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        // 清除缓存
        String key = "dish:category:" + dishDTO.getCategoryId();
//        redisTemplate.delete(key);
        CleanCache(key);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @ApiOperation("查询菜品")
    @GetMapping("/page")
    public Result page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询菜品{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @ApiOperation("批量删除菜品")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除菜品{}",ids);
        dishService.deleteBatch(ids);
        // 清除缓存
//        Set keys = redisTemplate.keys("dish:category:*");
//        redisTemplate.delete(keys);
        CleanCache(PATTERN);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @ApiOperation("根据id查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> get(@PathVariable Long id) {
        log.info("查询菜品{}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("修改菜品")
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("更新菜品{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        // 清除缓存
        //由于修改分类，会导致两个缓存数据都发生变化，这里直接删除所有缓存
//        Set keys = redisTemplate.keys("dish:category:*");
//        redisTemplate.delete(keys);
        CleanCache(PATTERN);
        return Result.success();
    }
    /**
     * 根据套餐id查询菜品
     * @param categoryId
     */
    @ApiOperation("根据套餐id查询菜品")
    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }
    /**
     * 启用或停用套餐
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("启用或停用套餐")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id){
        dishService.startOrStop(status, id);
        // 清除缓存
        CleanCache(PATTERN);
        return Result.success();
    }

    /**
     * 清除缓存
     * @param pattern
     */
    private void CleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
