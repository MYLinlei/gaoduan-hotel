package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.DishCommentDTO;
import com.sky.dto.DishNoteDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishComment;
import com.sky.entity.DishFavorite;
import com.sky.entity.DishFlavor;
import com.sky.entity.DishLike;
import com.sky.entity.DishNote;
import com.sky.exception.BaseException;
import com.sky.mapper.DishCommentMapper;
import com.sky.mapper.DishFavoriteMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishLikeMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishNoteMapper;
import com.sky.result.PageResult;
import com.sky.service.DishInteractionService;
import com.sky.vo.DishCommentVO;
import com.sky.vo.DishNoteVO;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DishInteractionServiceImpl implements DishInteractionService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private DishNoteMapper dishNoteMapper;

    @Autowired
    private DishCommentMapper dishCommentMapper;

    @Autowired
    private DishLikeMapper dishLikeMapper;

    @Autowired
    private DishFavoriteMapper dishFavoriteMapper;

    @Override
    public DishVO getDetail(Long dishId) {
        Dish dish = getDishOrThrow(dishId);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(dishId);
        dishVO.setFlavors(flavors);

        Long userId = BaseContext.getCurrentId();
        dishVO.setLiked(dishLikeMapper.getByDishIdAndUserId(dishId, userId) != null);
        dishVO.setFavorited(dishFavoriteMapper.getByDishIdAndUserId(dishId, userId) != null);
        return dishVO;
    }

    @Override
    @Transactional
    public boolean toggleLike(Long dishId) {
        getDishOrThrow(dishId);
        Long userId = BaseContext.getCurrentId();
        DishLike current = dishLikeMapper.getByDishIdAndUserId(dishId, userId);
        if (current == null) {
            dishLikeMapper.insert(DishLike.builder().dishId(dishId).userId(userId).build());
            dishMapper.updateLikeCount(dishId, 1);
            return true;
        }
        dishLikeMapper.deleteByDishIdAndUserId(dishId, userId);
        dishMapper.updateLikeCount(dishId, -1);
        return false;
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long dishId) {
        getDishOrThrow(dishId);
        Long userId = BaseContext.getCurrentId();
        DishFavorite current = dishFavoriteMapper.getByDishIdAndUserId(dishId, userId);
        if (current == null) {
            dishFavoriteMapper.insert(DishFavorite.builder().dishId(dishId).userId(userId).build());
            dishMapper.updateFavoriteCount(dishId, 1);
            return true;
        }
        dishFavoriteMapper.deleteByDishIdAndUserId(dishId, userId);
        dishMapper.updateFavoriteCount(dishId, -1);
        return false;
    }

    @Override
    public PageResult favoritePage(Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Page<Dish> records = dishMapper.pageFavoriteByUserId(BaseContext.getCurrentId());
        List<DishVO> dishVOList = records.getResult().stream().map(this::toDishVO).toList();
        return new PageResult(records.getTotal(), dishVOList);
    }

    @Override
    public List<DishNoteVO> listNotes(Long dishId) {
        getDishOrThrow(dishId);
        return dishNoteMapper.listVisibleByDishId(dishId);
    }

    @Override
    @Transactional
    public void publishNote(DishNoteDTO dishNoteDTO) {
        Dish dish = getDishOrThrow(dishNoteDTO.getDishId());
        if (isBlank(dishNoteDTO.getTitle())) {
            throw new BaseException("种草标题不能为空");
        }
        if (isBlank(dishNoteDTO.getContent())) {
            throw new BaseException("种草内容不能为空");
        }

        DishNote dishNote = DishNote.builder()
                .dishId(dish.getId())
                .userId(BaseContext.getCurrentId())
                .title(dishNoteDTO.getTitle().trim())
                .content(dishNoteDTO.getContent().trim())
                .images(trimToNull(dishNoteDTO.getImages()))
                .liked(0)
                .status(1)
                .isFeatured(0)
                .build();
        dishNoteMapper.insert(dishNote);
        dishMapper.updateNoteCount(dish.getId(), 1);
    }

    @Override
    public PageResult commentPage(Long dishId, Integer page, Integer pageSize) {
        getDishOrThrow(dishId);
        PageHelper.startPage(page, pageSize);
        Page<DishCommentVO> records = dishCommentMapper.pageVisibleByDishId(dishId);
        return new PageResult(records.getTotal(), records.getResult());
    }

    @Override
    @Transactional
    public void publishComment(DishCommentDTO dishCommentDTO) {
        Dish dish = getDishOrThrow(dishCommentDTO.getDishId());
        if (isBlank(dishCommentDTO.getContent())) {
            throw new BaseException("评价内容不能为空");
        }
        BigDecimal score = normalizeScore(dishCommentDTO.getScore());
        DishComment dishComment = DishComment.builder()
                .dishId(dish.getId())
                .orderId(dishCommentDTO.getOrderId())
                .userId(BaseContext.getCurrentId())
                .parentId(dishCommentDTO.getParentId())
                .content(dishCommentDTO.getContent().trim())
                .score(score)
                .liked(0)
                .status(1)
                .build();
        dishCommentMapper.insert(dishComment);
        BigDecimal avgScore = dishCommentMapper.avgScoreByDishId(dish.getId());
        dishMapper.updateCommentStats(dish.getId(), 1, avgScore == null ? new BigDecimal("5.00") : avgScore);
    }

    private Dish getDishOrThrow(Long dishId) {
        if (dishId == null) {
            throw new BaseException("菜品不存在");
        }
        Dish dish = dishMapper.getById(dishId);
        if (dish == null) {
            throw new BaseException("菜品不存在");
        }
        return dish;
    }

    private DishVO toDishVO(Dish dish) {
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavorMapper.getByDishId(dish.getId()));
        dishVO.setLiked(dishLikeMapper.getByDishIdAndUserId(dish.getId(), BaseContext.getCurrentId()) != null);
        dishVO.setFavorited(true);
        return dishVO;
    }

    private BigDecimal normalizeScore(BigDecimal score) {
        BigDecimal current = score == null ? new BigDecimal("5.00") : score;
        if (current.compareTo(BigDecimal.ONE) < 0 || current.compareTo(new BigDecimal("5.00")) > 0) {
            throw new BaseException("评分必须在1到5之间");
        }
        return current;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
