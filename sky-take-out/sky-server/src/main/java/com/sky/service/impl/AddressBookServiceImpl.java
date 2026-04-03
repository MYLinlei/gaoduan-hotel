package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.exception.AddressBookBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    @Transactional
    public void save(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        if (Integer.valueOf(1).equals(addressBook.getIsDefault())) {
            addressBookMapper.clearDefaultByUserId(userId);
        } else if (isFirstAddress(userId)) {
            addressBook.setIsDefault(1);
        }
        addressBookMapper.insert(addressBook);
    }

    @Override
    public List<AddressBook> list() {
        AddressBook query = new AddressBook();
        query.setUserId(BaseContext.getCurrentId());
        return addressBookMapper.list(query);
    }

    @Override
    public AddressBook getById(Long id) {
        return requireOwnedAddress(id);
    }

    @Override
    @Transactional
    public void update(AddressBook addressBook) {
        AddressBook current = requireOwnedAddress(addressBook.getId());
        if (Integer.valueOf(1).equals(addressBook.getIsDefault())) {
            addressBookMapper.clearDefaultByUserId(current.getUserId());
        }
        addressBookMapper.update(addressBook);
    }

    @Override
    public void deleteById(Long id) {
        requireOwnedAddress(id);
        addressBookMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void setDefault(AddressBook addressBook) {
        AddressBook current = requireOwnedAddress(addressBook.getId());
        addressBookMapper.clearDefaultByUserId(current.getUserId());
        AddressBook updateAddress = new AddressBook();
        updateAddress.setId(current.getId());
        updateAddress.setIsDefault(1);
        addressBookMapper.update(updateAddress);
    }

    @Override
    public AddressBook getDefault() {
        return addressBookMapper.getDefaultByUserId(BaseContext.getCurrentId());
    }

    private AddressBook requireOwnedAddress(Long id) {
        AddressBook addressBook = addressBookMapper.getByIdAndUserId(id, BaseContext.getCurrentId());
        if (addressBook == null) {
            throw new AddressBookBusinessException("地址不存在");
        }
        return addressBook;
    }

    private boolean isFirstAddress(Long userId) {
        Integer count = addressBookMapper.countByUserId(userId);
        return count == null || count == 0;
    }
}
