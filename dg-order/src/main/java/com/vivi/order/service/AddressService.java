package com.vivi.order.service;

import com.vivi.entity.UserInfo;
import com.vivi.order.interceptor.UserInterceptor;
import com.vivi.order.mapper.AddressMapper;
import com.vivi.order.pojo.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class AddressService {
    @Autowired
    private AddressMapper addressMapper;


    public void deleteAddress(Long addressId) {
        UserInfo userInfo = UserInterceptor.getThreadLocal();
        Example example = new Example(Address.class);
        example.createCriteria().andEqualTo("userId",userInfo.getId()).andEqualTo("id",addressId);
        this.addressMapper.deleteByExample(example);
    }


    public void updateAddressByUserId(Address address) {
        UserInfo userInfo = UserInterceptor.getThreadLocal();
        address.setUserId(userInfo.getId());
        setDefaultAddress(address);
        this.addressMapper.updateByPrimaryKeySelective(address);

    }


    public List<Address> queryAddressByUserId() {
        UserInfo userInfo = UserInterceptor.getThreadLocal();
        Example example = new Example(Address.class);
        example.createCriteria().andEqualTo("userId",userInfo.getId());
        return this.addressMapper.selectByExample(example);
    }


    public void addAddressByUserId(Address address) {
        UserInfo userInfo = UserInterceptor.getThreadLocal();
        address.setUserId(userInfo.getId());
        setDefaultAddress(address);
        this.addressMapper.insert(address);
    }


    public Address queryAddressById(Long addressId) {
        UserInfo userInfo = UserInterceptor.getThreadLocal();
        Example example = new Example(Address.class);
        example.createCriteria().andEqualTo("id",addressId).andEqualTo("userId",userInfo.getId());
        return this.addressMapper.selectByExample(example).get(0);
    }

    public void setDefaultAddress(Address address){
        if (address.getDefaultAddress()){
            //如果将本地址设置为默认地址，那么该用户下的其他地址都应该是非默认地址
            List<Address> addressList = this.queryAddressByUserId();
            addressList.forEach(addressTemp -> {
                if (addressTemp.getDefaultAddress()){
                    addressTemp.setDefaultAddress(false);
                    this.addressMapper.updateByPrimaryKeySelective(addressTemp);
                }
            } );
        }
    }
}
