package com.sbvadmin.controller;

import com.sbvadmin.common.service.JwtTokenService;
import com.sbvadmin.model.Role;
import com.sbvadmin.model.User;
import com.sbvadmin.model.UserInfo;
import com.sbvadmin.service.impl.PermissionServiceImpl;
import com.sbvadmin.service.impl.UserServiceImpl;
import com.sbvadmin.service.utils.CommonService;
import com.sbvadmin.service.utils.CommonUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notes: 权限，用户信息相关
 * Author: 涛声依旧 likeboat@163.com
 * Time: 2022/8/26 20:10
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class AuthController {

    @Autowired
    UserServiceImpl userService;

    @Autowired
    PermissionServiceImpl permissionService;

    @Autowired
    JwtTokenService jwtTokenService;

    @Autowired
    CommonService commonService;

    /**
     * Notes:  解决访问必须带index.html的问题
     * @param: []
     * @return: org.springframework.web.servlet.ModelAndView
     * Author: 涛声依旧 likeboat@163.com
     * Time: 2022/9/29 21:54
     **/
    @GetMapping("/")
    public ModelAndView index(){
        return new ModelAndView("redirect:/index.html");
    }
    /**
     * Notes:  获取个人信息
     * @param: []
     * @return: com.sbvadmin.model.UserInfo
     * Author: 涛声依旧 likeboat@163.com
     * Time: 2022/9/5 21:30
     **/
    @GetMapping("/getUserInfo")
    public UserInfo getUserInfo(){
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setAvatar(commonService.getAvatarUrl(user.getAvatar())); // TIPS: 拼接服务器公网路径
        userInfo.setHomePath(user.getHomePath()); // 登录后去到的第一个页面路由
        userInfo.setRealName(user.getNickname());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        List<Role> roles = userService.getUserRolesByUid(user.getId());
        userInfo.setRoles(roles);
        userInfo.setToken((String)authentication.getCredentials());
        return userInfo;
    }

    /**
     * Notes:  获得个人所拥有的菜单
     * @param: []
     * @return: java.util.Map
     * Author: 涛声依旧 likeboat@163.com
     * Time: 2022/9/5 21:43
     **/
    @GetMapping("/getMenuList")
    public List<Map<String, Object>> getMenuList(){
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return permissionService.getMenusByUid(user.getId());
    }

    /**
     * Notes:  获得按钮权限点
     * @param: []
     * @return: java.util.List
     * Author: 涛声依旧 likeboat@163.com
     * Time: 2022/9/6 16:54
     **/
    @GetMapping("/getPermCode")
    public List getPermCode(){
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return permissionService.getCodesByUid(user.getId());
    }

    /**
     * Notes:  刷新token，目前给小程序用
     * @param: []
     * @return: java.lang.String
     * Author: 涛声依旧 likeboat@163.com
     * Time: 2023/4/11 10:26
     **/
    @PostMapping("/refreshToken")
    public String refreshToken(HttpServletRequest request){
        String jwtToken = request.getHeader("authorization");
        log.info("jwtToken:"+ jwtToken);
        if (jwtToken != null && jwtToken != "") {
            jwtToken = jwtToken.replace("Bearer", "");
//            if (!jwtTokenUtil.isTokenExpired(jwtToken)) { // 可以随时刷新token，待考虑 TODO
            Claims claims = jwtTokenService.parserToken(jwtToken);
//                String username = claims.getSubject();//获取当前登录用户名
//                List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList((String) claims.get("authorities"));
            Long uid = Long.valueOf(String.valueOf(claims.get("uid")));
            User user = userService.getById(uid);
            StringBuffer as = new StringBuffer();
//                for (GrantedAuthority authority : authorities) {
//                    as.append(authority.getAuthority())
//                            .append(",");
//                }
            List<Role> roleList = userService.getUserRolesByUid(user.getId()); // 刷新token时，同时更新角色信息
            for (Role authority : roleList) {
                as.append(authority.getName())
                        .append(",");
            }
            Date expired = jwtTokenService.getExpiredDate();
            log.info("token 过期时间:"+ expired.toString());
            Map<String, Object> map = new HashMap<>();
            map.put("authorities", as); // 配置用户角色
            map.put("uid", user.getId()); // 配置用户id
            String jwt = jwtTokenService.genToken(map, user.getUsername(), expired);
            return jwt;
//            }
        }
        return "先登录，才能刷新token";
    }
}
