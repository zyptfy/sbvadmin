package com.shenfangtao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shenfangtao.mapper.PermissionMapper;
import com.shenfangtao.model.Permission;
import com.shenfangtao.service.PermissionService;
import com.shenfangtao.service.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

/**
 * Notes:
 * Author: 涛声依旧 likeboat@163.com
 * Time: 2022/7/26 17:19
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
    @Autowired
    PermissionMapper permissionMapper;

    @Override
    public List<Permission> getAllPermissions() {
        return permissionMapper.getAllPermissions();
    }

    /**
     * Notes:  获得用户的菜单
     * @param: [id]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * Author: 涛声依旧 likeboat@163.com
     * Time: 2022/9/19 16:38
     **/
    @Override
    public List<Map<String, Object>> getMenusByUid(BigInteger id) {
        List<Permission> permissionList = getPermissionsByUid(id);
        List<Permission> allPermissionList = getAllPermissions();
        List<Permission> fathers = new ArrayList<>();

        // 找到所有父节点菜单
        for (Permission permission : permissionList) {
            AuthUtil.findFathers(permission,allPermissionList,fathers);
        }

        // 去掉按钮
        List<Permission> menuList = new ArrayList<Permission>();
        for (Permission permission : fathers) {
            if (permission.getType() != 2){
                menuList.add(permission);
            }
        }

        // 生成目录结构
        List<Map<String, Object>> treeMenu = AuthUtil.findChildren(0L,menuList);
        return treeMenu;
    }

    @Override
    public List<String> getCodesByUid(BigInteger id) {
        List<Permission> permissionList = getPermissionsByUid(id);
        List<String> codeList = new ArrayList<String>();
        for (Permission permission : permissionList) {
            if (permission.getType() == 2){
                codeList.add(AuthUtil.genPermissionRule(permission));
            }
        }
        return codeList;
    }

    @Override
    public List<Permission> getPermissionsByUid(BigInteger id) {
        List<Permission> permissionList = permissionMapper.getPermissionsByUid(id);
        return permissionList;
    }

    @Override
    public List<Map<String, Object>> getAllPermissionsAsTree() {
        List<Permission> permissionList = this.list();
        List<Map<String, Object>> treePermissions = AuthUtil.findChildren(0L,permissionList);
        return treePermissions;
    }

//    public List<Permission> getAllPermissions() {
//        List<Permission> permissionList = this.list();
//        return permissionList;
//    }

}
