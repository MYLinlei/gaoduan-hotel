package com.sky.mapper;

import com.sky.entity.SkuStockLog;
import com.sky.entity.SkuStockReservation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SkuInventoryMapper {

    @Update("update product_sku set locked_stock = locked_stock + #{quantity}, version = version + 1 " +
            "where id = #{skuId} and status = 1 and is_deleted = 0 " +
            "and stock - locked_stock >= #{quantity}")
    int reserveSku(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    @Update("update product_sku set stock = stock - #{quantity}, locked_stock = locked_stock - #{quantity}, " +
            "version = version + 1 where id = #{skuId} and is_deleted = 0 " +
            "and stock >= #{quantity} and locked_stock >= #{quantity}")
    int confirmSku(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    @Update("update product_sku set locked_stock = locked_stock - #{quantity}, version = version + 1 " +
            "where id = #{skuId} and is_deleted = 0 and locked_stock >= #{quantity}")
    int releaseLockedSku(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    @Update("update product_sku set stock = stock + #{quantity}, version = version + 1 " +
            "where id = #{skuId} and is_deleted = 0")
    int restoreConfirmedSku(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    @Insert("insert into sku_stock_reservation " +
            "(order_id, order_number, product_id, sku_id, quantity, status, create_time, update_time) " +
            "values (#{orderId}, #{orderNumber}, #{productId}, #{skuId}, #{quantity}, #{status}, #{createTime}, #{updateTime})")
    int insertReservation(SkuStockReservation reservation);

    @Select("select * from sku_stock_reservation where order_id = #{orderId} order by id for update")
    List<SkuStockReservation> listByOrderIdForUpdate(Long orderId);

    @Update("update sku_stock_reservation set status = #{targetStatus}, update_time = now() " +
            "where id = #{id} and status = #{expectedStatus}")
    int transitionReservation(@Param("id") Long id,
                              @Param("expectedStatus") Integer expectedStatus,
                              @Param("targetStatus") Integer targetStatus);

    @Insert("insert into sku_stock_log " +
            "(order_id, order_number, product_id, sku_id, change_type, stock_delta, locked_stock_delta, " +
            "before_stock, after_stock, before_locked_stock, after_locked_stock, create_time) " +
            "values (#{orderId}, #{orderNumber}, #{productId}, #{skuId}, #{changeType}, #{stockDelta}, " +
            "#{lockedStockDelta}, #{beforeStock}, #{afterStock}, #{beforeLockedStock}, #{afterLockedStock}, #{createTime})")
    int insertLog(SkuStockLog stockLog);
}
