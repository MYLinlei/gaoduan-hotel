package com.sky.service;

import com.sky.dto.DishCommentDTO;
import com.sky.dto.DishNoteDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishNoteVO;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishInteractionService {

    DishVO getDetail(Long dishId);

    boolean toggleLike(Long dishId);

    boolean toggleFavorite(Long dishId);

    PageResult favoritePage(Integer page, Integer pageSize);

    List<DishNoteVO> listNotes(Long dishId);

    void publishNote(DishNoteDTO dishNoteDTO);

    PageResult commentPage(Long dishId, Integer page, Integer pageSize);

    void publishComment(DishCommentDTO dishCommentDTO);
}
